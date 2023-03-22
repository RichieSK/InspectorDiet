package com.example.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class homePage extends AppCompatActivity {



    private String authToken;

    private static String foodResponse;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Intent intent = getIntent();
//        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//        authToken = preferences.getString("token", "");
        authToken = intent.getStringExtra("AUTH_TOKEN");
        String user = intent.getStringExtra("UserName");

        if (Objects.equals(authToken, "") || authToken == null) {
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            authToken = preferences.getString("authToken", null);
            user = preferences.getString("userName", null);
        }

        System.out.println("Token in homepage " + authToken);
        // add this to the other pages, also add parent in manifest as well
//        Objects.requireNonNull(getSupportActionBar()).setTitle("Home Page");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fabCamera = findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(view -> {
            Intent intent1 = new Intent(homePage.this, FoodPic.class);
            startActivity(intent1);
//            finish();
        });

        FloatingActionButton logout = findViewById(R.id.log_out_btn);
        logout.setOnClickListener(view ->{
            Intent intent3 = new Intent(homePage.this, MainActivity.class);
            startActivity(intent3);
            finish();
        });

        TextView name = findViewById(R.id.HomeUserName);
        name.setText(getString(R.string.Salutation)+user);
        getDataFromAPI();
        System.out.println("Food Response " + foodResponse);
        if (foodResponse != null) {
            System.out.println(foodResponse);
        }


    }



    @Override
    protected void onResume() {
        super.onResume();
//        populateTable();
        // getDataFromAPI();
    }

    private void populateTable() {
        String[] dataArray = {"hello", "burger", "fries"};
        TableLayout tableLayout = findViewById(R.id.table_layout);

//        Get a reference to the TableRow template in your activity
//        TableRow tableRowTemplate = findViewById(R.id.table_row_template);

// Loop through the array of data and add rows to the table
        for (int i = 0; i < dataArray.length; i++) {

            // Inflate a new TableRow from the template
            TableRow tableRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_template, null);

            // Populate the TableRow with data from the array
            TextView col1TextView = tableRow.findViewById(R.id.col1);
            System.out.println(dataArray[i]);
            col1TextView.setText(dataArray[i]);
            TextView col2TextView = tableRow.findViewById(R.id.col2);
            col2TextView.setText(dataArray[i]);

            // Add the TableRow to the TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private void populateTable(List<List<String>> foods, List<String> calories) {
        System.out.println("got populateTable request" + foods);
        TableLayout tableLayout = findViewById(R.id.table_layout);

//        for (List<String> foodList : foods) {
        for (int i = 0; i < foods.size(); i++) {
            List<String> foodList = foods.get(i);
            String calorie = calories.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            View rowView = inflater.inflate(R.layout.table_row_template, tableLayout, false);

            TextView foodNameTextView = rowView.findViewById(R.id.col1);
            TextView caloriesTextView = rowView.findViewById(R.id.col2);


            if (foodList.size() == 1) {
                foodNameTextView.setText(foodList.get(0));

            } else {
                String foodItems = TextUtils.join(", ", foodList);
                foodNameTextView.setText(foodItems);
            }
            caloriesTextView.setText(calorie);

            tableLayout.addView(rowView);
        }
    }

    private void getDataFromAPI() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/gethistory/";

        // Add Authorization header to the request
        Map<String, String> headers = new HashMap<>();
        headers.put("token", authToken);

        StringRequest mStringRequest = new StringRequest(Request.Method.GET, url, response -> {
//            Toast.makeText(getApplicationContext(), "Response :" + response, Toast.LENGTH_LONG).show();//display the response on screen
            foodResponse = response;

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = jsonParser.parse(foodResponse).getAsJsonObject();

// Retrieve the "classes" value
            JsonArray foodsJsonArray = jsonObject.getAsJsonObject("data").getAsJsonArray("foods");
            List<List<String>> foodsList = new ArrayList<>();
            for (JsonElement foodJsonElement : foodsJsonArray) {
                JsonArray foodJsonArray = foodJsonElement.getAsJsonArray();
                List<String> foodList = new ArrayList<>();
                for (JsonElement foodElement : foodJsonArray) {
                    foodList.add(foodElement.getAsString());
                }
                foodsList.add(foodList);
            }

            ArrayList<String> calories = new ArrayList<>();

            JsonArray nutritionJsonArray = jsonObject.getAsJsonObject("data").getAsJsonArray("nutrition");
            for (JsonElement nutElem : nutritionJsonArray) {
                JsonObject nutObj = nutElem.getAsJsonObject();
                JsonElement cal = nutObj.get("calories");
                calories.add(cal.getAsString());
            }
//            JsonArray classes = jsonObject.getAsJsonArray("classes");
            populateTable(foodsList, calories);

            System.out.println("Food response from API : " + foodResponse);
        }, error -> Log.i("url", "Error :" + error.toString())) {
            // Override getHeaders() to include Authorization header
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
        mRequestQueue.add(mStringRequest);
    }
}