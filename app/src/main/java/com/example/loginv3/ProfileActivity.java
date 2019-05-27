package com.example.loginv3;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";
    private TextView mTextView;
    private EditText usernameField;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.updateButton).setOnClickListener(this);
        findViewById(R.id.logoutButton).setOnClickListener(this);

        mTextView = findViewById(R.id.welcomeText);
        usernameField = findViewById(R.id.usernameField);


        mAuth = FirebaseAuth.getInstance();

        mTextView.setText("Seja bem vindo de volta " + mAuth.getCurrentUser().getEmail());


        mDatabase = FirebaseDatabase.getInstance().getReference();

        updateDisplayName();


    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.updateButton){
            updateUserData(mAuth.getCurrentUser().getUid(), usernameField.getText().toString());

        }
        if (i == R.id.logoutButton){
            mDatabase.child("message").setValue("desloguei");
            signOut();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    private void updateUserData(String userId, String name) {
        mDatabase.child("users").child(userId).child("username").setValue(name);
        updateDisplayName();
    }

    private void updateDisplayName(){
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User temp = dataSnapshot.getValue(User.class);
                        String name = temp.username; // "John Doe"
                        mTextView.setText("Seja bem vindo de volta Sr(a)" + name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }
}


