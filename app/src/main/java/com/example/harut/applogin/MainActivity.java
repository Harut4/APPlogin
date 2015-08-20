package com.example.harut.applogin;

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

public class MainActivity extends AppCompatActivity {

    TextView textViewLogin, textViewPassword;
    EditText editTextLogin, editTextPassword;
    Button buttonSave, buttonExit;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getInit(); // initialize UI elements
    }

    public void getInit(){
        textViewLogin = (TextView) findViewById(R.id.textViewLogin);
        textViewPassword = (TextView) findViewById(R.id.textViewPassword);
        editTextLogin = (EditText)findViewById(R.id.editTextLogin);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
    }

    public void save(){
        sharedPreferences = getSharedPreferences("My_Login_Preferences", MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("Login", editTextLogin.getText().toString());
        sharedPreferencesEditor.putString("Password", editTextPassword.getText().toString());
        sharedPreferencesEditor.commit();
        Toast message = Toast.makeText(this,"Preferences Saved", 10);
        message.show();
    }

    public void exit(){
        finish();
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
