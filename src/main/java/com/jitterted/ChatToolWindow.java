package com.jitterted;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.CommandEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

public class ChatToolWindow {
  public static final String TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY = "twitch.api.oauth.access.token";
  public static final String TWITCH_API_CLIENT_ID_PROPERTY_KEY = "twitch.api.client.id";
  public static final String TWITCH_API_CLIENT_SECRET_PROPERTY_KEY = "twitch.api.client.secret";
  private final Project project;
  private JButton connectButton;
  private JPanel chatContentPanel;
  private JTextArea chatText;
  private JScrollPane chatScrollPane;
  private JTextField myMessage;
  private JPanel statusPanel;
  private JLabel editorCommentCountLabel;

  private String oAuthToken;
  private String twitchClientId;
  private String twitchClientSecret;

  public ChatToolWindow(ToolWindow toolWindow, Project project) {
    this.project = project;
    Properties twitchProperties = new Properties();
    try {
      twitchProperties.load(new FileReader("/Users/ted/.twitch.properties"));
      oAuthToken = twitchProperties.getProperty(TWITCH_API_OAUTH_ACCESS_TOKEN_PROPERTY_KEY);
      twitchClientId = twitchProperties.getProperty(TWITCH_API_CLIENT_ID_PROPERTY_KEY);
      twitchClientSecret = twitchProperties.getProperty(TWITCH_API_CLIENT_SECRET_PROPERTY_KEY);

      chatText.setText("Twitch properties loaded.\n\n");

      myMessage.addActionListener(this::sendMessage);

      connectButton.addActionListener(e -> connect());

    } catch (IOException e) {
      chatText.setText("Unable to load Twitch Properties: " + e.getMessage());
      connectButton.setEnabled(false);
    }

    project.getMessageBus().connect().subscribe(
        FileEditorManagerListener.FILE_EDITOR_MANAGER,
        new FileEditorManagerListener() {
          @Override
          public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            VirtualFile newFile = event.getNewFile();
            String commentCount;
            if (newFile == null) {
              commentCount = "<no file>";
            } else {
              ChatCommentLookup service = ServiceManager.getService(ChatCommentLookup.class);
              commentCount = newFile.getNameWithoutExtension() + ": " + service.commentCountFor(newFile);
            }
            SwingUtilities.invokeLater(() -> editorCommentCountLabel.setText(commentCount));
          }
        });

  }

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
    chat.getEventManager()
        .onEvent(ChannelMessageEvent.class)
        .subscribe(this::onChannelMessage);
    chat.getEventManager()
        .onEvent(CommandEvent.class)
        .subscribe(this::onCommand);
  }

  private void onCommand(CommandEvent commandEvent) {
    String commandText = commandEvent.getCommand();
    chatWrite("Command received [" + commandEvent.getUser().getName() + "]: '" + commandText + "'");
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
    String command = split[0];
    if (command.equalsIgnoreCase("line")) {
      if (split.length == 2) {
        try {
          int lineNumber = Integer.parseInt(split[1]) - 1;
          ApplicationManager.getApplication().invokeLater(() -> moveCaretTo(lineNumber));
        } catch (Exception e) {
          chatWrite("Exception during move caret: " + e.getMessage());
        }
      }
    } else if (command.equalsIgnoreCase("comment")) {
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
        SwingUtilities.invokeLater(() -> service.addComment(lineNumber, fileFromEditor(currentEditor()), comment));
      } catch (NumberFormatException e) {
        chatWrite("Bad line number: " + e.getMessage());
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


  private void onChannelMessage(ChannelMessageEvent event) {
    Instant eventFiredAt = event.getFiredAt().toInstant();
    String userName = event.getUser().getName();
    String message = event.getMessage();

    chatWrite(userName + ": " + message);
  }

  private void chatWrite(String message) {
    chatText.append(message + "\n");
  }

  public JPanel getContent() {
    return chatContentPanel;
  }
}
