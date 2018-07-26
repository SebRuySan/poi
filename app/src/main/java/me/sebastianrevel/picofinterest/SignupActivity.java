package me.sebastianrevel.picofinterest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameInput = findViewById(R.id.username_et);
        passwordInput = findViewById(R.id.password_et);
        registerBtn = findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get references to username, password and email that user typed once user clicks on Sign up button
                final String username = usernameInput.getText().toString();
                final String password = passwordInput.getText().toString();
                signup(username, password);
            }
        });
    }

    // sign up in Parse using username, password, email, and handle
    private void signup(String username, String password){
        // Create the ParseUser
        final ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        user.put("UserScore", 0);
        //final File file = new File("/desktop/profilepicturedef.png"); // create a default profile pic
        //final ParseFile parseFile = new ParseFile(file);
        /*parseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                // If successful save image as profile picture
                if(null == e) {
                    user.put("profilepic", parseFile);
                    user.saveInBackground();
                    Log.d("mainactivity", "ProfilePic save requested");
                }
            }
        });*/
        // Invoke signUpInBackgroundput("pro
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now. (No error)
                    Log.d("SignupActivity", "Signup successful!");

                    // want to go to Home Activity with intent after successful log in
                    //final Intent intent = new Intent(SignupActivity.this, TimelineActivity.class);
                    final Intent intent = new Intent(SignupActivity.this, MainActivity.class); // this is to use fragments
                    startActivity(intent);
                    finish(); //so that user can't just press back and log out
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.e("SignupActivity", "Sign up failure.");
                    e.printStackTrace();
                }
            }
        });
    }
}