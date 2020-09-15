package dev.chatcodes;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;

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
        CredentialAttributes credentialAttributes = new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("ChatCodes", "TwitchOAuthToken")
        );

        String oAuthToken = PasswordSafe.getInstance().getPassword(credentialAttributes);
        textOauthToken.setText(oAuthToken);

        return settingsPanel;
    }

    public char[] getOAuthTokenValue() {
        return textOauthToken.getPassword();
    }
}
