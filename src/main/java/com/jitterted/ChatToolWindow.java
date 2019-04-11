package com.jitterted;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class ChatToolWindow {
  private JButton sendButton;
  private JPanel chatContentPanel;
  private JTextArea chatText;
  private JScrollPane chatScrollPane;

  public ChatToolWindow(ToolWindow toolWindow) {
    sendButton.addActionListener(e -> chatText.append("Hello from window.\r"));
  }

  public JPanel getContent() {
    return chatContentPanel;
  }
}
