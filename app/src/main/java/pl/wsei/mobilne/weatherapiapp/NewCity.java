package pl.wsei.mobilne.weatherapiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;

public class NewCity extends AppCompatActivity {

    EditText etCity;
    TextView tvResult;
    LinearLayout llCityList; // Nowy kontener na przyciski usuwania dla każdego miasta
    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appId = "cb263a9ed6988851a2ee09f0fe224e91";
    DecimalFormat df = new DecimalFormat("#.##");
    CityDatabase cityDatabase; // Obiekt bazy danych

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llCityList = findViewById(R.id.llCityList);
        setContentView(R.layout.activity_new_city);
        etCity = findViewById(R.id.etCity);
        tvResult = findViewById(R.id.tvResult);
        llCityList = findViewById(R.id.llCityList); // Inicjalizacja kontenera na przyciski usuwania miast

        // Inicjalizacja bazy danych
        cityDatabase = CityDatabaseClient.getInstance(getApplicationContext()).getCityDatabase();
        updateCityList();
    }

    public void getWeatherDetails(View view) {
        String tempUrl = "";
        String cityName = etCity.getText().toString().trim();
        if (cityName.equals("")) {
            tvResult.setText("City field cannot be empty!");
        } else {
            tempUrl = url + "?q=" + cityName + "&appId=" + appId;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15;
                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                    float pressure = jsonObjectMain.getInt("pressure");
                    int humidity = jsonObjectMain.getInt("humidity");
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    String wind = jsonObjectWind.getString("speed");
                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                    String clouds = jsonObjectClouds.getString("all");
                    JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                    String countryName = jsonObjectSys.getString("country");
                    String cityName = jsonResponse.getString("name");
                    tvResult.setTextColor(Color.rgb(0, 0, 0));
                    output += " Current weather of " + cityName + " (" + countryName + ")"
                            + "\n Temp: " + df.format(temp) + " °C"
                            + "\n Feels Like: " + df.format(feelsLike) + " °C"
                            + "\n Humidity: " + humidity + "%"
                            + "\n Description: " + description
                            + "\n Wind Speed: " + wind + "m/s (meters per second)"
                            + "\n Cloudiness: " + clouds + "%"
                            + "\n Pressure: " + pressure + " hPa";
                    tvResult.setText(output);

                    // Zapisanie miasta do bazy danych
                    City city = new City(cityName);
                    city.setTemperature(temp);
                    city.setDescription(description);
                    city.setHumidity(humidity);
                    city.setWindSpeed(wind);
                    city.setCloudiness(clouds);
                    city.setPressure(pressure);
                    saveCityToDatabase(city);

                    // Aktualizacja listy miast
                    updateCityList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void saveCityToDatabase(final City city) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Zamień nazwę miasta na małe litery przed porównaniem
                String cityNameLower = city.getName().toLowerCase();

                // Sprawdź, czy miasto już istnieje w bazie danych
                List<City> existingCities = cityDatabase.cityDao().getCitiesByName(cityNameLower);
                boolean cityExists = false;
                for (City existingCity : existingCities) {
                    if (existingCity.getName().equalsIgnoreCase(city.getName())) {
                        cityExists = true;
                        break;
                    }
                }

                if (!cityExists) {
                    // Miasto nie istnieje, można je dodać do bazy danych
                    cityDatabase.cityDao().insertCity(city);
                } else {
                    // Miasto już istnieje, wyświetl komunikat o istniejącym wpisie
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewCity.this, "City already exists: " + city.getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    private void updateCityList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final List<City> cityList = cityDatabase.cityDao().getAllCities();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llCityList.removeAllViews(); // Usunięcie istniejących przycisków przed dodaniem nowych

                        for (final City city : cityList) {
                            // Dodanie przycisku usuwania dla miasta
                            Button btnDelete = new Button(NewCity.this);
                            btnDelete.setText("[Usuń] " + city.getName());
                            btnDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteCity(city);
                                }
                            });

                            llCityList.addView(btnDelete); // Dodanie przycisku do kontenera
                        }
                    }
                });
            }
        });
    }

    private void deleteCity(final City city) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                cityDatabase.cityDao().deleteCity(city);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewCity.this, "City deleted: " + city.getName(), Toast.LENGTH_SHORT).show();
                        updateCityList(); // Odświeżenie listy miast po usunięciu
                    }
                });
            }
        });
    }

    //MENU

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.item1) {
            Intent intent = new Intent(NewCity.this,About.class);
            startActivity(intent);
            return true;
        }
        else if (id==R.id.item2) {
            Intent intent = new Intent(NewCity.this, Settings.class);
            startActivity(intent);
            return true;
        }

        else if (id==R.id.item3) {
            Intent intent = new Intent(NewCity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id==R.id.item4) {
            Intent intent = new Intent(NewCity.this, NewCity.class);
            startActivity(intent);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

}
