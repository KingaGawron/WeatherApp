package pl.wsei.mobilne.weatherapiapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pl.wsei.mobilne.weatherapiapp.City;

@Database(entities = {City.class}, version = 1, exportSchema = false)
public abstract class CityDatabase extends RoomDatabase {
    private static CityDatabase instance;

    public abstract CityDao cityDao();

    public static synchronized CityDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            CityDatabase.class, "city_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
