package org.syncloud.android;

import android.content.SharedPreferences;

import org.apache.log4j.Logger;
import org.syncloud.ssh.EndpointPreference;

import java.net.MalformedURLException;
import java.net.URL;

public class Preferences implements EndpointPreference {

    private static Logger logger = Logger.getLogger(Preferences.class);

    public static final String KEY_PREF_API_URL = "pref_api_url";
    public static final String KEY_PREF_DEBUG_MODE = "pref_debug_mode";
    public static final String KEY_PREF_ACCOUNT_REMOVE = "pref_account_remove";
    public static final String KEY_PREF_EMAIL = "pref_email";
    public static final String KEY_PREF_PASSWORD = "pref_password";
    public static final String KEY_PREF_FEEDBACK_SEND= "pref_feedback_send";
    public static final String KEY_PREF_DISCOVERY_LIBRARY = "pref_discovery_library";
    public static final String KEY_PREF_LOGS = "pref_logs";
    public static final String KEY_PREF_SSH_MODE = "pref_ssh_mode";

    private SharedPreferences preferences;

    public Preferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getApiUrl() {
        return preferences.getString(KEY_PREF_API_URL, "");
    }

    public String getDomain() {
        try {
            return new URL(getApiUrl()).getHost().replace("api.", "");
        } catch (MalformedURLException e) {
            return "syncloud.it";
        }
    }

    public Boolean isDebug() {
        return preferences.getBoolean(KEY_PREF_DEBUG_MODE, false);
    }

    public void setCredentials(String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PREF_EMAIL, email);
        editor.putString(KEY_PREF_PASSWORD, password);
        editor.apply();
    }

    public String getEmail() {
        return preferences.getString(KEY_PREF_EMAIL, null);
    }

    public String getPassword() {
        return preferences.getString(KEY_PREF_PASSWORD, null);
    }

    public boolean hasCredentials() {
        return getEmail() != null;
    }

    public String getDiscoveryLibrary() {
        return preferences.getString(KEY_PREF_DISCOVERY_LIBRARY, "JmDNS");
    }

    @Override
    public boolean isRemote() {
        return preferences.getString(KEY_PREF_SSH_MODE, "Local").equals("Remote");
    }

    @Override
    public void swap() {
        logger.info("swapping ssh mode preference");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PREF_SSH_MODE, isRemote() ? "Local" : "Remote");
        editor.apply();
    }
}
