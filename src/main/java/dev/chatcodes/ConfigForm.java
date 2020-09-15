package dev.chatcodes;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.PropertiesComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ConfigForm {

    public static final String CHAT_CODES_SETTINGS_TWITCH_USER_NAME = "ChatCodes.Settings.TwitchUserName";
    public boolean textOauthTokenUpdated = false;
    private JPanel settingsPanel;
    private JPasswordField textOauthToken;
    private JTextField textTwitchUsername;

    public ConfigForm() {
        textOauthToken.addActionListener(this::oAuthTokenUpdated);
    }

    private void oAuthTokenUpdated(ActionEvent actionEvent) {
        this.textOauthTokenUpdated = true;
    }

    public JComponent getSettingsPanel() {
        initFields();
        return settingsPanel;
    }

    private void initFields() {
        CredentialAttributes credentialAttributes = new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("ChatCodes", "TwitchOAuthToken")
        );
        String oAuthToken = PasswordSafe.getInstance().getPassword(credentialAttributes);
        textOauthToken.setText(oAuthToken);

        textTwitchUsername.setText(PropertiesComponent.getInstance().getValue(CHAT_CODES_SETTINGS_TWITCH_USER_NAME));
    }

    public char[] getOAuthTokenValue() {
        return textOauthToken.getPassword();
    }

    public String getTwitchUsernameValue() {
        return textTwitchUsername.getText();
    }
}
