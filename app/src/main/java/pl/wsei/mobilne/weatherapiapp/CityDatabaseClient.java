package pl.wsei.mobilne.weatherapiapp;

import android.content.Context;

public class CityDatabaseClient {
    private static CityDatabaseClient instance;
    private CityDatabase cityDatabase;

    private CityDatabaseClient(Context context) {
        cityDatabase = CityDatabase.getInstance(context);
    }

    public static synchronized CityDatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new CityDatabaseClient(context);
        }
        return instance;
    }

    public CityDatabase getCityDatabase() {
        return cityDatabase;
    }
}
