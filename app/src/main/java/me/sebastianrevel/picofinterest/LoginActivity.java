package me.sebastianrevel.picofinterest;

import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn;
    private Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.username_et);
        passwordInput = findViewById(R.id.password_et);
        loginBtn = findViewById(R.id.login_btn);
        signupBtn = findViewById(R.id.signup_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get references to username and password that user typed once user clicks on Log in button
                final String username = usernameInput.getText().toString();
                final String password = passwordInput.getText().toString();
                login(username, password);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // want to go to Sign Up activity when sign up is clicked
                final Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            //final Intent intent = new Intent(LoginActivity.this, TimelineActivity.class);
            final Intent intent = new Intent(LoginActivity.this, MainActivity.class); // this is to use fragments now
            startActivity(intent);
            finish();
        } else {
            // show the signup or login screen
            // which happens automatically
        }


    }

    private void login(String username, String password){
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) { // if no errors, so user was logged in correctly
                    Log.d("LoginActivity", "Login successful!");

                    // want to go to Home Activity with intent after successful log in
                    //final Intent intent = new Intent(LoginActivity.this, TimelineActivity.class);
                    final Intent intent = new Intent(LoginActivity.this, MainActivity.class); // this is to use fragments now
                    startActivity(intent);
                    finish(); //so that user can't just press back and log out
                }
                else { // if there is a ParseException
                    Log.e("LoginActivity", "Login failure.");
                    e.printStackTrace();
                }
            }
        });
    }
}