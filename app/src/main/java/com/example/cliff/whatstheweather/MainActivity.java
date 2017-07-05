package com.example.cliff.whatstheweather;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText inputWeatherText;
    ListView favoritesListView;
    static ArrayList<String> favorites;
    static ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputWeatherText = (EditText) findViewById(R.id.cityInputText);
        favoritesListView = (ListView) findViewById(R.id.favoritesListView);

        favorites = new ArrayList<>();
        favorites.add("Tulsa");

        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_white_text, favorites);
        favoritesListView.setAdapter(arrayAdapter);

        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WeatherDisplayActivity.class);
                intent.putExtra("newCityName", favorites.get(position));
                startActivity(intent);
            }
        });

        favoritesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {

                final int itemToDelete = i;

                 new AlertDialog.Builder(MainActivity.this)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle("Are you sure?")
                         .setMessage("Do you want to delete this city?")
                         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 favorites.remove(itemToDelete);
                                 arrayAdapter.notifyDataSetChanged();
                             }
                         })
                         .setNegativeButton("No", null)
                         .show();
                 return true;
            }
        });
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
