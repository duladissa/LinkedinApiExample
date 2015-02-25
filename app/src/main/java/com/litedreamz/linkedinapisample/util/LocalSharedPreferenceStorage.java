package com.litedreamz.linkedinapisample.util;

/*
 * @author Dulan Dissanayake
 * @date 25/02/2015
 *
 * Implementation of Linkedin OAuth 2.0
 *
 * */

import android.content.Context;
import android.content.SharedPreferences;

import org.scribe.model.Token;

public class LocalSharedPreferenceStorage {

    private static LocalSharedPreferenceStorage mInstance;

    private SharedPreferences mSharedPreferences;

    private final static String PREFERENCES_NAME_LINKEDIN_SAMPLE = "linkedin_api_sample";

    public final static String LINKEDIN_ACCESS_TOKEN = "linkedin_access_token";
    public final static String LINKEDIN_ACCESS_SECRET = "linkedin_access_secret";
    public final static String LINKEDIN_USER_INDUSTRY = "linkedin_user_industry";

    public static LocalSharedPreferenceStorage getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LocalSharedPreferenceStorage(context.getApplicationContext());
        }
        return mInstance;
    }

    private LocalSharedPreferenceStorage(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME_LINKEDIN_SAMPLE, Context.MODE_PRIVATE);
    }

    // Save linkedin access token
    public void saveLinkedinAccessToken(Token accessToken) {
        SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();

        if (accessToken == null) {
            prefsEditor.remove(LINKEDIN_ACCESS_TOKEN);
            prefsEditor.remove(LINKEDIN_ACCESS_SECRET);
            prefsEditor.commit();
            return;
        }

        prefsEditor.putString(LINKEDIN_ACCESS_TOKEN, accessToken.getToken());
        prefsEditor.putString(LINKEDIN_ACCESS_SECRET, accessToken.getSecret());
        prefsEditor.commit();
    }

    // get linkedin access token from device
    public Token getLinkedinAccessToken() {
        String accessToken = mSharedPreferences.getString(LINKEDIN_ACCESS_TOKEN, null);
        String accessSecret = mSharedPreferences.getString(LINKEDIN_ACCESS_SECRET, null);

        if (accessToken == null || accessSecret == null) {
            return null;
        }

        return new Token(accessToken, accessSecret);
    }
}
