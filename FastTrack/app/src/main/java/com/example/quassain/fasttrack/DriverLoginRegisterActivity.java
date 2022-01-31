package com.example.quassain.fasttrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class DriverLoginRegisterActivity extends AppCompatActivity {


    private Button driverloginbutton;
    private Button driverregisterbutton;
    private TextView driverreglink;
    private TextView driverstatus;
    private EditText emaildriver;
    private EditText passworddriver;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingbar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        firebaseAuth = FirebaseAuth.getInstance();
        driverloginbutton = findViewById(R.id.driver_login_button);
        driverregisterbutton = findViewById(R.id.driver_register_button);
        driverreglink = findViewById(R.id.driver_link);
        driverstatus = findViewById(R.id.Driver_login_status);
        emaildriver = findViewById(R.id.driver_emailid);
        passworddriver = findViewById(R.id.driver_password);

        loadingbar = new ProgressDialog(this);

        driverregisterbutton.setVisibility(View.INVISIBLE);
        driverregisterbutton.setEnabled(false);

        driverreglink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverloginbutton.setVisibility(View.INVISIBLE);
                driverreglink.setVisibility(View.INVISIBLE);
                driverstatus.setText("Register Driver");

                driverregisterbutton.setVisibility(View.VISIBLE);
                driverregisterbutton.setEnabled(true);
            }
        });

        driverregisterbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emaildriver.getText().toString();
                String password = passworddriver.getText().toString();
                RegisterDriver(email,password);
            }
        });


        driverloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emaildriver.getText().toString();
                String password = passworddriver.getText().toString();
                LoginDriver(email,password);
            }
        });

    }

    private void LoginDriver(String email, String password) {


        if(TextUtils.isEmpty(email)){

            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else{

            loadingbar.setTitle("Driver Login");
            loadingbar.setMessage("Please wait while we are checking your credentials..");
            loadingbar.show();

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Logged in Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Intent driverIntent = new Intent(DriverLoginRegisterActivity.this,DriverMapActivity.class);
                                startActivity(driverIntent);    // Driver login leads to map


                            }else{

                                Toast.makeText(DriverLoginRegisterActivity.this, "Login Unsuccessfull... ", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }

    }

    private void RegisterDriver(String email, String password) {    // Register to the firebase database

        if(TextUtils.isEmpty(email)){

            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(DriverLoginRegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else{

            loadingbar.setTitle("Driver Registration");
            loadingbar.setMessage("Please wait while we register your data..");
            loadingbar.show();

            firebaseAuth.createUserWithEmailAndPassword(email,password)    // Send these email and pass to FB
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Registered Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();   // Loading bar stops
                                Intent driverIntent = new Intent(DriverLoginRegisterActivity.this,DriverMapActivity.class); // From login to Map
                                startActivity(driverIntent);
                            }else{

                                Toast.makeText(DriverLoginRegisterActivity.this, "Registration Unsuccessfull... ", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }


    }
}