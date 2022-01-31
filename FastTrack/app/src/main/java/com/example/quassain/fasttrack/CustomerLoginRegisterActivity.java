package com.example.quassain.fasttrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button Customerloginbutton;     // Variables to control the stuff in views
    private Button Customerregisterbutton;
    private TextView customerreglink;
    private TextView customerstatus;
    private EditText emailcustomer;
    private EditText passwordcustomer;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingbar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);
        firebaseAuth = FirebaseAuth.getInstance();  // Assign instance to firebase
        Customerloginbutton = findViewById(R.id.customer_login_btn);
        Customerregisterbutton = findViewById(R.id.customer_register_btn);
        customerreglink = findViewById(R.id.customer_link);
        customerstatus = findViewById(R.id.customer_login_status);
        emailcustomer = findViewById(R.id.customer_emailid);
        passwordcustomer = findViewById(R.id.customer_password);

        loadingbar = new ProgressDialog(this);
        Customerregisterbutton.setVisibility(View.INVISIBLE);
        Customerregisterbutton.setEnabled(false);

        customerreglink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Customerloginbutton.setVisibility(View.INVISIBLE);
                customerreglink.setVisibility(View.INVISIBLE);
                customerstatus.setText("Register Customer");

                Customerregisterbutton.setVisibility(View.VISIBLE);
                Customerregisterbutton.setEnabled(true);


            }
        });


        Customerregisterbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailcustomer.getText().toString();
                String password = passwordcustomer.getText().toString();
                RegisterCustomer(email,password);   // reg customer email
            }
        });


        Customerloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailcustomer.getText().toString();
                String password = passwordcustomer.getText().toString();
                LoginCustomer(email,password);
            }
        });








    }

    private void LoginCustomer(String email, String password) {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else{

            loadingbar.setTitle("Customer Login");
            loadingbar.setMessage("Please wait while we checking your credentials..");
            loadingbar.show();

            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Logged in Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }else{

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Login Unsuccessfull... ", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }



    }

    private void RegisterCustomer(String email, String password) {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(CustomerLoginRegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else{

            loadingbar.setTitle("Customer Registration");
            loadingbar.setMessage("Please wait while we register your data..");
            loadingbar.show();

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Registered Successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }else{

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Registration Unsuccessfull... ", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }


    }
}