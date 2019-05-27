package com.example.loginv3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "EmailPassword";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private EditText mEmailField;
    private EditText mPasswordField;
   // private Button mRegisterButton;
    private TextView mTextView;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buttons
        findViewById(R.id.registerButton).setOnClickListener(this);
        findViewById(R.id.loginButton).setOnClickListener(this);

        //Views
        mEmailField = findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);
       // mRegisterButton = findViewById(R.id.registerButton);
        mTextView = findViewById(R.id.textView);

        //FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void createAccount(String email, String password) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Account created sucessfully.",
                                    Toast.LENGTH_SHORT).show();
                            onAuthSuccess(task.getResult().getUser());
                            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());

                        } else {
                           // Toast.makeText(MainActivity.this, "Authentication failed.",
                           //         Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "createUserWithEmail:failure", task.getException());

                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "User Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            Intent myIntent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(myIntent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "User Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                        }

                        // [START_EXCLUDE]
                        //if (!task.isSuccessful()) {
                        //    mStatusTextView.setText(R.string.auth_failed);
                        //}
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }




    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }




    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.registerButton){
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());

        }
        if (i == R.id.loginButton){
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());

        }
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);

    }


}
