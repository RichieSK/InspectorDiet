package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FoodPic extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 1;


    private ImageView mImageView;
    private int requestCode;
    private int resultCode;
    private Intent data;

    LinearLayout checkboxLayout;
    private Button submitButton;
    private Button confirmButton;
    private Button changeButton;
//    private FrameLayout tablePlaceholder;

    private static String authToken;

    public static final String API_REQUEST_FINISHED = "com.yourapp.api_request_finished";

    private BroadcastReceiver receiver;

//    private LocalBroadcastManager


    static class NetworkTask extends AsyncTask<Void, Void, String> {
        private OkHttpClient client;
        private Request request;
        private Context context;


        NetworkTask(OkHttpClient client, Request request, Context context) {
            this.client = client;
            this.request = request;
            this.context=context;

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                return responseBody;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseBody) {
            if (responseBody != null) {
                Log.d("FoodPic", "Response: " + responseBody);

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(responseBody).getAsJsonObject();
                JsonArray foodJsonArray = jsonObject.getAsJsonArray("classes");

                ArrayList<String> classes = new ArrayList<>();
                for (JsonElement foodElement : foodJsonArray) {
                    classes.add(foodElement.getAsString());
                }

                System.out.println("food predicted classes" + classes);

                NetworkTask.triggerBroadcast(context, classes);

                System.out.println("Called broadcast");



            }
        }
        public static void triggerBroadcast(Context context, ArrayList<String> classes) {
//            Intent intent = new Intent("my_custom_action");
            Intent intent = new Intent("com.example.yourapp.ACTION_NAME");
            intent.putStringArrayListExtra("foods", classes);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            System.out.println("Sending broadcast from trigger");
            context.sendBroadcast(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodpic);

        submitButton = findViewById(R.id.Submit_btn);
        changeButton = findViewById(R.id.change_btn);
        confirmButton = findViewById(R.id.confirm_button1);
        checkboxLayout = findViewById(R.id.checkbox_linear_layout);

        submitButton.setVisibility(View.GONE);
        changeButton.setVisibility(View.GONE);
        checkboxLayout.setVisibility(View.GONE);

        Intent intent = getIntent();
        intentCheck(intent);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Update your UI here
                // Find the table placeholder view by its ID and make it visible
                System.out.println("Received broadcast");
//                View tablePlaceholder = findViewById(R.id.table_placeholder);
//                tablePlaceholder.setVisibility(View.VISIBLE);

                ArrayList<String> classes = intent.getStringArrayListExtra("foods");
                // Find the checkbox list view by its ID and populate it with the classes
                populateCheckBoxes(classes);
                System.out.println("Done with making things visible");
//
            }
        };

        IntentFilter filter = new IntentFilter("com.example.yourapp.ACTION_NAME");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        authToken = preferences.getString("authToken", null);
        System.out.println("Token in FoodPic " + authToken);

        mImageView = findViewById(R.id.image_view);
        Button takePictureButton = findViewById(R.id.button_take_picture);
        takePictureButton.setOnClickListener(v -> dispatchTakePictureIntent());

        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> selectedFoods = getSelectedFoods();
                Intent intent1 = new Intent(FoodPic.this, FoodChoose.class);
                intent1.putExtra("foods", selectedFoods);
                startActivity(intent1);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Confirm Button clicked");
                ArrayList<String> selectedFoods = getSelectedFoods();
                confirmPostAPI(selectedFoods);
                Intent intent2 = new Intent(FoodPic.this, homePage.class);
                startActivity(intent2);
//                finish();
            }
        });

        Button submit = findViewById(R.id.Submit_btn);
        submit.setOnClickListener(v -> submitFoodImage());
    }

    private void populateCheckBoxes(ArrayList<String> classes) {
        LinearLayout linearLayout = findViewById(R.id.checkbox_linear_layout);
        Button changeButton = findViewById(R.id.change_btn);

        linearLayout.removeAllViews();

        int checkboxStyle = R.style.MyCheckboxStyle;

        for (String food : classes) {
            CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this, checkboxStyle));
            checkBox.setText(food.toLowerCase());
            checkBox.setTextColor(getResources().getColor(android.R.color.white));
            ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.white));
            checkBox.setButtonTintList(colorStateList);
            checkBox.setChecked(true);
            linearLayout.addView(checkBox);
        }

        System.out.println("Populated the checkboxes");

//        System.out.println("Classes in handling broadcast " + classes);
        linearLayout.setVisibility(View.VISIBLE);
        changeButton.setVisibility(View.VISIBLE);
    }

    private ArrayList<String> getSelectedFoods() {
        int childCount = checkboxLayout.getChildCount();
        System.out.println(childCount + "Children for checkbox layour");
        ArrayList<String> selectedFoods = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View child = checkboxLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                System.out.println("Child is checkbox");
                if (checkBox.isChecked()) {
                    System.out.println("child has been added to selectedFoods");
                    selectedFoods.add(checkBox.getText().toString());
                } else {
                    System.out.println("child is not activated");
                }
            }
        }
        return selectedFoods;
    }

    private void intentCheck(Intent intent) {
        ArrayList<String> excludedFoods = (ArrayList<String>) intent.getSerializableExtra("foods");
        boolean imageViewed = intent.getBooleanExtra("image_viewed", false);
        if (excludedFoods == null || excludedFoods.isEmpty() || !imageViewed) {
            return;
        }
        File file = new File(getExternalFilesDir(null), "my_image.jpg");
        if (file.exists()) {
            System.out.println("excluded foods " + excludedFoods);
            mImageView = findViewById(R.id.image_view);
            mImageView.setImageURI(Uri.fromFile(file));
            populateCheckBoxes(excludedFoods);
        } else {
            System.out.println("File does not exist in intent check");
        }

    }

    private void confirmPostAPI(ArrayList<String> selectedFoods) {
        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/confirmentry/";
        System.out.println("Calling post request");

        if (selectedFoods.isEmpty()) {
            Toast.makeText(this, "No items selected.", Toast.LENGTH_SHORT);
            System.out.println("No items selected");
            return;
        }

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

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url,
                response -> {
                    Toast.makeText(getApplicationContext(), "Updated food", Toast.LENGTH_LONG).show();//display the response on screen
                    //TODO
                    JsonParser jsonParser = new JsonParser();
                    JsonObject res = jsonParser.parse(response).getAsJsonObject();
                    JsonObject nutrition = res.getAsJsonObject("nutrition");
                    Set<String> keys = nutrition.keySet();
                    HashMap<String, String> nutritionVals = new HashMap<>();
                    for (String key : keys) {
                        String value = String.valueOf(nutrition.get(key));
                        Log.d("JSON", "Key: " + key + ", Value: " + value);
                        nutritionVals.put(key, value);
                    }


                    Intent intent1 = new Intent(FoodPic.this, DisplayNutrition.class);
                    intent1.putExtra("nutrition", nutritionVals);
                    intent1.putExtra("foods", selectedFoods);
                    startActivity(intent1);
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
        System.out.println("Added to queue");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void sendPostAPIRequest(String url) {
        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

// Create multipart request body with file and other data
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
//        builder.addFormDataPart("param_name", "param_value");
        File file = new File(getExternalFilesDir(null), "my_image.jpg");
        builder.addFormDataPart("sentFile", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file));

// Create RequestBody instance from multipart request body
        RequestBody requestBody = builder.build();

// Create Request instance with URL and method
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("token", authToken)
                .build();

        new NetworkTask(client, request, this).execute();


    }

    public void submitFoodImage(){
        String url = "http://" + getResources().getString(R.string.localhost) + ":"+ getResources().getString(R.string.port) +"/myimage/";
        sendPostAPIRequest(url);
        submitButton.setVisibility(View.GONE);
//        finish();
    }

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    byte[] byteArray = outputStream.toByteArray();
                    storeImage(byteArray);
                    mImageView.setImageBitmap(imageBitmap);
                    submitButton.setVisibility(View.VISIBLE);
                }
            }
    );

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, ask for it
            System.out.println("asking permission");
            Toast.makeText(this, "Asking Permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_IMAGE_CAPTURE);
        } else {
            // Permission is granted, launch the camera
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            System.out.println("Inside else");
            takePictureLauncher.launch(takePictureIntent);
            //need to work in this condition
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                System.out.println("Opening camera");
//                Toast.makeText(this, "Opening camera", Toast.LENGTH_SHORT).show();
//                takePictureLauncher.launch(takePictureIntent);
//            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch the camera
                dispatchTakePictureIntent();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivity Result called");

//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Uri selectedImageUri = data.getData();
//
//            mImageView.setImageURI(selectedImageUri);
//        }
//        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
//        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            System.out.println("Inside onActivity Result if");
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                // Save the byte array as a JPEG file or use it as needed
                storeImage(byteArray);
                mImageView.setImageURI(selectedImageUri);
                submitButton.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            System.out.println("Inside onActivity Result else if");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            storeImage(byteArray);
            mImageView.setImageBitmap(imageBitmap);
            submitButton.setVisibility(View.VISIBLE);
        }



    }

    private void storeImage(byte[] byteArray) {
        FileOutputStream outputStream = null;
        try {
            // Create a new file in the device storage
            System.out.println("Trying to create a file to store the image");
            File file = new File(getExternalFilesDir(null), "my_image.jpg");
            outputStream = new FileOutputStream(file);

            if (byteArray == null) {
                System.out.println("Invalid byte array");
            }

            // Write the byte array to the file output stream
            outputStream.write(byteArray);

            // Close the file output stream
            outputStream.close();
            System.out.println("File created");
            if (file.exists()) {
                System.out.println("The file now exists");
            }
        } catch (IOException e) {
            System.out.println("Error thrown while trying to save file");
            e.printStackTrace();
        }
        clearEverything();
    }

    private void clearEverything() {
        LinearLayout linearLayout = findViewById(R.id.checkbox_linear_layout);
        linearLayout.removeAllViews();
        linearLayout.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}



