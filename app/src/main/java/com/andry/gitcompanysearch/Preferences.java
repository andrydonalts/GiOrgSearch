package com.andry.gitcompanysearch;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {
    private static final String PREF_SEARCH_QUERY = "prefSearch";

    public static String getQueryPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setQueryPreferences(String query, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}
