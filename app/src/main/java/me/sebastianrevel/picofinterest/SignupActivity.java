package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Collections;

public class SignupActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText passwordConfirmInput;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final Context context = this;

        usernameInput = findViewById(R.id.username_et);
        passwordInput = findViewById(R.id.password_et);
        passwordConfirmInput = findViewById(R.id.passwordConfirm_et);
        registerBtn = findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get references to username, password and email that user typed once user clicks on Sign up button
                final String username = usernameInput.getText().toString();
                final String password = passwordInput.getText().toString();
                final String confirmPassword = passwordConfirmInput.getText().toString();

                if ((!username.equals("")) && !(password.equals("")) && password.equals(confirmPassword)) {
                    signup(username, password);
                } else {
                    if (username.equals("")) {
                        Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show();
                    } else if (password.equals("")) {
                        Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    }

                    passwordInput.setText("");
                    passwordConfirmInput.setText("");
                }
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
        user.put("userScore", 0);
        user.put("followers", Collections.emptyList());

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