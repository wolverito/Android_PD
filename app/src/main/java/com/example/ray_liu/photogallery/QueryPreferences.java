package com.example.ray_liu.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by ray_liu on 16/3/25.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    //return the query value stored in shared preferences
    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
        //The 2nd input upside, specifies the default return value
        // that should be used if there is no entry for the key.
    }

    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
        //SharedPreferences.Editor is the class used to stash values in SP.
        // to group sets of changes together in transactions.
    }

    public static String getLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID,null);
    }

    public static void setLastResultId(Context context, String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply();
    }
    public static boolean isAlarmOn(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON,false);
    }
    public static void setAlarmOn(Context context, boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON,isOn)
                .apply();
    }
}
