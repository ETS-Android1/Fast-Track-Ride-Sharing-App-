package com.example.quassain.fasttrack;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.quassain.fasttrack.databinding.ActivityDriverMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityDriverMapBinding binding;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference driveravailaibityRef;
    private Button logoutdriverbutton;
    private Button settingsdriverbutton;
    private DatabaseReference AssignedCustomerRef,AssignedCustomerPickupRef;
    private  ValueEventListener AssignedCustomerPickupRefListner;
    private TextView txtname, txtphone;
    private CircleImageView profilepicture;
    private RelativeLayout relativeLayout;


    Marker PickupMarker;
    private String driverID,customerID="";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private  Boolean currentLogoutDriverstatus = false;

    @SuppressLint("InflateParams")

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        logoutdriverbutton = findViewById(R.id.driver_logout_button);
        settingsdriverbutton = findViewById(R.id.driver_settings_button);
        txtname = findViewById(R.id.name_customer);
        txtphone = findViewById(R.id.customer_phone);
        relativeLayout = findViewById(R.id.relative2);
        profilepicture = findViewById(R.id.profile_image_customer);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        driverID = firebaseAuth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();

           // binding = ActivityDriverMapBinding.inflate(getLayoutInflater());
            //setContentView(binding.getRoot());



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        if(mapFragment!=null) {
            mapFragment.getMapAsync(this);
        }


        settingsdriverbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotosettings = new Intent(DriverMapActivity.this , SettingsActivity.class);
                gotosettings.putExtra("type","Drivers");
                startActivity(gotosettings);
            }
        });

        logoutdriverbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentLogoutDriverstatus = true;
                Disconnectthedriver();
                firebaseAuth.signOut();
                logoutDriver();
            }
        });


        getAssignedCustomerRequest();
    }

    private void getAssignedCustomerRequest() {

        AssignedCustomerRef = firebaseDatabase.getReference().child("Users")
                .child("Drivers").child(driverID).child("CustomerRideID");
        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    customerID  = snapshot.getValue().toString();

                    getAssignedCustomerpickuplocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedCustomerInformation();
                }
                else{

                    customerID = "";
                    if(PickupMarker!=null){
                        PickupMarker.remove();
                    }
                    if(AssignedCustomerPickupRefListner!=null){
                        AssignedCustomerPickupRef.removeEventListener(AssignedCustomerPickupRefListner);
                    }
                    relativeLayout.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAssignedCustomerpickuplocation() {

        AssignedCustomerPickupRef = firebaseDatabase.getReference().child("Customer Requests").
                child(customerID).child("l");

      AssignedCustomerPickupRefListner =  AssignedCustomerPickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Object> customerLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat=0;
                    double LocationLng=0;


                    if(customerLocationMap.get(0)!=null){
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if(customerLocationMap.get(1)!=null){
                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    LatLng DriverLatLng = new LatLng(LocationLat,LocationLng);
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Customer Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        if(getApplicationContext()!=null){

            lastlocation=location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driveravailaibityRef = firebaseDatabase.getReference().child("FAST DRIVERS AVAILABLE");
            GeoFire geoFireAvailability = new GeoFire(driveravailaibityRef);
            DatabaseReference driverworkingRef = firebaseDatabase.getReference().child("FAST Drivers Working");
            GeoFire geoFireWorking = new GeoFire(driverworkingRef);

            switch (customerID){

                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()),new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error!=null)
                            {
                                Toast.makeText(DriverMapActivity.this,"Can't go Active",Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(DriverMapActivity.this,"You are Active",Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()),new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error!=null)
                            {
                                Toast.makeText(DriverMapActivity.this,"Can't go Active",Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(DriverMapActivity.this,"You are Active",Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;


            }


        }

    }
    protected  synchronized  void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!currentLogoutDriverstatus){
            Disconnectthedriver();
        }

    }

    private void Disconnectthedriver() {

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        driveravailaibityRef = firebaseDatabase.getReference().child("FAST DRIVERS AVAILABLE");
        GeoFire geoFire = new GeoFire(driveravailaibityRef);
        geoFire.removeLocation(userID);
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }


    private void logoutDriver() {

        Intent welcomeintent = new Intent(DriverMapActivity.this , WelcomeActivity.class);
        welcomeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeintent);
        finish();
    }
    private void getAssignedCustomerInformation(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerID);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount() > 0){
                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();

                    txtname.setText(name);
                    txtphone.setText(phone);

                    if(snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilepicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}