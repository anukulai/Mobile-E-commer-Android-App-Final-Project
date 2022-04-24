package com.example.findme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findme.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class registerUser extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextView banner, registerUser;
    private EditText editTextFullName, editTextPhoneNumber, editTextEmail, editTextPassword;
    private ProgressBar prbar;

    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);

        registerUser = (Button) findViewById(R.id.register);
        registerUser.setOnClickListener(this);


        editTextFullName = (EditText) findViewById(R.id.fullname);
        editTextPhoneNumber = (EditText) findViewById(R.id.phonenumber);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        prbar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.banner:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.register:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = (editTextEmail).getText().toString().trim();
        String password = (editTextPassword).getText().toString().trim();
        String fullname = (editTextFullName).getText().toString().trim();
        String phonenumber = (editTextPhoneNumber).getText().toString().trim();

        if (fullname.isEmpty()) {
            editTextFullName.setError("Full Name is Required");
            editTextFullName.requestFocus();
            return;
        }
        if (phonenumber.isEmpty()) {
            editTextPhoneNumber.setError("Phone Number is Required");
            editTextPhoneNumber.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError("Email is Required");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Valid Email is Required");
            editTextEmail.requestFocus();
            return;

        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password is Required");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimum 6 character password is Required");
            editTextPassword.requestFocus();
            return;
        }

        prbar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            Toast.makeText(registerUser.this,""+ currentUser.getUid(), Toast.LENGTH_LONG).show();
                            User user = new User();
                            user.setEmail(email);
                            user.setName(fullname);
                            user.setPhone(phonenumber);
                            user.setUserId(currentUser.getUid());
                            user.setCode(generatecode());

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(registerUser.this, "User has been Registered Successfully", Toast.LENGTH_LONG).show();
                                        prbar.setVisibility(View.GONE);
                                    } else {

                                        Toast.makeText(registerUser.this, "Failed to register user ! Try Again !", Toast.LENGTH_LONG).show();
                                        prbar.setVisibility(View.GONE);

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(registerUser.this, "Failed to register user ! Try Again !", Toast.LENGTH_LONG).show();
                            prbar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private String generatecode() {
        Random r = new Random();
        int n = 1000 + r.nextInt(9000);
        String code = String.valueOf(n);
        return code;
    }
}


