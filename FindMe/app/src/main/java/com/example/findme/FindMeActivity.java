package com.example.findme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findme.Model.CircleName;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;



public class FindMeActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

//    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
//    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference databaseReference, ref, userRef, databaseReference1;
    FirebaseUser user;
    GeoFire geoFire;
    GoogleApiClient client;
    LocationRequest locationRequest;
    LatLng latLng;
    Location lastLocation;

    //PermissionManager permissionManager;

    CircleName circleName;
    RecyclerView recyclerView;
    CircleNamesAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<CircleName> namelist;
    String cn;
    Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_me);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Circlename");

        buildGoogleApiClient();

        databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("location");
        geoFire = new GeoFire(databaseReference1);

        recyclerView = (RecyclerView) findViewById(R.id.recycle_circlename);
        namelist = new ArrayList<>();

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        loadRecyclerViewData();

        addBtn = (Button) findViewById(R.id.addBtn);

        addBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        AddingCircle();
    }

    private void AddingCircle() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(FindMeActivity.this);
        dialog.setTitle("Name a Circle");

        LayoutInflater inflater = LayoutInflater.from(FindMeActivity.this);
        final View add_circle_layout = inflater.inflate(R.layout.layout_add_circle, null);

        final EditText circlename = add_circle_layout.findViewById(R.id.circleNameEt);

        dialog.setView(add_circle_layout);

        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                databaseReference.child("Circlename").orderByChild("cname").equalTo(circlename.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {

                            circleName = new CircleName();
                            circleName.setCname(circlename.getText().toString());
                            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Circlename");


                            userRef.push().setValue(circleName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    Toast.makeText(getApplicationContext(), "" + circlename.getText().toString() + " circle added", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(getApplicationContext(), "failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {

                            Toast.makeText(getApplicationContext(), "" + circlename.getText().toString() + " is already added", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(getApplicationContext(), databaseError.getMessage().toString(), Toast.LENGTH_LONG);
                    }
                });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        dialog.show();

    }

    private void loadRecyclerViewData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                namelist.clear();
                if (dataSnapshot.exists()){
                    CircleName circleName1;

                    for (DataSnapshot dss: dataSnapshot.getChildren()){
                        cn = dss.child("cname").getValue(String.class);

                        circleName1 = new CircleName(cn);
                        namelist.add(circleName1);
                        //            adapter.notifyDataSetChanged();
                    }
                    adapter = new CircleNamesAdapter(namelist,FindMeActivity.this);
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

    private void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest().create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //   locationRequest.setInterval(5000);
        //   locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);

        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        client.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);

        if (lastLocation != null){

            latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());

            geoFire.setLocation("", new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    //Add marker


                    //      mMap.addMarker(new MarkerOptions().position(latLng).title("You"));

                    //     rotateMarker(mCurrent,-360,mMap);

                }
            });

        }
        else {
            Log.d("ERROR", "Cannot get your location");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location == null){
            Toast.makeText(FindMeActivity.this,"Could not get location",Toast.LENGTH_LONG).show();
        }
        else {

            lastLocation = location;
            displayLocation();
           /* latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f)); */
        }
    }
}