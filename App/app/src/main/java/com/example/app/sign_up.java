package com.example.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class sign_up extends AppCompatActivity {

    private static EditText userName;
    private static EditText userPass;
    private static EditText userPassR;

    private static Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userName = findViewById(R.id.userName);
        userPass = findViewById(R.id.userPass);
        userPassR = findViewById(R.id.userPassR);

        button = findViewById(R.id.sign_up_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userName.getText().toString().length() < 3) {
                    Toast.makeText(sign_up.this,  "Please add at least 3 letters to the username", Toast.LENGTH_SHORT);
                    reset();
                } else if (userPass.getText().toString().length() < 8) {
                    Toast.makeText(sign_up.this,  "Please add at least 8 characters to the password", Toast.LENGTH_SHORT);
                    reset();
                } else if (!userPass.getText().toString().equals(userPassR.getText().toString())) {
                    Toast.makeText(sign_up.this,  "The passwords do not match", Toast.LENGTH_SHORT);
                    reset();
                }
                signup();

                Intent intent = new Intent(sign_up.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void reset() {
        userPassR.setText("");
        userPass.setText("");
        userName.setText("");
    }

    private void signup() {
        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/signup/";

        System.out.println("Sending request to " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Toast.makeText(sign_up.this, "You've signed up successfully", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                Toast.makeText(sign_up.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = super.getParams();
                params.put("username", userName.getText().toString());
                params.put("password", userPass.getText().toString());
                params.put("cpassword", userPassR.getText().toString());
                params.put("fname", "John");
                params.put("lname", "Doe");
                params.put("email", "email@email.com");
                return params;
            }
        };

        // Add the request to the request queue
        queue.add(request);
    }
}