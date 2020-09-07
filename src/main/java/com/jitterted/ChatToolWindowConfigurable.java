package com.jitterted;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public class ChatToolWindowConfigurable implements Configurable {

    public static final String SETTINGS_OAUTHTOKEN = "es.chatco.settings.oauthtoken";
    private ConfigForm settingsPane;

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsPane.getOauthTokenTextComponent();
    }

    public JComponent createComponent() {
        settingsPane = new ConfigForm();

        return settingsPane.getSettingsPanel();
    }

    @Override
    public boolean isModified() {
        return settingsPane.isUpdated;
    }

    @Override
    public void apply() throws ConfigurationException {
        String oAuthValue = settingsPane.getOauthTokenValue();
        PropertiesComponent.getInstance().setValue(SETTINGS_OAUTHTOKEN, oAuthValue);
        settingsPane.isUpdated = false;
    }

    @Override
    public String getDisplayName() {
        return "ChatCodes";
    }
}
