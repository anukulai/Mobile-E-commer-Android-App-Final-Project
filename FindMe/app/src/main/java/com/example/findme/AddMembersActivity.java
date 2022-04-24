package com.example.findme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.findme.Model.Name;
import com.example.findme.Model.Uid;
import com.example.findme.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddMembersActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference,circleref,ref;
    String circlename,join_user_id,join_user_name;
    User u;
    Uid uid;
    RecyclerView recyclerView;
    MembersAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Name> namelist;
    String n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        recyclerView = (RecyclerView) findViewById(R.id.recycle_members);
        namelist = new ArrayList<>();

        Intent intent = getIntent();
        circlename = intent.getStringExtra("circlename");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ref = databaseReference.child(user.getUid()).child(circlename);



        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        loadRecyclerViewData();

        Button addMembersBtn = (Button) findViewById(R.id.add_members_btn);
        addMembersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMemberDialog();
            }
        });

    }

    private void loadRecyclerViewData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                namelist.clear();
                if (dataSnapshot.exists()){
                    Name name;

                    for (DataSnapshot dss: dataSnapshot.getChildren()){
                        n = dss.child("name").getValue(String.class);
                        //      n = dss.child("uid").getValue(String.class);
                        //Toast.makeText(AddMembersActivity.this," "+n,Toast.LENGTH_SHORT).show();
                        name = new Name(n);
                        namelist.add(name);
                        //       Toast.makeText(getApplicationContext(), " " +p , Toast.LENGTH_SHORT).show();
                        //            adapter.notifyDataSetChanged();
                    }
                    adapter = new MembersAdapter(namelist,AddMembersActivity.this,circlename);
                    recyclerView.setAdapter(adapter);
                    //       adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "failed " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void addMemberDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AddMembersActivity.this);
        dialog.setTitle("Enter code to add him/her");

        LayoutInflater inflater = LayoutInflater.from(AddMembersActivity.this);
        View add_member_layout = inflater.inflate(R.layout.layout_add_members,null);

        final EditText codeET = add_member_layout.findViewById(R.id.codeET);

        dialog.setView(add_member_layout);


        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                //final android.app.AlertDialog waitingDialog = new SpotsDialog(AddMembersActivity.this);
                //waitingDialog.show();
                databaseReference.orderByChild("code").equalTo(codeET.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            u = new User();
                            for (DataSnapshot dss : dataSnapshot.getChildren()){
                                u = dss.getValue(User.class);
                                join_user_id = u.userId;
                                join_user_name = u.name;
                                //    Toast.makeText(AddMembersActivity.this," "+join_user_name,Toast.LENGTH_SHORT).show();
                                uid = new Uid(join_user_id,join_user_name);

                                circleref = databaseReference.child(user.getUid()).child(circlename);
                                circleref.push().setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //waitingDialog.dismiss();
                                            Toast.makeText(AddMembersActivity.this,"Added successfully ",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //aitingDialog.dismiss();
                                        Toast.makeText(AddMembersActivity.this,"Failer: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        }
                        else{
                            //waitingDialog.dismiss();
                            Toast.makeText(AddMembersActivity.this,"Not found!",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //waitingDialog.dismiss();
                        Toast.makeText(AddMembersActivity.this,"Cancel: " + databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}