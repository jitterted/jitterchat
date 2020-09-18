package dev.chatcodes;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import java.util.Arrays;

public class ChatCodesConfigurable implements Configurable {

    private ConfigForm settingsPane;

    public JComponent createComponent() {
        settingsPane = new ConfigForm();
        return settingsPane.getSettingsPanel();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        // set the Twitch oAuth Token into the credentials store; a secret place for secret stuff
        CredentialAttributes credentialAttributes = new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("ChatCodes", "TwitchOAuthToken")
        );
        Credentials credentials = new Credentials(settingsPane.getTwitchUsernameValue(), settingsPane.getOAuthTokenValue());
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    @Override
    public String getDisplayName() {
        return "ChatCodes";
    }
}
