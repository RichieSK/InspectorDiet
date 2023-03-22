package com.example.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodChoose extends AppCompatActivity {

    private String authToken;

    private String foods;

    private LinearLayout chooseLinearLayout;
    private Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_choose);

        Intent intent = getIntent();
        ArrayList<String> excludedFoods = (ArrayList<String>) intent.getSerializableExtra("foods");

        chooseLinearLayout = findViewById(R.id.choose_linear_layout);
        confirmBtn = findViewById(R.id.submit_changes);
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        authToken = preferences.getString("authToken", null);
        System.out.println("Token in FoodPic " + authToken);
        getDataFromAPI(excludedFoods);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> checkedValues = new ArrayList<>();
                for (int i = 0; i < chooseLinearLayout.getChildCount(); i++) {
                    View v = chooseLinearLayout.getChildAt(i);
                    if (v instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) v;
                        if (checkBox.isChecked()) {
                            checkedValues.add(checkBox.getText().toString());
                        }
                    }
                }

                Intent intent1 = new Intent(FoodChoose.this, FoodPic.class);
                intent1.putExtra("foods", checkedValues);
                intent1.putExtra("image_viewed", true);
                startActivity(intent1);
//                sendData(checkedValues);
            }
        });
    }

    private void getDataFromAPI(ArrayList<String> excludedFoods) {
        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) + "/getfoods/";

        System.out.println("excluded foods in foodchoose contains " + excludedFoods);

        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        // Add Authorization header to the request
        Map<String, String> headers = new HashMap<>();
        headers.put("token", authToken);

        StringRequest mStringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Toast.makeText(getApplicationContext(), "Response :" + response, Toast.LENGTH_LONG).show();//display the response on screen
            foods = response;

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(foods).getAsJsonObject();

// Retrieve the "classes" value
            JsonArray foodsJsonArray = jsonObject.getAsJsonArray("foods");
            List<String> foodList = new ArrayList<>();
            for (JsonElement foodElement : foodsJsonArray) {
                foodList.add(foodElement.getAsString());
            }

            // Populate checkboxes
            for (String food : foodList) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(food);
                if (excludedFoods.contains(checkBox.getText().toString())) {
                    System.out.println(checkBox.getText().toString() + " present in the array");
                    checkBox.setChecked(true);
                }
                chooseLinearLayout.addView(checkBox);
            }

            System.out.println("Food response from API : " + foods);
        }, error -> Log.i("url", "Error :" + error.toString())) {
            // Override getHeaders() to include Authorization header
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

        };
        mRequestQueue.add(mStringRequest);
    }

    private void sendData(List<String> selectedFoods) {
        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/confirmentry/";

        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray(selectedFoods);
            jsonObject.put("foods", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add Authorization header to the request
        Map<String, String> headers = new HashMap<>();
        headers.put("token", authToken);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(getApplicationContext(), "Updated food", Toast.LENGTH_LONG).show();//display the response on screen
                },
                error -> {
                    Log.i("url", "Error :" + error.toString());
                })
        {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonObject.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers =  super.getHeaders();
                if (headers == null || headers.isEmpty()) {
                    headers = new HashMap<>();
                }
                headers.put("Token", authToken);
                return headers;
            }
        };


        mRequestQueue.add(stringRequest);


    }

}