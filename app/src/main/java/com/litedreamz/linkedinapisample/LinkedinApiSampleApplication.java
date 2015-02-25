package com.litedreamz.linkedinapisample;

import android.app.Application;

import com.litedreamz.linkedinapisample.common.Constants;
import com.litedreamz.linkedinapisample.common.ProductFlavourConstants;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.oauth.OAuthService;

/*
 * @author Dulan Dissanayake
 * @date 25/02/2015
 *
 *
 * */
public class LinkedinApiSampleApplication extends Application {

    private static OAuthService linkedinOAuthService;

    @Override
    public void onCreate() {
        super.onCreate();
        initLinkedinService();
    }

    // Initialize the Linkein API Service
    private void initLinkedinService() {
        if (linkedinOAuthService == null) {
            // never use the .scope to set linkedin scope from code
            linkedinOAuthService = new ServiceBuilder().provider(LinkedInApi.class)
                    .apiKey(ProductFlavourConstants.LINKEDIN_CONSUMER_KEY)
                    .apiSecret(ProductFlavourConstants.LINKEDIN_CONSUMER_SECRET)
                    .callback(Constants.LINKEDIN_OAUTH_CALLBACK_URL).build();
        }
    }

    public static OAuthService getLinkedinOAuthService(){
        return linkedinOAuthService;
    }
}
