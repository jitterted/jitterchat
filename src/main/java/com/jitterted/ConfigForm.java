package com.jitterted;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigForm implements ActionListener {
    private JPanel settingsPanel;
    private JPasswordField textOauthToken;
    public boolean isUpdated = false;

    public JComponent getSettingsPanel() {
        textOauthToken.addActionListener(this);
        return settingsPanel;
    }

    public String getOauthTokenValue() {
        return String.valueOf(textOauthToken.getPassword());
    }

    public void setOauthTokenValue(String oAuthToken) {
        textOauthToken.setText(oAuthToken);
    }

    public JPasswordField getOauthTokenTextComponent() {
        return textOauthToken;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == textOauthToken) {
            this.isUpdated = true;
        }
    }
}
