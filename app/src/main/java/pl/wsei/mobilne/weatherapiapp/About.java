package pl.wsei.mobilne.weatherapiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
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
        if (id==R.id.item2) {
            Intent intent = new Intent(About.this, Settings.class);
            startActivity(intent);
            return true;
        }
        else if (id==R.id.item3) {
            Intent intent = new Intent(About.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id==R.id.item4) {
            Intent intent = new Intent(About.this, NewCity.class);
            startActivity(intent);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
}