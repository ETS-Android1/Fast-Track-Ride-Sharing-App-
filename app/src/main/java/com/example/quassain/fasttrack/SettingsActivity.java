package com.example.quassain.fasttrack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private String getType;
    private CircleImageView profileimageview;
    private EditText username,usernumber,drivercarname,drivercarnumber;
    private ImageView closebtn,savebtn;
    private TextView profilechangebtn;
    private String checker="";
    private Uri imageUri;
    private String myUrl="";
    private StorageTask uploadtask;
    private StorageReference storageProfilePicReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getType = getIntent().getStringExtra("type");
        Toast.makeText(this, getType, Toast.LENGTH_SHORT).show();


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
        storageProfilePicReference = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        profileimageview = findViewById(R.id.profile_image);
        username = findViewById(R.id.name);
        usernumber= findViewById(R.id.phone);
        drivercarname = findViewById(R.id.driver_car_name);
        drivercarnumber = findViewById(R.id.driver_car_number);
        if(getType.equals("Drivers")){

           drivercarname.setVisibility(View.VISIBLE);
           drivercarnumber.setVisibility(View.VISIBLE);
        }

        closebtn = findViewById(R.id.close_btn);
        savebtn = findViewById(R.id.save_btn);
        profilechangebtn = findViewById(R.id.change_picture_btn);


        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getType.equals("Drivers")){

                    startActivity(new Intent(SettingsActivity.this,DriverMapActivity.class));
                }else{
                    startActivity(new Intent(SettingsActivity.this,CustomerMapActivity.class));
                }
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checker.equals("clicked")){

                    validatecontrollers();
                }else{
                    validateOnlyInformation();

                }

            }
        });

        profilechangebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker="clicked";
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });

        getUserInformation();

    }

    private void validateOnlyInformation() {


        if(TextUtils.isEmpty(username.getText().toString())){

            Toast.makeText(this, "Please Provide Your Name...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(usernumber.getText().toString())){
            Toast.makeText(this, "Please Provide Your Number...", Toast.LENGTH_SHORT).show();
        }else if((getType.equals("Drivers"))   &&  TextUtils.isEmpty(drivercarname.getText().toString())){
            Toast.makeText(this, "Please Provide Your CarName...", Toast.LENGTH_SHORT).show();
        }else if((getType.equals("Drivers"))   &&  TextUtils.isEmpty(drivercarnumber.getText().toString())){
            Toast.makeText(this, "Please Provide Your CarNumber...", Toast.LENGTH_SHORT).show();
        }else{


            HashMap<String ,Object> userMap = new HashMap<>();
            userMap.put("uid",firebaseAuth.getCurrentUser().getUid());
            userMap.put("name",username.getText().toString());
            userMap.put("phone",usernumber.getText().toString());

            if(getType.equals("Drivers")){
                userMap.put("car name",drivercarname.getText().toString());
                userMap.put("car number",drivercarnumber.getText().toString());
            }
            databaseReference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(userMap);

            if(getType.equals("Drivers")){
                startActivity(new Intent(SettingsActivity.this,DriverMapActivity.class));
            }else{
                startActivity(new Intent(SettingsActivity.this,CustomerMapActivity.class));
            }


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE &&  resultCode == RESULT_OK && data!=null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileimageview.setImageURI(imageUri);
        }
        else{

            if(getType.equals("Drivers")){
                startActivity(new Intent(SettingsActivity.this,DriverMapActivity.class));
            }else{
                startActivity(new Intent(SettingsActivity.this,CustomerMapActivity.class));
            }

            Toast.makeText(this, "Error Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private void validatecontrollers(){

        if(TextUtils.isEmpty(username.getText().toString())){

            Toast.makeText(this, "Please Provide Your Name...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(usernumber.getText().toString())){
            Toast.makeText(this, "Please Provide Your Number...", Toast.LENGTH_SHORT).show();
        }else if((getType.equals("Drivers"))   &&  TextUtils.isEmpty(drivercarname.getText().toString())){
            Toast.makeText(this, "Please Provide Your CarName...", Toast.LENGTH_SHORT).show();
        }else if((getType.equals("Drivers"))   &&  TextUtils.isEmpty(drivercarnumber.getText().toString())){
            Toast.makeText(this, "Please Provide Your CarNumber...", Toast.LENGTH_SHORT).show();
        }else if(checker.equals("clicked")){
            uploadProfilePicture();

        }
    }

    private void uploadProfilePicture() {


        final ProgressDialog progressDialog  =new ProgressDialog(this);
        progressDialog.setTitle("Settings Account Informationn");
        progressDialog.setMessage("Please wait,while we are settings your account information");
        progressDialog.show();

        if(imageUri!=null){
            final StorageReference fileref = storageProfilePicReference
                    .child(firebaseAuth.getCurrentUser().getUid() + ".jpg");
            uploadtask = fileref.putFile(imageUri);
            uploadtask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws
                        Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileref.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadurl = task.getResult();
                        myUrl = downloadurl.toString();
                        HashMap<String ,Object> userMap = new HashMap<>();
                        userMap.put("uid",firebaseAuth.getCurrentUser().getUid());
                        userMap.put("name",username.getText().toString());
                        userMap.put("phone",usernumber.getText().toString());
                        userMap.put("image",myUrl);

                        if(getType.equals("Drivers")){
                            userMap.put("car name",drivercarname.getText().toString());
                            userMap.put("car number",drivercarnumber.getText().toString());
                        }
                        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(userMap);

                        progressDialog.dismiss();
                        if(getType.equals("Drivers")){
                            startActivity(new Intent(SettingsActivity.this,DriverMapActivity.class));
                        }else{
                            startActivity(new Intent(SettingsActivity.this,CustomerMapActivity.class));
                        }




                    }
                }
            });
        }else{

            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void getUserInformation(){

        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() && snapshot.getChildrenCount() > 0){

                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();

                    username.setText(name);
                    usernumber.setText(phone);


                    if(getType.equals("Drivers")) {
                        String carname = snapshot.child("car name").getValue().toString();
                        String carnumber = snapshot.child("car number").getValue().toString();
                        drivercarname.setText(carname);
                        drivercarnumber.setText(carnumber);
                    }
                    if(snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileimageview);
                    }
                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}