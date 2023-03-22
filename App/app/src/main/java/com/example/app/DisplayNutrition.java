package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DisplayNutrition extends AppCompatActivity {

    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_nutrition);

        tableLayout = findViewById(R.id.disp_table_layout);
        Intent intent = getIntent();
        HashMap<String, String> nutritionVals = (HashMap<String, String>) intent.getSerializableExtra("nutrition");
        ArrayList<String> foods = (ArrayList<String>) intent.getSerializableExtra("foods");
        populateTable(nutritionVals);

        FloatingActionButton close = findViewById(R.id.close_btn);
        close.setOnClickListener(v->{
            Intent intent_home = new Intent(DisplayNutrition.this, homePage.class);
            startActivity(intent_home);
//            finish();
        });
    }

    private void populateTable(HashMap<String, String> nutritionVals) {


//        TableRow tableRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.disp_table_row_template, null);

        if (nutritionVals != null) {
            for (Map.Entry<String, String> entry : nutritionVals.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.d("HashMap", "Key: " + key + ", Value: " + value);

                LayoutInflater inflater = LayoutInflater.from(this);
                View tableRow = inflater.inflate(R.layout.disp_table_row_template, tableLayout, false);

                TextView col1TextView = tableRow.findViewById(R.id.disp_col1);
                System.out.println(key);
                col1TextView.setText(key);
                TextView col2TextView = tableRow.findViewById(R.id.disp_col2);
                col2TextView.setText(value);

                tableLayout.addView(tableRow);

            }
        }

        // Populate the TableRow with data from the array
//        TextView col1TextView = tableRow.findViewById(R.id.disp_col1);
//        System.out.println(dataArray[i]);
//        col1TextView.setText(dataArray[i]);
//        TextView col2TextView = tableRow.findViewById(R.id.disp_col2);
//        col2TextView.setText(dataArray[i]);

        // Add the TableRow to the TableLayout
    }
}