package com.example.harut.applogin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView textViewUsername, textViewPassword;   //Labels: Username, Password
    private EditText editTextUsername, editTextPassword;   //Text fields: Username, Password
    private Button buttonLogin, buttonExit;                //Buttons: Login, Exit
    private LoginButton buttonFacebook;                    //Facebook login Button
    private SignInButton buttonGooglePlus;                 //Google Plus Button
    private Button buttonVK;
    private SharedPreferences sharedPreferences;           //Shared Preferences object
    private SharedPreferences.Editor sharedPreferencesEditor;   //Editor for the Shared Preferences file
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;
    private boolean mIntentInProgress;
    private boolean signedInUser;

    private CallbackManager callbackManager;    //Facebook Callback Manager
    private static int requestCodeFacebook; //Facebook Activity Result request code

    private static final int requestCodeGooglePlus = 888888; //Need to specify the requestCode for the startResolutionForResult(this, int ReqCode);
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;


    private static final String TAG = "MyActivity";

    //From where the login can be made
    public enum LoginSource{
        APP,FACEBOOK, GOOGLEPLUS, VK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("super onCreate");

        Context appContext = getApplicationContext();

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

        VKSdk.initialize(appContext);
        System.out.println("VkSDK init");

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
        vkSetup();
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
        echo("Google Plus Button Setup Completed");
    }

    private void googlePlusLogin() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }

    private void googlePlusLogout() {
        echo("Entering G+ logout");
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            echo("Logging out");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        // Show the signed-in UI
        //showSignedInUI();
        Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        String id = person.getDisplayName();
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        login(LoginSource.GOOGLEPLUS, id, email);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, requestCodeGooglePlus);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                //showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            //showSignedOutUI();
        }
    }

/*/////////////////////////////////
    public void googlePlusSetup(){
        buttonGooglePlus = (SignInButton) findViewById(R.id.buttonGooglePlus);
        buttonGooglePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googlePlusLogin();
            }
        });
        echo("Google Plus Button Setup Completed");
    }

    private void googlePlusLogin() {
        echo("Entering G+ login");
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
            echo("calling resolveSigninError");
        }
    }

    private void googlePlusLogout() {
        echo("Entering G+ logout");
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            echo("Logging out");
        }
    }

    private void resolveSignInError() {
        echo("Entering resolveSignInError");
        if (mConnectionResult.hasResolution()) {
            echo("it has Resolution");
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, requestCodeGooglePlus);
                echo("Started Resolution For Result");
            } catch (IntentSender.SendIntentException e) {
                echo("SendIntentException caught");
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        System.out.println("Entering onConnected");
        signedInUser = false;
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
    }

    private void getProfileInformation() {
        echo("Entering getProfileinformation");
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
        echo("ConnectionSuspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        echo("Entering onConnectionFailed");
        if (!result.hasResolution()) {
            echo("Hasn't resolution");
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
        echo("Entering onStart");
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        echo("Entering onStop");
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    ////////////////////////////////////*/

    public void vkSetup(){
        buttonVK = (Button) findViewById(R.id.buttonVK);
        buttonVK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vkLogin();
            }
        });
    }

    public void vkLogin(){
        VKSdk.login(this, VKScope.EMAIL);
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
                sharedPreferencesEditor.putString("VK_User_ID", key1);
                sharedPreferencesEditor.putString("VK_EMAIL", key2);
            }
        }
        sharedPreferencesEditor.commit();           //Save Preferences
        Toast message = Toast.makeText(this,"Preferences Saved", Toast.LENGTH_SHORT);
        message.show();     //Display on Screen success message
    }

    public void exit(){
        googlePlusLogout();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        echo("Entering onActivityResult");
       if(requestCode == requestCodeFacebook) {
           echo("RequestCode Facebook");
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } else if(requestCode == requestCodeGooglePlus) {
           echo("RequestCode GooglePlus");
           /*if (resultCode == RESULT_OK) {
               echo("ResultCode OK");
               signedInUser = false;
           }
           mIntentInProgress = false;
           if (!mGoogleApiClient.isConnecting()) {
               echo("mGoogleApiClient is not Connecting");
               mGoogleApiClient.connect();
           }*/
           // If the error resolution was not successful we should not resolve further.
           if (resultCode != RESULT_OK) {
               mShouldResolve = false;
           }

           mIsResolving = false;
           mGoogleApiClient.connect();
        } else {
           echo("ReqCode VK");
            VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization
                //echo(res.accessToken.toString());
                String email = res.email;
                String userId = res.userId;
                login(LoginSource.VK,userId, email);
                echo("login() VK");
            }
            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                echo("VK onError");
                }
            });

        }
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

    public static void echo(String s){
        Log.v(TAG, s);
    }
}
