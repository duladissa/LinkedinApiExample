package com.litedreamz.linkedinapisample.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.litedreamz.linkedinapisample.R;
import com.litedreamz.linkedinapisample.helper.LinkedinWrapper;
import com.litedreamz.linkedinapisample.interfaces.ILinkedinApiCallListner;
import com.litedreamz.linkedinapisample.interfaces.ILinkedinDataLoader;
import com.litedreamz.linkedinapisample.model.LinkedinPeopleProfile;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static final int TAG_BUTTON_GET_CURRENT_USER = 1;
    private static final int TAG_BUTTON_GET_USER_CONNECTIONS = 2;

    private ILinkedinDataLoader linkedinDataLoader;

    private TextView tvDescription;
    private Button btnGetCurrentUser;
    private Button btnUserConnections;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkedinDataLoader = new LinkedinWrapper(MainActivity.this);

        initUI();
    }

    public void initUI(){
        tvDescription = (TextView)findViewById(R.id.tv_description);

        btnGetCurrentUser = (Button)findViewById(R.id.btn_getCurrentUser);
        btnGetCurrentUser.setTag(TAG_BUTTON_GET_CURRENT_USER);
        btnGetCurrentUser.setOnClickListener(btn_OnClickListener);

        btnUserConnections = (Button)findViewById(R.id.btn_getUserConnections);
        btnUserConnections.setTag(TAG_BUTTON_GET_USER_CONNECTIONS);
        btnUserConnections.setOnClickListener(btn_OnClickListener);

    }

    View.OnClickListener btn_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int iTag = (Integer) view.getTag();

            switch (iTag){
                case TAG_BUTTON_GET_CURRENT_USER:
                    getCurrentUser();
                    break;

                case TAG_BUTTON_GET_USER_CONNECTIONS:
                    getUserConnections();
                    break;

            }

        }
    };

    //Step 01)
    private void getCurrentUser() {

        linkedinDataLoader.getCurrentUserProfile(new ILinkedinApiCallListner() {

            @Override
            public void onLinkedinApiCallSuccess(ArrayList<LinkedinPeopleProfile> people) {
                if (people != null) {
                    if (people.size() == 1) {

                        LinkedinPeopleProfile currentUser = people.get(0);
                        String email = currentUser.getEmailAddress();
                        String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();

                        Toast.makeText(MainActivity.this,"Full Name ="+fullName+"\nEmail ="+email,Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onLinkedinApiCallError(String message) {

            }
        });
    }

    //Step 02)
    private void getUserConnections(){

        linkedinDataLoader.getCurrentUserConnections(new ILinkedinApiCallListner() {

            @Override
            public void onLinkedinApiCallSuccess(ArrayList<LinkedinPeopleProfile> people) {
                if (people != null) {
                    if (people.size() > 0) {
                        Toast.makeText(MainActivity.this,"Current User have "+people.size()+" Connections.",Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onLinkedinApiCallError(String message) {

            }
        });
    }
}
