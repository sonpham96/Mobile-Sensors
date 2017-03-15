package com.sonpham.sensors_publishers;

import android.content.SharedPreferences;

/**
 * Created by sonpham on 2017/03/03.
 */

public class PreferenceUtility {
    private static final String DEFAULT_PUBLISHER_NAME = "";
    private static final String DEFAULT_HOST = "";
    private static final int DEFAULT_PORT = 2000;
    private static final boolean DEFAULT_SWITCH_VALUE = false;

    public static boolean getSwitchPressure(SharedPreferences sp) {
        return sp.getBoolean("pref_settings_1", DEFAULT_SWITCH_VALUE);
    }

    public static boolean getSwitchLight(SharedPreferences sp) {
        return sp.getBoolean("pref_settings_2", DEFAULT_SWITCH_VALUE);
    }

    public static boolean getSwitchTemperature(SharedPreferences sp) {
        return sp.getBoolean("pref_settings_3", DEFAULT_SWITCH_VALUE);
    }

    public static String getPublisherName(SharedPreferences sp) {
        return sp.getString("pref_pname", DEFAULT_PUBLISHER_NAME);
    }

    public static String getHost(SharedPreferences sp) {
        return sp.getString("host_info", DEFAULT_HOST);
    }

    public static int getPort(SharedPreferences sp) {
        return sp.getInt("port_info", DEFAULT_PORT);
    }

    public static void setPublishername(SharedPreferences sp, String value) {
        sp.edit().putString("pref_pname", value).apply();
    }

    public static void setHost(SharedPreferences sp, String value) {
        sp.edit().putString("host_info", value).apply();
    }

    public static void setPort(SharedPreferences sp, int value) {
        sp.edit().putInt("port_info", value).apply();
    }

    public static void setSwitchPressure(SharedPreferences sp, boolean value) {
        sp.edit().putBoolean("pref_settings_1", value).apply();
    }

    public static void setSwitchLight(SharedPreferences sp, boolean value) {
        sp.edit().putBoolean("pref_settings_2", value).apply();
    }

    public static void setSwitchTemperature(SharedPreferences sp, boolean value) {
        sp.edit().putBoolean("pref_settings_3", value).apply();
    }
}
