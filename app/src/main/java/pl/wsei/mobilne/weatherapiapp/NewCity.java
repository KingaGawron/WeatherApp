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
import android.widget.EditText;
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
    TextView tvCityList; // Nowe pole do wyświetlania listy miast
    private final String url="https://api.openweathermap.org/data/2.5/weather";
    private final String appId="cb263a9ed6988851a2ee09f0fe224e91";
    DecimalFormat df = new DecimalFormat("#.##");
    CityDatabase cityDatabase; // Obiekt bazy danych

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_city);
        etCity = findViewById(R.id.etCity);
        tvResult = findViewById(R.id.tvResult);
        tvCityList = findViewById(R.id.tvCityList); // Inicjalizacja pola do wyświetlania listy miast

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
                        StringBuilder sb = new StringBuilder();
                        for (City city : cityList) {
                            sb.append(city.getName()).append(" - ").append(df.format(city.getTemperature())).append(" °C");
                            sb.append(" ");
                            sb.append("[Usuń]");
                            sb.append("\n");
                        }
                        tvCityList.setText(sb.toString());
                        tvCityList.setMovementMethod(new ScrollingMovementMethod()); // Dodanie obsługi przewijania tekstu
                        tvCityList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int offset = tvCityList.getOffsetForPosition(v.getX(), v.getY()); // Pobranie pozycji kliknięcia w TextView
                                int line = tvCityList.getLayout().getLineForOffset(offset); // Pobranie linii dla danej pozycji
                                int start = tvCityList.getLayout().getLineStart(line); // Początek linii
                                int end = tvCityList.getLayout().getLineEnd(line); // Koniec linii
                                String clickedLine = tvCityList.getText().subSequence(start, end).toString(); // Pobranie klikniętej linii
                                String cityName = clickedLine.substring(0, clickedLine.indexOf(" -")); // Wyodrębnienie nazwy miasta

                                // Wywołanie metody do usunięcia miasta z bazy danych
                                deleteCity(cityName);
                            }
                        });
                    }
                });
            }
        });
    }
    private void deleteCity(final String cityName) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                City city = cityDatabase.cityDao().getCityByName(cityName);
                if (city != null) {
                    cityDatabase.cityDao().deleteCity(city);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewCity.this, "City deleted: " + cityName, Toast.LENGTH_SHORT).show();
                            updateCityList(); // Odświeżenie listy miast po usunięciu
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewCity.this, "City not found: " + cityName, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
