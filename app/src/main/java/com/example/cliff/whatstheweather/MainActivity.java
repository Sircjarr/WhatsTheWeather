package com.example.cliff.whatstheweather;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText inputWeatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputWeatherText = (EditText) findViewById(R.id.cityInputText);
    }

    public void getWeather(View view) {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        String cityName = inputWeatherText.getText().toString();

        if (cityName.trim().length() == 0) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_LONG).show();
        }
        else{
            try {
                // Account for spaces in the URL by encoding the input
                String newCityName = URLEncoder.encode(cityName, "UTF-8");
                Intent intent = new Intent(this, WeatherDisplayActivity.class);
                intent.putExtra("newCityName", newCityName);
                startActivity(intent);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast.makeText(this, "Could not find weather", Toast.LENGTH_LONG).show();
            }

        }
    }
}
