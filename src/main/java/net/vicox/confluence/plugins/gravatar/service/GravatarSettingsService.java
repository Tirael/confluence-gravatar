package net.vicox.confluence.plugins.gravatar.service;

import java.util.Map;

public interface GravatarSettingsService {

    Map<String, String> getSettings();

    void setSettings(Map<String, String> newSettings);
}
