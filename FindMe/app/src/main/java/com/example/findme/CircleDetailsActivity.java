package com.example.findme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CircleDetailsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    TextView circlenameTV,nameTV,emailTV;
    private GoogleMap mMap;
    private Marker marker;
    FirebaseAuth auth;
    FirebaseUser user;
    GeoFire geoFire;
    SupportMapFragment mapFragment;
    public String[] uids;
    int size;
    Handler mHandler;

    GoogleApiClient client;
    LocationRequest locationRequest;
    Location lastLocation;
    LatLng latLng;

    boolean isFirstTime = true;

    DatabaseReference databaseReference,circleref,ref,databaseReference1,detailreference;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildGoogleApiClient();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        this.mHandler = new Handler();

        Intent intent = getIntent();
        String circle = intent.getStringExtra("circlename");

        circleref = databaseReference.child(user.getUid()).child(circle);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        circlenameTV = header.findViewById(R.id.circlenameTv);
        nameTV = header.findViewById(R.id.name_header);
        emailTV = header.findViewById(R.id.email_header);
        circlenameTV.setText(circle);

        detailreference = databaseReference.child(user.getUid());
        detailreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameTV.setText(dataSnapshot.child("name").getValue(String.class));
                code = (dataSnapshot.child("code").getValue(String.class));
                emailTV.setText(dataSnapshot.child("email").getValue(String.class));
                //    phone.setText(dataSnapshot.child("phone").getValue(String.class));
                //      profile_pic.setImageURI(dataSnapshot.child("imageUrl").getValue(Uri.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("location");
        geoFire = new GeoFire(databaseReference1);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.circle_details, menu);
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
            Intent i = new Intent(CircleDetailsActivity.this,SettingActivity.class);
            startActivity(i);
            //Toast.makeText(this,"Setting clicked", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            //   Toast.makeText(CircleDetailsActivity.this,"in runnable",Toast.LENGTH_SHORT).show();
            mMap.clear();
            for (int i=0;i<size;i++){
                ref = databaseReference.child(uids[i]);
                //    locationreference = ref.child("location").child("l");
                //  namereference = ref.child("name");
                //     Toast.makeText(CircleDetailsActivity.this,""+ref.child("0").getKey(),Toast.LENGTH_LONG).show();
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        LatLng location = new LatLng(dataSnapshot.child("location").child("l").child("0").getValue(Double.class),dataSnapshot.child("location").child("l").child("1").getValue(Double.class));
                        marker = mMap.addMarker(new MarkerOptions().position(location).title(dataSnapshot.child("name").getValue(String.class)));
                        //Toast.makeText(getApplicationContext(),""+dataSnapshot.child("0").toString()+"  ",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            displayLocation();
            CircleDetailsActivity.this.mHandler.postDelayed(m_Runnable,10000);
        }

    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        circleref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                size=0;
                uids = new String[100];
                for (DataSnapshot dss : dataSnapshot.getChildren()){
                    String u = dss.child("uid").getValue(String.class);
                    uids[size]=u;
                    size++;
                }
                //Toast.makeText(getApplicationContext(),""+size+" "+uids[0]+"    "+uids[1],Toast.LENGTH_LONG).show();


                //     Toast.makeText(CircleDetailsActivity.this,"hi  "+size+" ",Toast.LENGTH_LONG).show();
                m_Runnable.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                marker.setTitle(marker.getTitle());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14);
                mMap.animateCamera(cameraUpdate, 100, null);
                return true;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_members) {
            Intent intent = new Intent(CircleDetailsActivity.this,AddMembersActivity.class);
            intent.putExtra("circlename",circlenameTV.getText().toString());
            startActivity(intent);
        }

        if(id == R.id.shareCode){
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,"Hello! Add me on FindMe app.This is my code: "+code);
            startActivity(i.createChooser(i,"Share using: "));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);

        if (lastLocation != null){

            latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());

            if (isFirstTime) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                mMap.animateCamera(cameraUpdate, 100, null);
                isFirstTime = false;
            }

            geoFire.setLocation("", new GeoLocation(latLng.latitude, latLng.longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    //Add marker
                    mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
                    //rotateMarker(mCurrent,-360,mMap);
                }
            });

        }
        else {
            Log.d("ERROR", "Cannot get your location");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null){
            Toast.makeText(CircleDetailsActivity.this,"Could not get location",Toast.LENGTH_LONG).show();
        }
        else {

            lastLocation = location;
            displayLocation();
           /* latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f)); */
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        client.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}