package dev.chatcodes;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.CommandEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.common.html.HtmlEscapers;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
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
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ChatToolWindow {

    private static final Logger log = LoggerFactory.getLogger(ChatToolWindow.class);
    private static final DateTimeFormatter CHAT_TIME_FORMATTER = DateTimeFormatter.ofPattern("kk:mm");
    private final Project project;
    private TwitchChat twitchChat;
    private JPanel chatContentPanel;
    private JTextPane chatTextPane;
    private JScrollPane chatScrollPane;
    private JTextField myMessage;

    private HTMLDocument htmlDocument;
    private Element chatElement;

    public ChatToolWindow(ToolWindow toolWindow, Project project) throws IOException {
        this.project = project;
        initializeTextPane();

        myMessage.addActionListener(this::sendMessage);
        connect();
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

        if (text.isEmpty()) {
            return;
        }

        sendChatMessage(text);

        Instant theTimeNow = Instant.now();
        formatAndWriteChatMessage(theTimeNow, "Spabby", text);

        textField.setText("");
    }

    private void sendMessageToEventLog(String text) {
        Notifications.Bus.notify(new Notification("ChatCodes", "Tooltip", text, NotificationType.INFORMATION));
    }

    private void connect() throws IOException {

        addItalicized("Get set...");

        CredentialAttributes credentialAttributes = new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("ChatCodes", "TwitchOAuthToken")
        );

        String oAuthToken = PasswordSafe.getInstance().getPassword(credentialAttributes);

        OAuth2Credential credential = new OAuth2Credential(
            "twitch",
            oAuthToken
        );

        TwitchClient twitchClient = TwitchClientBuilder.builder()
            .withEnableChat(true)
            .withChatAccount(credential)
            .withCommandTrigger("!")
            .build();

        twitchChat = twitchClient.getChat();

        twitchChat.joinChannel("spabby");
        sendChatMessage("The bot has arrived");

        EventManager chatEventManager = twitchChat.getEventManager();

        chatEventManager
            .onEvent(ChannelMessageEvent.class)
            .subscribe(this::onChannelMessage);

        chatEventManager
            .onEvent(CommandEvent.class)
            .subscribe(this::onCommand);

        addItalicized("GO GO GO!");

        myMessage.setEnabled(true);
    }

    private void sendChatMessage(String message) {
        //@TODO Fix this so it's not hard-coded and is the actual streamer's name
        twitchChat.sendMessage("spabby", message);
        if (message.charAt(0) != '!') {
            return;
        }

        //@TODO See above
        processCommand(message.substring(1), "Spabby");
    }

    private void onCommand(CommandEvent commandEvent) {
        String commandText = commandEvent.getCommand();

        log.debug("Command received [" + commandEvent.getUser().getName() + "]: '" + commandText + "'");
        // from user: !line 35
        // then commandText is: line 35
        try {
            if (commandText.charAt(0) == ' ') {
                return;
            }

            processCommand(commandText, commandEvent.getUser().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processCommand(String commandText, String commandUser) {
        String[] split = StringUtils.split(commandText, " ", 3);
        String command = split[0].toLowerCase();

        // move caret to line
        if (command.equals("line") || command.equals("l")) {
            if (split.length == 2) {
                try {
                    int lineNumber = Integer.parseInt(split[1]) - 1;
                    ApplicationManager.getApplication().invokeLater(() -> moveCaretAndScrollTo(lineNumber));
                } catch (Exception e) {
                    log.debug("Exception during move caret: " + e.getMessage());
                }
            }
            return;
        }

        if (command.equals("comment") || command.equals("c")) {
            int lineNumber = Integer.parseInt(split[1]) - 1;

            ApplicationManager.getApplication().invokeLater(() -> moveCaretAndScrollTo(lineNumber));
            ApplicationManager.getApplication().invokeLater(() -> addTheComment(split[2], commandUser));

            return;
        }

        if (command.equals("tooltip") || command.equals("t")) {
            if (split.length < 3) {
                chatWrite("Expected 3 pieces for '" + commandText + "'");
                return;
            }
            int lineNumber;
            try {
                lineNumber = Integer.parseInt(split[1]) - 1;
                // comment 26 what is this thing here?

                String comment = split[2];
                ChatCommentModel service = ServiceManager.getService(ChatCommentModel.class);
                ApplicationManager.getApplication().invokeLater(() -> {
                    VirtualFile virtualFile = fileFromEditor(currentEditor());
                    service.addComment(lineNumber, virtualFile, comment, commandUser);
                });
                sendMessageToEventLog(commandUser + " added a tooltip to line " + split[1]);

            } catch (NumberFormatException e) {
                chatWrite("Bad line number: " + e.getMessage());
            } catch (Exception e) {
                chatWrite("Exception during comment processing: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
    }

    private void addTheComment(String comment, String user) {
        Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedTextEditor == null) {
            chatWrite("Error: couldn't find the selected text editor");
            return;
        }

        final Document document = selectedTextEditor.getDocument();

        WriteCommandAction.runWriteCommandAction(project, () ->
            document.insertString(selectedTextEditor.getCaretModel().getOffset(), "// " + user + ": " + comment + "\n")
        );

    }

    private void moveCaretAndScrollTo(int lineNumber) {
        Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedTextEditor == null) {
            chatWrite("Error: couldn't find the selected text editor");
            return;
        }

        selectedTextEditor.getCaretModel().moveToLogicalPosition(new LogicalPosition(lineNumber, 0));
        selectedTextEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
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
        message = HtmlEscapers.htmlEscaper().escape(message);

        formatAndWriteChatMessage(eventFiredAt, userName, message);
    }

    private void formatAndWriteChatMessage(Instant eventFiredAt, String userName, String message) {
        String time = CHAT_TIME_FORMATTER.format(eventFiredAt.atZone(Clock.systemDefaultZone().getZone()));

        chatWrite("<small>[" + time + "]</small> <b>" + userName + "</b>: " + message);
    }

    private void chatWrite(String message) {
        addHtml("<p>" + message + "</p>");
    }

    private void initializeTextPane() {
        StyleSheet styleSheet = new StyleSheet();
        styleSheet.addRule("body { font-face: sans-serif; font-size: large; } #chat { padding: 2px; }");

        DefaultCaret caret = (DefaultCaret) chatTextPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        htmlEditorKit.setStyleSheet(styleSheet);
        htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        chatTextPane.setEditorKit(htmlEditorKit);
        chatTextPane.setDocument(htmlDocument);
        chatTextPane.setText("<html><head></head><body><div id='chat'></div></body></html>");

        chatElement = htmlDocument.getElement("chat");

        addItalicized("On your marks...");
    }

    private void addContentInDiv(String text) {
        addHtml("<div>" + text + "</div>");
    }

    private void addHtml(String content) {
        try {
            htmlDocument.insertBeforeEnd(chatElement, content);
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }

        chatTextPane.setCaretPosition(chatTextPane.getDocument().getLength());
    }
}
