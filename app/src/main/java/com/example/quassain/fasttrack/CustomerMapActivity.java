package com.example.quassain.fasttrack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quassain.fasttrack.databinding.ActivityDriverMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.example.quassain.fasttrack.databinding.ActivityCustomerMapBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private ActivityCustomerMapBinding binding;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private Button customerlogoutbtn,customersettingsbtn;
    private  Button calldriverbtn;
    private String customerID;
    private DatabaseReference customerdatabaseRef;
    private  DatabaseReference driveravailableref;
    private LatLng customerpickuplocation;
    private int radius=1;
    private boolean driverfound =false,requestType=false;
    private String driverID;
    private  DatabaseReference driverRef;
    private  DatabaseReference driverlocationRef;
    Marker DriverMarker,PickupMarker;
    private  ValueEventListener DriverLocationRefListner;
    GeoQuery geoQuery;

    private TextView txtname, txtphone, txtcarname, txtcarnumber;
    private CircleImageView profilepicture;
    private RelativeLayout relativeLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);


        customerlogoutbtn  = (Button) findViewById(R.id.customer_logout_button);
        customersettingsbtn = findViewById(R.id.customer_settings_button);
        calldriverbtn = (Button) findViewById(R.id.call_a_driver);
        txtname = findViewById(R.id.name_driver);
        txtphone = findViewById(R.id.driver_phone);
        txtcarname = findViewById(R.id.driver_car_name);
        txtcarnumber = findViewById(R.id.driver_car_number);
        relativeLayout = findViewById(R.id.relative1);
        profilepicture = findViewById(R.id.profile_image_driver);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        customerID  = firebaseAuth.getCurrentUser().getUid();
        customerdatabaseRef = firebaseDatabase.getReference().child("FAST CUSTOMERS REQUESTS");
        driveravailableref = firebaseDatabase.getReference().child("FAST DRIVERS AVAILABLE");
        driverlocationRef = firebaseDatabase.getReference().child("FAST DRIVERS WORKING");
        // binding = ActivityCustomerMapBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
       if(mapFragment!=null) {
           mapFragment.getMapAsync(this);
       }

       customersettingsbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent gotosettings = new Intent(CustomerMapActivity.this , SettingsActivity.class);
               gotosettings.putExtra("type","Customers");
               startActivity(gotosettings);
           }
       });


       customerlogoutbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               firebaseAuth.signOut();;
               customerlogout();
           }
       });

       calldriverbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if(requestType){

                   requestType = false;
                   geoQuery.removeAllListeners();
                   driverlocationRef.removeEventListener(DriverLocationRefListner);

                   if(driverfound!=false){

                       driverRef = firebaseDatabase.getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideID");
                       driverRef.removeValue();
                       driverID=null;


                   }
                   driverfound = false;
                   radius =1;
                   GeoFire geoFire = new GeoFire(customerdatabaseRef);
                   geoFire.removeLocation(customerID);
                   if(PickupMarker!=null){

                       PickupMarker.remove();
                   }
                   if(DriverMarker!=null){

                       DriverMarker.remove();
                   }
                   calldriverbtn.setText("Call a driver..");
                   relativeLayout.setVisibility(View.GONE);

               }
               else{

                   requestType = true;
                   GeoFire geoFire = new GeoFire(customerdatabaseRef);
                   geoFire.setLocation(customerID, new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()),new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {
                           if (error!=null)
                           {
                               Toast.makeText(CustomerMapActivity.this,"Can't go Active",Toast.LENGTH_SHORT).show();
                           }
                           Toast.makeText(CustomerMapActivity.this,"You are Active",Toast.LENGTH_SHORT).show();
                       }
                   });

                   customerpickuplocation = new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                   mMap.addMarker(new MarkerOptions().position(customerpickuplocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                   calldriverbtn.setText("Your University fellow will pickyouup soon...");
                   getclosestdriver();
               }

           }
       });


    }

    private void getclosestdriver() {

        GeoFire geoFire = new GeoFire(driveravailableref);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerpickuplocation.latitude,customerpickuplocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //this method will be called whenever driver is nearby the customer location.....
                if(!driverfound && requestType){
                    driverfound=true;
                    driverID = key;

                    driverRef = firebaseDatabase.getReference().child("Users").child("Drivers").child(driverID);
                    HashMap driveMap = new HashMap();
                    driveMap.put("CustomerRideID",customerID);
                    driverRef.updateChildren(driveMap);
                    
                    
                    gettingdriverlocation();
                    calldriverbtn.setText("Looking for Driver location..");


                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!driverfound){

                    radius = radius +1;
                    getclosestdriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void gettingdriverlocation() {

      DriverLocationRefListner =   driverlocationRef.child(driverID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && requestType){

                            List<Object> driverlocationMap = (List<Object>) snapshot.getValue();
                            double LocationLat=0;
                            double LocationLng=0;
                            calldriverbtn.setText("FAST Driver Found");

                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssigneddriverInformation();

                            if(driverlocationMap.get(0)!=null){
                                LocationLat = Double.parseDouble(driverlocationMap.get(0).toString());
                            }
                            if(driverlocationMap.get(1)!=null){
                                LocationLng = Double.parseDouble(driverlocationMap.get(1).toString());
                            }

                            LatLng DriverLatLng = new LatLng(LocationLat,LocationLng);
                            if(DriverMarker!=null){
                                DriverMarker.remove();
                            }

                            Location location1 = new Location("");
                            location1.setLatitude(customerpickuplocation.latitude);
                            location1.setLongitude(customerpickuplocation.longitude);



                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLng.latitude);
                            location2.setLongitude(DriverLatLng.longitude);

                            float Distance = location1.distanceTo(location2);
                            if(Distance <90){
                                calldriverbtn.setText("Driver is Arrived..");

                            }else{
                                calldriverbtn.setText("Driverfound:"+String.valueOf(Distance));
                            }



                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
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

        lastlocation=location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


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
    }


    private void customerlogout() {

        Intent welcomeintent = new Intent(CustomerMapActivity.this , WelcomeActivity.class);
        welcomeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeintent);
        finish();

    }

    private void getAssigneddriverInformation(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverID);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              if(snapshot.exists() && snapshot.getChildrenCount() > 0){
                  String name = snapshot.child("name").getValue().toString();
                  String phone = snapshot.child("phone").getValue().toString();
                  String carname = snapshot.child("car name").getValue().toString();
                  String carnumber = snapshot.child("car number").getValue().toString();

                  txtname.setText(name);
                  txtphone.setText(phone);
                  txtcarname.setText(carname);
                  txtcarnumber.setText(carnumber);

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