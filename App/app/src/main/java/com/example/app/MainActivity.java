package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userName;
    private EditText userPass;
    private TextView tView;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button logInBtn;
        userName = findViewById(R.id.userName);
        userPass = findViewById(R.id.userPass);
        logInBtn = findViewById(R.id.logInBtn);

        logInBtn.setOnClickListener(this);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button SignUP = findViewById(R.id.sign_up_btn);
        SignUP.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, sign_up.class);
            startActivity(intent);
        });

    }
    protected String authToken;

    private void getData() {
        String username = userName.getText().toString().trim();
        String password = userPass.getText().toString().trim();

        // Initialize RequestQueue
        mRequestQueue = Volley.newRequestQueue(this);

        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/login/";


        System.out.println("Sending request to " + url);

        // Initialize StringRequest with headers
        mStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Response: " + response, Toast.LENGTH_LONG).show();
//                authToken = response;
//                System.out.println(authToken);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    authToken = (String) jsonObject.get("token");
                    SharedPreferences preferences = getSharedPreferences("Authentication", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", authToken);
                    editor.apply();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(authToken);

                // Handle response
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                System.out.println(error.toString());
                // Handle error
            }
        }) {
            // Add headers to the request
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("username", username);
                headers.put("password", password);
//                String credentials = username + ":" + password;
//                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                headers.put("Authorization", auth);
                return headers;
            }
        };

        // Add the request to the RequestQueue
        mRequestQueue.add(mStringRequest);
    }

    @SuppressLint("SetTextI18n")
    private boolean validate(){
        if(userName.getText().toString().equals("")){
            tView = findViewById(R.id.tView);
            tView.setVisibility(View.VISIBLE);
            tView.setText("Empty entry noticed");
            return false;
        }
        else return !userPass.getText().toString().equals("");
    }

    private void authenticateUser(String username, String password) {

        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/login/";

        System.out.println("Sending request to " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Parse the backend response to get the token
                        String token = parseResponse(response);

                        // Save the token to SharedPreferences
                        saveToken(token);

                        // Move to the homePage activity
                        Intent intent = new Intent(MainActivity.this, homePage.class);
                        intent.putExtra("AUTH_TOKEN", token);
                        intent.putExtra("UserName", username);
                        startActivity(intent);

                        // Hide the login form
                        tView = findViewById(R.id.tView);
                        tView.setVisibility(View.GONE);

                        // Display success message
                        Toast.makeText(MainActivity.this, "You've logged in successfully", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }) {
            // Override the getHeaders() method to add the username and password to the headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("username", username);
                headers.put("password", password);
                return headers;
            }
        };

        // Add the request to the request queue
        queue.add(request);
    }

    private String parseResponse(JSONObject response) {
        String token = null;
        try {
            token = response.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return token;
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.putString("authToken", token);
        editor.putString("userName", userName.getText().toString());
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.logInBtn) {
            try {
                if (validate()) {
                    System.out.println(userName.getText().toString());
                    System.out.println(userPass.getText());
                    authenticateUser(userName.getText().toString(), userPass.getText().toString());
//                    getData();
//                    Intent intent = new Intent(this, homePage.class);
//                    System.out.println("Auth token in main act " + authToken);
//                    intent.putExtra("AUTH_TOKEN",authToken);
//                    intent.putExtra("UserName",userName.getText().toString());
//                    startActivity(intent);
////                    getData();
//                    tView = findViewById(R.id.tView);
//                    tView.setVisibility(View.GONE);
//                    Toast.makeText(this, "You've Logged in successfully", Toast.LENGTH_SHORT).show();
//                    System.out.println("Hey");
                }
            } catch (Exception e) {
//                    System.out.println(e);
            }
        }
    }
}