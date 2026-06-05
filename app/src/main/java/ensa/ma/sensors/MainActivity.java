package ensa.ma.sensors;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import ensa.ma.sensors.fragments.ActivityRecognitionFragment;
import ensa.ma.sensors.fragments.CompassFragment;
import ensa.ma.sensors.fragments.MotionSensorFragment;
import ensa.ma.sensors.fragments.SensorGraphFragment;
import ensa.ma.sensors.fragments.SensorsListFragment;
import ensa.ma.sensors.fragments.StepCounterFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            loadScreen(new SensorsListFragment());
            navigationView.setCheckedItem(R.id.nav_sensors);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            loadScreen(new SensorsListFragment());
        } else if (id == R.id.nav_temp) {
            loadScreen(SensorGraphFragment.build(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient Temperature", "SINGLE_VALUE"));
        } else if (id == R.id.nav_humidity) {
            loadScreen(SensorGraphFragment.build(Sensor.TYPE_RELATIVE_HUMIDITY, "Relative Humidity", "SINGLE_VALUE"));
        } else if (id == R.id.nav_proximity) {
            loadScreen(SensorGraphFragment.build(Sensor.TYPE_PROXIMITY, "Proximity Monitor", "SINGLE_VALUE"));
        } else if (id == R.id.nav_magnetic) {
            loadScreen(SensorGraphFragment.build(Sensor.TYPE_MAGNETIC_FIELD, "Magnetic Field Intensity", "VECTOR_MAGNITUDE"));
        } else if (id == R.id.nav_accel) {
            loadScreen(MotionSensorFragment.create(Sensor.TYPE_ACCELEROMETER, "Accelerometer Data"));
        } else if (id == R.id.nav_gravity) {
            loadScreen(MotionSensorFragment.create(Sensor.TYPE_GRAVITY, "Gravity Vector"));
        } else if (id == R.id.nav_gyro) {
            loadScreen(MotionSensorFragment.create(Sensor.TYPE_GYROSCOPE, "Rotation Rate (Gyro)"));
        } else if (id == R.id.nav_steps) {
            loadScreen(new StepCounterFragment());
        } else if (id == R.id.nav_compass) {
            loadScreen(new CompassFragment());
        } else if (id == R.id.nav_activity) {
            loadScreen(new ActivityRecognitionFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
