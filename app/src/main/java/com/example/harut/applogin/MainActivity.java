package com.example.harut.applogin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView textViewUsername, textViewPassword;   //Labels: Username, Password
    private EditText editTextUsername, editTextPassword;   //Text fields: Username, Password
    private Button buttonLogin, buttonExit;                //Buttons: Login, Exit
    private LoginButton buttonFacebook;                    //Facebook login Button
    private SignInButton buttonGooglePlus;                 //Google Plus Button
    private SharedPreferences sharedPreferences;           //Shared Preferences object
    private SharedPreferences.Editor sharedPreferencesEditor;   //Editor for the Shared Preferences file
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;
    private boolean mIntentInProgress;
    private boolean signedInUser;

    private CallbackManager callbackManager;    //Facebook Callback Manager
    private static int requestCodeFacebook; //Facebook Activity Result request code

    private static final int requestCodeGooglePlus = 1111; //Need to specify the requestCode for the startResolutionForResult(this, int ReqCode);


    //From where the login can be made
    public enum LoginSource{
        APP,FACEBOOK, GOOGLEPLUS, VK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("super onCreate");

        Context appContext = getApplicationContext();

       // VKSdk.initialize(appContext);
        //System.out.println("VkSDK init");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        FacebookSdk.sdkInitialize(appContext);
        System.out.println("Facebook init");

        callbackManager = CallbackManager.Factory.create();
        System.out.println("CallbackManager");


        setContentView(R.layout.activity_main);
        System.out.println("Set content view");



        getInit(); // initialize UI elements
        System.out.println("UI init");
    }

    // initialize UI elements
    public void getInit(){
        textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewPassword = (TextView) findViewById(R.id.textViewPassword);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonExit = (Button) findViewById(R.id.buttonExit);
        facebookSetup();        //facebook button initialization
        googlePlusSetup();
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(LoginSource.APP, "", "");
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
    }

    public void facebookSetup(){
        buttonFacebook = (LoginButton) findViewById(R.id.buttonFacebook);
        buttonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String userId = loginResult.getAccessToken().getUserId();
                String token = loginResult.getAccessToken().getToken();
                login(LoginSource.FACEBOOK, userId, token);
            }

            @Override
            public void onCancel() {
                Toast message = Toast.makeText(getApplicationContext(), "Login attempt canceled", Toast.LENGTH_SHORT);
                message.show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast message = Toast.makeText(getApplicationContext(), "Login attempt failed", Toast.LENGTH_SHORT);
                message.show();
            }
        });
        requestCodeFacebook = buttonFacebook.getRequestCode();
    }

    public void googlePlusSetup(){
        buttonGooglePlus = (SignInButton) findViewById(R.id.buttonGooglePlus);
        buttonGooglePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googlePlusLogin();
            }
        });
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }

        private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, requestCodeGooglePlus);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        signedInUser = false;
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                login(LoginSource.GOOGLEPLUS, personName, email);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            System.out.println("onConnectionFailed error");
            return;
        }
        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;
            if (signedInUser) {
                resolveSignInError();
            }
        }
    }


    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    //Receives login source. Pass any String when not using keys
    public void login(LoginSource loginSource, String key1, String key2){
        sharedPreferences = getSharedPreferences("My_Login_Preferences", MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        //Switch the source of login info
        switch (loginSource) {
            case APP: {
                sharedPreferencesEditor.putString("APP_Username", editTextUsername.getText().toString());
                sharedPreferencesEditor.putString("APP_Password", editTextPassword.getText().toString());
            }
            case FACEBOOK:{
                sharedPreferencesEditor.putString("FACEBOOK_User_ID", key1);
                sharedPreferencesEditor.putString("FACEBOOK_Auth_Token", key2);
            }
            case GOOGLEPLUS:{
                sharedPreferencesEditor.putString("GOOGLEPLUS_User_Name", key1);
                sharedPreferencesEditor.putString("GOOGLEPLUS_EMAIL", key2);
            }
            case VK:{

            }
        }
        sharedPreferencesEditor.commit();           //Save Preferences
        Toast message = Toast.makeText(this,"Preferences Saved", Toast.LENGTH_SHORT);
        message.show();     //Display on Screen success message
    }

    public void exit(){
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(requestCode == requestCodeFacebook) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } else if(requestCode == requestCodeGooglePlus){
             if (resultCode == RESULT_OK) {
                    signedInUser = false;
                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
        } /*else if(requestCode == ){

        } else {

        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
