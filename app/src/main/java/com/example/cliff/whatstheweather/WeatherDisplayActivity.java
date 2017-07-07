package com.example.cliff.whatstheweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class WeatherDisplayActivity extends AppCompatActivity {

    TextView cityTextView;
    TextView descriptionTextView;
    ImageView weatherIcon;
    TextView weatherTextView;

    Button favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_weather_display);

        cityTextView = (TextView) findViewById(R.id.cityTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
        weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        favoriteButton = (Button) findViewById(R.id.favoriteButton);

        // Retrieve newCityName String variable from MainActivity
        Intent intent = getIntent();
        String newCityName = intent.getStringExtra("newCityName");

        // Begin retrieving weather data from the API
        try {
            DownloadTask task = new DownloadTask();
            task.execute("http://api.openweathermap.org/data/2.5/weather?q=" +
                    newCityName + "&appid=9fb1de6c6ceddb029f160d2d5c22d65b");
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not find weather", Toast.LENGTH_LONG).show();
        }
    }

    // Class Asynchronous Task allows separate thread execution for resource-heavy operations
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                }

                return result;
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather", Toast.LENGTH_LONG).show();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                String description = "";
                String icon = "";
                double temperature;
                int humidity;
                double temperatureMax;
                double windSpeed;
                String name;

                // Use the JSON data from the API call to get desired data
                // Get weather description
                JSONObject jo = new JSONObject(s);
                String weather = jo.getString("weather");
                JSONArray ja = new JSONArray(weather);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject JSONpart = ja.getJSONObject(i);
                    description = JSONpart.getString("description");
                    icon = JSONpart.getString("icon");
                }

                // Get temperature data
                String main = jo.getString("main");
                JSONObject joMain = new JSONObject(main);
                temperature = joMain.getDouble("temp");
                temperature = kelvinToF(temperature);
                humidity = joMain.getInt("humidity");
                temperatureMax = joMain.getDouble("temp_max");
                temperatureMax = kelvinToF(temperatureMax);

                // Get wind data
                String wind = jo.getString("wind");
                JSONObject joWind = new JSONObject(wind);
                windSpeed = joWind.getDouble("speed");

                // Get name of city
                name = jo.getString("name");

                // Display data on the UI
                cityTextView.setText(name);
                if (!MainActivity.favorites.contains(name)) {
                    favoriteButton.setText("Favorite");
                }
                else {
                    favoriteButton.setText("Unfavorite");
                }

                descriptionTextView.setText(description);

                // Fetch and set the weather icon
                if (!icon.equals("")) {
                    ImageDownloader task = new ImageDownloader();
                    Bitmap img;
                    try {
                        img = task.execute("http://openweathermap.org/img/w/" + icon + ".png").get();
                        if (img != null) {
                            weatherIcon.setImageBitmap(img);
                            weatherIcon.animate().alpha(1f).setDuration(1200);
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                // Format the way weather is displayed
                String weatherText =
                                "Temperature: " + String.format("%.4s", String.valueOf(temperature)) + (char)0x00b0 + "F\r\n"
                                + "High: " + String.format("%.4s", String.valueOf(temperatureMax)) + (char)0x00b0 + "F\r\n"
                                + "Humidity: " + humidity + "%\r\n"
                                + "Wind speed: " + windSpeed + "mph\r\n";

                weatherTextView.setText(weatherText);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error parsing weather data", Toast.LENGTH_LONG).show();
            }
        }
    }

    // API returns temperatures in Kelvin by default
    public static double kelvinToF(double kelvins) {
        return (1.8 * (kelvins - 273)) + 32;
    }

    public void addOrRemoveFavorite(View view) {
        if(favoriteButton.getText().toString().equals("Favorite")) {

            favoriteButton.setText("Unfavorite");
            MainActivity.favorites.add(cityTextView.getText().toString());
            MainActivity.arrayAdapter.notifyDataSetChanged();

            // Code to convert ArrayList<String> favorites to a serializable HashSet<String> and store in sharedPreferences
            SharedPreferences sp = this.getSharedPreferences("com.example.cliff.whatstheweather", Context.MODE_PRIVATE);
            Set<String> set = new HashSet<>(MainActivity.favorites);
            sp.edit().putStringSet("favorites", set).apply();

            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
        else {

            favoriteButton.setText("Favorite");
            MainActivity.favorites.remove(cityTextView.getText().toString());
            MainActivity.arrayAdapter.notifyDataSetChanged();

            SharedPreferences sp = this.getSharedPreferences("com.example.cliff.whatstheweather", Context.MODE_PRIVATE);
            Set<String> set = new HashSet<>(MainActivity.favorites);
            sp.edit().putStringSet("favorites", set).apply();

            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                // Convert data into an image
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();

                //BitmapFactory is code to manipulate, encode, decode images
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;
            }
            catch(MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
