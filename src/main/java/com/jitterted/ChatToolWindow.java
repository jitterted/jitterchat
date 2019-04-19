package com.jitterted;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.CommandEvent;
import com.github.twitch4j.chat.events.channel.ChannelJoinEvent;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class ChatToolWindow {
  private static final Logger log = LoggerFactory.getLogger(ChatToolWindow.class);
  public static final String TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY = "twitch.api.oauth.access.token";
  public static final String TWITCH_API_CLIENT_ID_PROPERTY_KEY = "twitch.api.client.id";
  public static final String TWITCH_API_CLIENT_SECRET_PROPERTY_KEY = "twitch.api.client.secret";
  private static final DateTimeFormatter CHAT_TIME_FORMATTER = DateTimeFormatter.ofPattern("kk:mm");


  private final Project project;
  private JButton connectButton;
  private JPanel chatContentPanel;
  private JTextPane chatTextPane;
  private JScrollPane chatScrollPane;
  private JTextField myMessage;

  private String oAuthToken;
  private String twitchClientId;
  private String twitchClientSecret;
  private HTMLDocument htmlDocument;

  private Editor currentEditor() {
    return FileEditorManager.getInstance(project).getSelectedTextEditor();
  }

  private VirtualFile fileFromEditor(Editor editor) {
    return FileDocumentManager.getInstance().getFile(editor.getDocument());
  }

  private void sendMessage(ActionEvent actionEvent) {
    JTextField textField = (JTextField) actionEvent.getSource();
    String text = textField.getText();

    processCommand(text);

    textField.setText("");
  }
  private Element chatElement;

  public ChatToolWindow(ToolWindow toolWindow, Project project) {
    initializeTextPane();
    this.project = project;
    Properties twitchProperties = new Properties();
    try {
      twitchProperties.load(new FileReader("/Users/ted/.twitch.properties"));
      oAuthToken = twitchProperties.getProperty(TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY);
      twitchClientId = twitchProperties.getProperty(TWITCH_API_CLIENT_ID_PROPERTY_KEY);
      twitchClientSecret = twitchProperties.getProperty(TWITCH_API_CLIENT_SECRET_PROPERTY_KEY);

      addItalicized("Twitch properties loaded.\n\n");

      myMessage.addActionListener(this::sendMessage);

      connectButton.addActionListener(e -> connect());

    } catch (IOException e) {
      addItalicized("Unable to load Twitch Properties: " + e.getMessage());
      connectButton.setEnabled(false);
    }

  }

  private void connect() {
    OAuth2Credential credential = new OAuth2Credential("twitch", oAuthToken);

    TwitchClient twitchClient = TwitchClientBuilder.builder()
                                                   .withClientId(twitchClientId)
                                                   .withClientSecret(twitchClientSecret)
                                                   .withEnableChat(true)
                                                   .withChatAccount(credential)
                                                   .withCommandTrigger("!")
                                                   .build();
    TwitchChat chat = twitchClient.getChat();

    chat.joinChannel("jitterted");
    chat.sendMessage("jitterted", "The JitterChat remote control bot is here!");

    EventManager chatEventManager = chat.getEventManager();

    chatEventManager
        .onEvent(ChannelMessageEvent.class)
        .subscribe(this::onChannelMessage);

    chatEventManager
        .onEvent(CommandEvent.class)
        .subscribe(this::onCommand);

    chatEventManager
        .onEvent(ChannelLeaveEvent.class)
        .subscribe(this::onLeave);

     chatEventManager
         .onEvent(ChannelJoinEvent.class)
         .subscribe(this::onJoin);

    addItalicized("JitterChat is connected.");
    connectButton.setText("Disconnect from Twitch");
    connectButton.setEnabled(false);
  }

  private void onLeave(ChannelLeaveEvent channelLeaveEvent) {
    addItalicized("<small>" + channelLeaveEvent.getUser().getName() + " has left.</small>");
  }

  private void onCommand(CommandEvent commandEvent) {
    String commandText = commandEvent.getCommand();

    log.debug("Command received [" + commandEvent.getUser().getName() + "]: '" + commandText + "'");
    // from user: !line 35
    // then commandText is: line 35
    try {
      processCommand(commandText);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void processCommand(String commandText) {
    String[] split = StringUtils.split(commandText, " ", 3);
    String command = split[0].toLowerCase();
    if (command.equals("line") || command.equals("l")) {
      if (split.length == 2) {
        try {
          int lineNumber = Integer.parseInt(split[1]) - 1;
          ApplicationManager.getApplication().invokeLater(() -> moveCaretTo(lineNumber));
        } catch (Exception e) {
          chatWrite("Exception during move caret: " + e.getMessage());
        }
      }
    } else if (command.equals("comment") || command.equals("c")) {
      if (split.length < 3) {
        chatWrite("Expected 3 pieces for '" + commandText + "'");
        return;
      }
      int lineNumber;
      try {
        lineNumber = Integer.parseInt(split[1]) - 1;
        // comment 26 what is this thing here?

        String comment = split[2];
        ChatCommentLookup service = ServiceManager.getService(ChatCommentLookup.class);
        ApplicationManager.getApplication().invokeLater(() -> {
          VirtualFile virtualFile = fileFromEditor(currentEditor());
          service.addComment(lineNumber, virtualFile, comment);
        });

      } catch (NumberFormatException e) {
        chatWrite("Bad line number: " + e.getMessage());
      } catch (Exception e) {
        chatWrite("Exception during comment processing: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  private void moveCaretTo(int lineNumber) {
    Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (selectedTextEditor == null) {
      chatWrite("Error: couldn't find the selected text editor");
      return;
    }

    selectedTextEditor.getCaretModel().moveToLogicalPosition(new LogicalPosition(lineNumber, 0));
  }

  private void onJoin(ChannelJoinEvent channelJoinEvent) {
    addItalicized("<small>" + channelJoinEvent.getUser().getName() + " has joined.</small>");
  }

  private void addItalicized(String text) {
    addContentInDiv("<i>" + text + "</i>");
  }

  public JPanel getContent() {
    return chatContentPanel;
  }

  private void onChannelMessage(ChannelMessageEvent event) {
    Instant eventFiredAt = event.getFiredAt().toInstant();
    String userName = event.getUser().getName();
    String message = event.getMessage();

    String time = CHAT_TIME_FORMATTER.format(eventFiredAt.atZone(Clock.systemDefaultZone().getZone()));

    chatWrite("<small>[" + time + "]</small> <b>" + userName + "</b>: " + message);
  }

  private void chatWrite(String message) {
    addHtml("<p>" + message + "</p>");
  }

  private void initializeTextPane() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule("body { font-face: sans-serif; }");

    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
    chatTextPane.setEditorKit(htmlEditorKit);
    chatTextPane.setDocument(htmlDocument);
    chatTextPane.setText("<html><head></head><body><div id='chat'></div></body></html>");

    chatElement = htmlDocument.getElement("chat");

    addContentInDiv("Ready to go...");
  }

  private void addContentInDiv(String text) {
    addHtml("<div>" + text + "</div>");
  }

  private void addHtml(String content)  {
    try {
      htmlDocument.insertBeforeEnd(chatElement, content);
    } catch (BadLocationException | IOException e) {
      e.printStackTrace();
    }
  }
}
