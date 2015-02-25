package com.litedreamz.linkedinapisample.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import com.litedreamz.linkedinapisample.R;
import com.litedreamz.linkedinapisample.common.Constants;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LinkedinWebDialog extends Dialog {

    private ArrayList<String> whitelistedUrl = new ArrayList<String>();
	
	 // width below which there are no extra margins
    private static final int NO_PADDING_SCREEN_WIDTH = 480;
    // width beyond which we're always using the MIN_SCALE_FACTOR
    private static final int MAX_PADDING_SCREEN_WIDTH = 800;
    // height below which there are no extra margins
    private static final int NO_PADDING_SCREEN_HEIGHT = 800;
    // height beyond which we're always using the MIN_SCALE_FACTOR
    private static final int MAX_PADDING_SCREEN_HEIGHT = 1280;
    
 // the minimum scaling factor for the web dialog (50% of screen size)
    private static final double MIN_SCALE_FACTOR = 0.5;
	
	private ProgressDialog progressDialog = null;

	private OAuthService linkeinOAuthService;
	private Token linkedinRequestToken;


	private WebView linkedinWebView;
	
	/**
	 * Construct a new LinkedIn dialog
	 * 
	 * @param context
	 *            activity {@link android.content.Context}
	 * @param progressDialog
	 *            {@link android.app.ProgressDialog}
	 */
	public LinkedinWebDialog(Context context, ProgressDialog progressDialog, OAuthService linkeinOAuthService) {
		super(context);
		this.progressDialog = progressDialog;
		this.linkeinOAuthService = linkeinOAuthService;
        if(whitelistedUrl != null){
            whitelistedUrl.add("linkedin.com");
        }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);// must call before super.
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_linkedin_web);
		
		calculateSize();
		
		linkedinWebView = (WebView) findViewById(R.id.wv_linkedin_dialog);
		linkedinWebView.setVisibility(View.GONE);
		
		new LinkedInAuthTask().execute();
	}
	
	private void calculateSize() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        // always use the portrait dimensions to do the scaling calculations so we always get a portrait shaped
        // web dialog
        int width = metrics.widthPixels < metrics.heightPixels ? metrics.widthPixels : metrics.heightPixels;
        int height = metrics.widthPixels < metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;

        int dialogWidth = Math.min(
                getScaledSize(width, metrics.density, NO_PADDING_SCREEN_WIDTH, MAX_PADDING_SCREEN_WIDTH),
                metrics.widthPixels);
        int dialogHeight = Math.min(
                getScaledSize(height, metrics.density, NO_PADDING_SCREEN_HEIGHT, MAX_PADDING_SCREEN_HEIGHT),
                metrics.heightPixels);

        getWindow().setLayout(dialogWidth, dialogHeight);
    }
	
	/**
     * Returns a scaled size (either width or height) based on the parameters passed.
     * @param screenSize a pixel dimension of the screen (either width or height)
     * @param density density of the screen
     * @param noPaddingSize the size at which there's no padding for the dialog
     * @param maxPaddingSize the size at which to apply maximum padding for the dialog
     * @return a scaled size.
     */
    private int getScaledSize(int screenSize, float density, int noPaddingSize, int maxPaddingSize) {
        int scaledSize = (int) ((float) screenSize / density);
        double scaleFactor;
        if (scaledSize <= noPaddingSize) {
            scaleFactor = 1.0;
        } else if (scaledSize >= maxPaddingSize) {
            scaleFactor = MIN_SCALE_FACTOR;
        } else {
            // between the noPadding and maxPadding widths, we take a linear reduction to go from 100%
            // of screen size down to MIN_SCALE_FACTOR
            scaleFactor = MIN_SCALE_FACTOR +
                    ((double) (maxPaddingSize - scaledSize))
                            / ((double) (maxPaddingSize - noPaddingSize))
                            * (1.0 - MIN_SCALE_FACTOR);
        }
        return (int) (screenSize * scaleFactor);
    }

	/**
	 * set webview.
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void setWebView(String authorizationUrl) {
		linkedinWebView.setVisibility(View.VISIBLE);
		linkedinWebView.getSettings().setJavaScriptEnabled(true);
		linkedinWebView.getSettings().setSavePassword(false);
		linkedinWebView.setClickable(true);
		linkedinWebView.setFocusableInTouchMode(true);
		linkedinWebView.setWebChromeClient(new WebChromeClient());
		linkedinWebView.setWebViewClient(new HelloWebViewClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            linkedinWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        if(validateAuthorizationUrl(authorizationUrl)){
            linkedinWebView.loadUrl(authorizationUrl);
        }else{
            linkedinWebView.loadUrl("http://api.linkedin.com/");
        }
		linkedinWebView.setPictureListener(new PictureListener() {
			@Override
			public void onNewPicture(WebView view, Picture picture) {
				if(progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

			}
		});

	}

    private boolean validateAuthorizationUrl(String url){
        if(URLUtil.isValidUrl(url) && url.startsWith("https://api.linkedin.com/uas/oauth/authenticate")){
            Uri uri = Uri.parse(url);

            if(whitelistedUrl == null)return false;

            Iterator<String> iterator = whitelistedUrl.iterator();
               while(iterator.hasNext()) {
                    String domain = iterator.next();
                        if(uri.toString().contains(domain)) {
                            return true;
                        }
               }
               return false;
        }
        return false;
    }

	/**
	 * webview client for internal url loading
	 *
	 */
	class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains(Constants.LINKEDIN_OAUTH_CALLBACK_URL)) {
				Uri uri = Uri.parse(url);
				
				cancel();
				
				String verifierString = uri.getQueryParameter("oauth_verifier");
				if(verifierString != null){
					Verifier verifier = new Verifier(verifierString);
					

					for (OnVerifyListener d : listeners) {
						// call listener method
						d.onVerify(verifier,linkedinRequestToken);
					}
				}
				
			} else {
				view.loadUrl(url);
			}

			return true;
		}
	}

	/**
	 * List of listener.
	 */
	private List<OnVerifyListener> listeners = new ArrayList<OnVerifyListener>();

	/**
	 * Register a callback to be invoked when authentication have finished.
	 * 
	 * @param data
	 *            The callback that will run
	 */
	public void setVerifierListener(OnVerifyListener data) {
		listeners.add(data);
	}

	/**
	 * Listener for oauth_verifier.
	 */
	public interface OnVerifyListener {
		/**
		 * invoked when authentication have finished.
		 * 
		 * @param verifier
		 *            oauth_verifier.
		 * @param requestToken
		 * 			  request_token
		 */
		public void onVerify(Verifier verifier, Token requestToken);
	}
	
	/*
	 * 
	 * Use AsyncTask for all the network calls done for linkedin api calls.
	 * */
	private class LinkedInAuthTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			// Temporary URL
			String authURL = "http://api.linkedin.com/";

			try {
				linkedinRequestToken = linkeinOAuthService.getRequestToken();
			    authURL = linkeinOAuthService.getAuthorizationUrl(linkedinRequestToken);
			}
			catch (OAuthException e ) {
                e.printStackTrace();
				return null;
			}
			return authURL;

		}
		
		@Override
		protected void onPostExecute(String authorizationUrl) {
			
			if(authorizationUrl != null){
				setWebView(authorizationUrl);
			}
		}
		
		@Override
		protected void onPreExecute(){
			
		}
	}
}
