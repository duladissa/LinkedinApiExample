package com.litedreamz.linkedinapisample.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.litedreamz.linkedinapisample.LinkedinApiSampleApplication;
import com.litedreamz.linkedinapisample.R;
import com.litedreamz.linkedinapisample.interfaces.ILinkedinApiCallListner;
import com.litedreamz.linkedinapisample.interfaces.ILinkedinDataLoader;
import com.litedreamz.linkedinapisample.model.LinkedinPeopleProfile;
import com.litedreamz.linkedinapisample.ui.LinkedinWebDialog;
import com.litedreamz.linkedinapisample.util.LocalSharedPreferenceStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;


/*
 * @author Dulan Dissanayake
 * @date 25/02/2015
 * 
 * Implementation of Linkedin OAuth 2.0
 * 
 * */

public class LinkedinWrapper implements ILinkedinDataLoader {

	// Linkedin people fields
	public static final String LINKEDIN_PEOPLE_FIELD_EMAIL_ADDRESS = "emailAddress";
	public static final String LINKEDIN_PEOPLE_FIELD_FIRST_NAME = "firstName";
	public static final String LINKEDIN_PEOPLE_FIELD_LAST_NAME = "lastName";
	public static final String LINKEDIN_PEOPLE_FIELD_PICTURE_URL = "pictureUrl";
	public static final String LINKEDIN_PEOPLE_FIELD_INDUSTRY = "industry";

	// Linkedin api urls
	private static final String PROTECTED_URL_GET_CURRENT_USER_PROFILE = "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,picture-url,industry)";
	public static final String PROTECTED_URL_GET_CURRENT_USER_CONNECTIONS = "http://api.linkedin.com/v1/people/~/connections";
	public static final String PROTECTED_URL_POST_MESSAGES_BETWEEN_CONNECTIONS = "http://api.linkedin.com/v1/people/~/mailbox";
	public static final String PROTECTED_URL_POST_POSTING_SHARES = "http://api.linkedin.com/v1/people/~/shares";
	
	// Current Operations
	private static final int CURRENT_OPERATION_GET_USER_PROFILE = 0;
	private static final int CURRENT_OPERATION_GET_USER_CONNECTIONS = 1;
	
	private Activity activity;
	private OAuthService linkedinOAuthService;
	private LocalSharedPreferenceStorage localSharedPreferenceStorage;
	private ILinkedinLoginListner linkedinLoginListner;
	
	private ILinkedinApiCallListner linkeinApiCallListner;

    private Gson gson;

	public LinkedinWrapper(Activity context) {

        gson = new Gson();
		linkedinOAuthService = LinkedinApiSampleApplication.getLinkedinOAuthService();
        localSharedPreferenceStorage = LocalSharedPreferenceStorage.getInstance(context);
		setLinkedinLoginListner(new ILinkedinLoginListner() {

			@Override
			public void onLoginSuccess(int currentOperation,
					ILinkedinApiCallListner listner) {
				linkeinApiCallListner = listner;
				executeCurrentOperation(currentOperation, linkeinApiCallListner);
			}

			@Override
			public void onLoginError() {

			}
		});
		this.activity = context;
	}

	// Access token not available
	private void loginToLinkedin(final int currentOperation,
			final ILinkedinApiCallListner listner) {
		ProgressDialog progressDialog = new ProgressDialog(activity);

		LinkedinWebDialog linkedInDialog = new LinkedinWebDialog(activity,
				progressDialog, linkedinOAuthService);
		linkedInDialog.show();

		// set call back listener to get oauth_verifier value
		linkedInDialog.setVerifierListener(new LinkedinWebDialog.OnVerifyListener() {
			@Override
			public void onVerify(final Verifier verifier,
					final Token requestToken) {

				Thread t1 = new Thread() {
					public void run() {
						Token accessToken = linkedinOAuthService
								.getAccessToken(requestToken, verifier);

                        localSharedPreferenceStorage.saveLinkedinAccessToken(accessToken);

						linkedinLoginListner.onLoginSuccess(currentOperation,
								listner);
					}
				};
				t1.start();
			}
		});

		// set progress dialog
		progressDialog.setMessage(activity.getResources().getString(
				R.string.information_message_loading));
		progressDialog.setCancelable(true);
		
		if(progressDialog != null && !progressDialog.isShowing())
		progressDialog.show();
	}

	public interface ILinkedinLoginListner {
		public void onLoginSuccess(int currentOperation,
                                   ILinkedinApiCallListner listner);

		public void onLoginError();
	}

	private void setLinkedinLoginListner(ILinkedinLoginListner listner) {
		linkedinLoginListner = listner;
	}

	// Executes the current operations by the user request.
	private void executeCurrentOperation(int currentOperation,
			ILinkedinApiCallListner linkedinApiCallListner) {
		switch (currentOperation) {

		case CURRENT_OPERATION_GET_USER_PROFILE:
            executeRequest(linkeinApiCallListner,currentOperation,PROTECTED_URL_GET_CURRENT_USER_PROFILE);
			break;
			
		case CURRENT_OPERATION_GET_USER_CONNECTIONS:
            executeRequest(linkeinApiCallListner,currentOperation,PROTECTED_URL_GET_CURRENT_USER_CONNECTIONS);
			break;
		}
	}

	private void executeRequest(ILinkedinApiCallListner linkeinApiCallListner,int currentOperation,String url) {
		new LinkedInRequestAsyncTask(linkeinApiCallListner,currentOperation,url).execute();
	}

	private class LinkedInRequestAsyncTask extends
            AsyncTask<String, String, String> {
		private ILinkedinApiCallListner linkeinApiCallListner;
		private String url;
		private int currentOperation;

		public LinkedInRequestAsyncTask(ILinkedinApiCallListner listner,int currentOperation,String url) {
			linkeinApiCallListner = listner;
			this.currentOperation = currentOperation;
			this.url = url;
		}
		
		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... params) {
            Response response = null;
            OAuthRequest request = null;
            if((currentOperation == CURRENT_OPERATION_GET_USER_PROFILE) || (currentOperation == CURRENT_OPERATION_GET_USER_CONNECTIONS)){
                request = new OAuthRequest(Verb.GET,
                            url);
            }

            request.addHeader("x-li-format", "json");
            linkedinOAuthService.signRequest(
                    getLinkedinAccessTokenFromDevice(), request);
            response = request.send();
            return response.getBody();

		}

		@Override
		protected void onPostExecute(String responseJsonObject) {
			if (responseJsonObject != null) {
                JSONObject jobjResponse;
				ArrayList<LinkedinPeopleProfile> linkedinPeople = new ArrayList<LinkedinPeopleProfile>();
				try{
                    jobjResponse = new JSONObject(responseJsonObject);
					
					if(currentOperation == CURRENT_OPERATION_GET_USER_PROFILE){
					
							LinkedinPeopleProfile currentUser = new LinkedinPeopleProfile();

							if (jobjResponse.has(LINKEDIN_PEOPLE_FIELD_FIRST_NAME)) {
								currentUser.setFirstName(jobjResponse.getString(LINKEDIN_PEOPLE_FIELD_FIRST_NAME));
							}
							if (jobjResponse.has(LINKEDIN_PEOPLE_FIELD_LAST_NAME)) {
								currentUser.setLastName(jobjResponse.getString(LINKEDIN_PEOPLE_FIELD_LAST_NAME));						
							}
							if (jobjResponse.has(LINKEDIN_PEOPLE_FIELD_EMAIL_ADDRESS)) {
								currentUser.setEmailAddress(jobjResponse.getString(LINKEDIN_PEOPLE_FIELD_EMAIL_ADDRESS));
							}
							if(jobjResponse.has(LINKEDIN_PEOPLE_FIELD_PICTURE_URL)){
								currentUser.setPictureUrl(jobjResponse.getString(LINKEDIN_PEOPLE_FIELD_PICTURE_URL));
							}
							if(jobjResponse.has(LINKEDIN_PEOPLE_FIELD_INDUSTRY)){
								currentUser.setIndustry(jobjResponse.getString(LINKEDIN_PEOPLE_FIELD_INDUSTRY));								
							}
							

							linkedinPeople.add(currentUser);
		
					}else if(currentOperation == CURRENT_OPERATION_GET_USER_CONNECTIONS){
                        try {
                            JSONArray connections = jobjResponse.getJSONArray("values");
                            LinkedinPeopleProfile profile = null;
                            for (int i = 0; i < connections.length(); i++) {
                                profile = gson.fromJson(connections.get(i).toString(), LinkedinPeopleProfile.class);

                                linkedinPeople.add(profile);
                            }
                        }catch (JSONException e) {
                        }
                    }

					linkeinApiCallListner
					.onLinkedinApiCallSuccess(linkedinPeople);
				}catch (JSONException e) {
					linkeinApiCallListner.onLinkedinApiCallError(e
							.getLocalizedMessage());
				}
				
				
			} else {
				linkeinApiCallListner
						.onLinkedinApiCallError("No user available.");
			}
		}
	}
	

	// Get the previously extracted linkedin accesstoken if available in
	// sharedpreferences
    @Override
	public Token getLinkedinAccessTokenFromDevice() {
		return localSharedPreferenceStorage.getLinkedinAccessToken();
	}
	

	@Override
	public void getCurrentUserProfile(ILinkedinApiCallListner listner) {
		Token accessToken = getLinkedinAccessTokenFromDevice();
		if (accessToken == null) {
			loginToLinkedin(CURRENT_OPERATION_GET_USER_PROFILE, listner);
		} else {
			linkedinLoginListner.onLoginSuccess(
					CURRENT_OPERATION_GET_USER_PROFILE, listner);
		}
	}

	@Override
	public void getCurrentUserConnections(ILinkedinApiCallListner listner) {
		Token accessToken = getLinkedinAccessTokenFromDevice();
		if (accessToken == null) {
			loginToLinkedin(CURRENT_OPERATION_GET_USER_CONNECTIONS, listner);
		} else {
			linkedinLoginListner.onLoginSuccess(
					CURRENT_OPERATION_GET_USER_CONNECTIONS, listner);
		}
	}

	
}
