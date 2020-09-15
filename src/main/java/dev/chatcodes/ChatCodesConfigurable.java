package dev.chatcodes;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

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
        CredentialAttributes credentialAttributes = new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("ChatCodes", "TwitchOAuthToken")
        );

        Credentials credentials = new Credentials("", settingsPane.getOAuthTokenValue());
        PasswordSafe.getInstance().set(credentialAttributes, credentials);

        PropertiesComponent.getInstance().setValue(settingsPane.CHAT_CODES_SETTINGS_TWITCH_USER_NAME, settingsPane.getTwitchUsernameValue());
    }

    @Override
    public String getDisplayName() {
        return "ChatCodes";
    }
}
