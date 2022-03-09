package com.crosby.foodfinderapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.IOException;

public class QueryActivity extends AppCompatActivity {

    Button mSumbit;
    EditText mQuery;
    public static String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        mSumbit = (Button) findViewById(R.id.sumbit);
        mQuery = (EditText) findViewById(R.id.foodquery);

        // API TEST QUERY: https://api.yelp.com/v3/businesses/search?latitude=52.6336686&longitude=-1.9290604&categories=food&term=pizza&Authentication=Bearer UKYiviWRJJV0GFIK35QDH1i6BJur_tpf4A2hjVtItJwNSHqRhz-U7zKeEjiG5DWVDcP5Ipe6Za3ZvQ5_rSYJhg6TxviDxGxedK8g2qsm6hRZT8bORI91tCiJ2z4cXnYx
    }

    public void submitFood(View view) {
        if (mQuery.getText().toString().trim().length() > 0){
            query = mQuery.getText().toString();
            Intent intent = new Intent(QueryActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        } else{
            Toast.makeText(QueryActivity.this, "Please Enter a query into the Text Box", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
