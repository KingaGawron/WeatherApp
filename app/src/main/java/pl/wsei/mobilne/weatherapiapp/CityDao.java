package pl.wsei.mobilne.weatherapiapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CityDao {
    @Insert
    void insertCity(City city);

    @Query("SELECT * FROM city")
    List<City> getAllCities();
    @Query("SELECT * FROM city WHERE name = :cityName LIMIT 1")
    City getCityByName(String cityName);
    @Delete
    void deleteCity(City city);
    @Query("SELECT * FROM city WHERE LOWER(name) = LOWER(:cityName)")
    List<City> getCitiesByName(String cityName);


}
