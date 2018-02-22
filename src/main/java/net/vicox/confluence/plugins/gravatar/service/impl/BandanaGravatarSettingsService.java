package net.vicox.confluence.plugins.gravatar.service.impl;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.thoughtworks.xstream.XStream;
import net.vicox.confluence.plugins.gravatar.service.GravatarSettingsService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BandanaGravatarSettingsService implements GravatarSettingsService {

    private static final String BANDANA_KEY = "gravatar.settings";
    private static final BandanaContext BANDANA_CONTEXT = ConfluenceBandanaContext.GLOBAL_CONTEXT;

    private static List<String> ALLOWED_SETTINGS;
    private static List<String> REQUIRED_SETTINGS;
    private static Map<String, String> DEFAULT_SETTINGS;

    static {
        ALLOWED_SETTINGS = new ArrayList<>();
        ALLOWED_SETTINGS.add("gravatarServerUrl");

        REQUIRED_SETTINGS = new ArrayList<>();
        REQUIRED_SETTINGS.add("gravatarServerUrl");

        DEFAULT_SETTINGS = new LinkedHashMap<>();
        DEFAULT_SETTINGS.put("gravatarServerUrl", "https://www.gravatar.com/avatar/");
    }

    private BandanaManager bandanaManager;
    private XStream xStream;

    public BandanaGravatarSettingsService() {
        this.xStream = new XStream();
        this.xStream.setClassLoader(this.getClass().getClassLoader());
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = loadSettings();
        DEFAULT_SETTINGS.forEach(settings::putIfAbsent);
        return settings;
    }

    @Override
    public void setSettings(Map<String, String> settings) {
        settings.entrySet().removeIf(entry ->
                !ALLOWED_SETTINGS.contains(entry.getKey())
                        || (REQUIRED_SETTINGS.contains(entry.getKey())
                        && StringUtils.isBlank(entry.getValue()))
                        || (DEFAULT_SETTINGS.get(entry.getKey()) != null
                        && DEFAULT_SETTINGS.get(entry.getKey()).equals(entry.getValue())));
        storeSettings(settings);
    }

    private Map<String, String> loadSettings() {
        Map<String, String> settings = new LinkedHashMap<>();

        String settingsXml = (String) bandanaManager.getValue(BANDANA_CONTEXT, BANDANA_KEY);
        if (StringUtils.isNotBlank(settingsXml)) {
            settings.putAll((Map<String, String>) xStream.fromXML(settingsXml));
        }
        return settings;
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
