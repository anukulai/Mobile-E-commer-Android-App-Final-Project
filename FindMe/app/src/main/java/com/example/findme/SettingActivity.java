package com.example.findme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingActivity extends AppCompatActivity {

    TextView name, email, phone, code;
    Button signout;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        name = (TextView) findViewById(R.id.nameSetting);
        email = findViewById(R.id.emailSetting);
        phone = findViewById(R.id.phoneSetting);
        code = findViewById(R.id.codeSetting);
        signout = findViewById(R.id.signoutSettings);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("name").getValue(String.class));
                code.setText(dataSnapshot.child("code").getValue(String.class));
                email.setText(dataSnapshot.child("email").getValue(String.class));
                phone.setText(dataSnapshot.child("phone").getValue(String.class));
                //      profile_pic.setImageURI(dataSnapshot.child("imageUrl").getValue(Uri.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user;
                user = auth.getCurrentUser();
                if (user != null) {
                    auth.signOut();
                }
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}