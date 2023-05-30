package pl.wsei.mobilne.weatherapiapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.wsei.mobilne.weatherapiapp.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testWeatherDataFetching() {
        // Rozpoczęcie scenariusza aktywności
        ActivityScenario<MainActivity> activityScenario = activityScenarioRule.getScenario();

        // Oczekiwanie na załadowanie aktywności
        activityScenario.onActivity(activity -> {
            // Sprawdzenie, czy pozwolenie ACCESS_FINE_LOCATION jest udzielone
            Context context = InstrumentationRegistry.getInstrumentation().getContext();
            boolean hasLocationPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!hasLocationPermission) {
                // W przypadku braku pozwolenia, odrzucenie testu
                return;
            }

            // Wywołanie metody letsdoSomeNetworking
            activity.letsdoSomeNetworking(new RequestParams());

            // Sprawdzenie, czy dane pogodowe są wyświetlane
            onView(withId(R.id.weatherCondition)).check(matches(isDisplayed()));
            onView(withId(R.id.temperature)).check(matches(isDisplayed()));
            onView(withId(R.id.weatherIcon)).check(matches(isDisplayed()));
            onView(withId(R.id.cityName)).check(matches(isDisplayed()));
        });
    }
}
