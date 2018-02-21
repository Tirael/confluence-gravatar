package net.vicox.confluence.plugins.gravatar.service.impl;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.thoughtworks.xstream.XStream;
import net.vicox.confluence.plugins.gravatar.service.GravatarSettingsService;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class BandanaGravatarSettingsService implements GravatarSettingsService {

    private static final String BANDANA_KEY = "gravatar.settings";
    private static final BandanaContext BANDANA_CONTEXT = ConfluenceBandanaContext.GLOBAL_CONTEXT;

    private static final String GRAVATAR_SERVER_URL_KEY = "gravatarServerUrl";
    private static final String GRAVATAR_SERVER_URL_DEFAULT_VALUE = "https://www.gravatar.com/avatar/";

    private BandanaManager bandanaManager;
    private XStream xStream;

    public BandanaGravatarSettingsService() {
        this.xStream = new XStream();
        this.xStream.setClassLoader(this.getClass().getClassLoader());
    }

    @Override
    public String getGravatarServerUrl() {
        return getSetting(GRAVATAR_SERVER_URL_KEY, GRAVATAR_SERVER_URL_DEFAULT_VALUE);
    }

    @Override
    public void setGravatarServerUrl(String gravatarServerUrl) {
        setSetting(GRAVATAR_SERVER_URL_KEY, GRAVATAR_SERVER_URL_DEFAULT_VALUE, gravatarServerUrl);
    }

    private String getSetting(String key, String defaultValue) {
        Map<String, String> settings = loadSettings();
        return settings.get(key) == null ? defaultValue : settings.get(key);
    }

    private void setSetting(String key, String defaultValue, String value) {
        Map<String, String> settings = loadSettings();
        if (defaultValue.equals(value)) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }
        storeSettings(settings);
    }

    private Map<String, String> loadSettings() {
        String settingsXml = (String) bandanaManager.getValue(BANDANA_CONTEXT, BANDANA_KEY);
        return StringUtils.isBlank(settingsXml) ? new LinkedHashMap<>() : (Map<String, String>) xStream.fromXML(settingsXml);
    }

    private void storeSettings(Map<String, String> settings) {
        if (settings.isEmpty()) {
            bandanaManager.removeValue(BANDANA_CONTEXT, BANDANA_KEY);
        } else {
            bandanaManager.setValue(BANDANA_CONTEXT, BANDANA_KEY, xStream.toXML(settings));
        }
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }
}
