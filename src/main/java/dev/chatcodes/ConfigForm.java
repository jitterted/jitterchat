package dev.chatcodes;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ConfigForm {

    public boolean textOauthTokenUpdated = false;
    private JPanel settingsPanel;
    private JPasswordField textOauthToken;

    public ConfigForm() {
        textOauthToken.addActionListener(this::oAuthTokenUpdated);
    }

    private void oAuthTokenUpdated(ActionEvent actionEvent) {
        this.textOauthTokenUpdated = true;
    }

    public JComponent getSettingsPanel() {
        return settingsPanel;
    }

    public char[] getOAuthTokenValue() {
        return textOauthToken.getPassword();
    }
}
