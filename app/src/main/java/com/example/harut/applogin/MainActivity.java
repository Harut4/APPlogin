package com.example.harut.applogin;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewPassword;   //Labels: Username, Password
    private EditText editTextUsername, editTextPassword;   //Text fields: Username, Password
    private Button buttonLogin, buttonExit;                //Buttons: Login, Exit
    private LoginButton buttonFacebook;                    //Facebook login button
    private SharedPreferences sharedPreferences;           //Shared Preferences object
    private SharedPreferences.Editor sharedPreferencesEditor;   //Editor for the Shared Preferences file

    private CallbackManager callbackManager;    //Facebook Callback Manager

    //From where the login can be made
    public enum LoginSource{
        APP,FACEBOOK, GOOGLEPLUS, VK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        getInit(); // initialize UI elements
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
    }

    //Receives login source, and in case of need, receives keys to store
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
