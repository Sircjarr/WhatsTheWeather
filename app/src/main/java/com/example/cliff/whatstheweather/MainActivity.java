package com.example.cliff.whatstheweather;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText inputWeatherText;
    TextView resultNameText;
    TextView resultWeatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputWeatherText = (EditText) findViewById(R.id.cityInputText);
        resultNameText = (TextView) findViewById(R.id.resultNameText);
        resultWeatherText = (TextView) findViewById(R.id.resultWeatherText);
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
                DownloadTask task = new DownloadTask();
                task.execute("http://api.openweathermap.org/data/2.5/weather?q=" +
                        newCityName + "&appid=9fb1de6c6ceddb029f160d2d5c22d65b");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast.makeText(this, "Could not find weather", Toast.LENGTH_LONG).show();
            }
        }
    }

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
                    result += (line + "\n");
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
                double temperature;
                int humidity;
                double temperatureMax;
                double windSpeed;
                String name;

                // Get weather description
                JSONObject jo = new JSONObject(s);
                String weather = jo.getString("weather");
                JSONArray ja = new JSONArray(weather);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject JSONpart = ja.getJSONObject(i);
                    description = JSONpart.getString("description");
                }
                Log.i("description", description);

                // Get temperature data
                String main = jo.getString("main");
                JSONObject joMain = new JSONObject(main);
                temperature = joMain.getDouble("temp");
                temperature = kelvinToF(temperature);
                humidity = joMain.getInt("humidity");
                temperatureMax = joMain.getDouble("temp_max");
                temperatureMax = kelvinToF(temperatureMax);
                Log.i("temperature", String.valueOf(temperature));
                Log.i("humidity", String.valueOf(humidity));
                Log.i("temperatureMax", String.valueOf(temperatureMax));

                // Get wind data
                String wind = jo.getString("wind");
                JSONObject joWind = new JSONObject(wind);
                windSpeed = joWind.getDouble("speed");
                Log.i("windSpeed", String.valueOf(windSpeed));

                // Get name of city
                name = jo.getString("name");
                Log.i("name", name);

                // Display data on the UI
                resultNameText.setText(name);
                String weatherText =
                        description + "\r\n\n"
                                + "Temperature: " + String.format("%.4s", String.valueOf(temperature)) + (char)0x00b0 + "F\r\n"
                                + "High: " + String.format("%.4s", String.valueOf(temperatureMax)) + (char)0x00b0 + "F\r\n"
                                + "Humidity: " + humidity + "%\r\n"
                                + "Wind speed: " + windSpeed + "mph\r\n";

                resultWeatherText.setText(weatherText);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error parsing weather data", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static double kelvinToF(double kelvins) {
        return (1.8 * (kelvins - 273)) + 32;
    }
}
