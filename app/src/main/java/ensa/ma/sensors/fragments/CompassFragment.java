package ensa.ma.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sm;
    private Sensor accel;
    private Sensor magnet;

    private TextView statusDisplay;

    private final float[] accelReadings = new float[3];
    private final float[] magnetReadings = new float[3];

    private boolean isAccelReady = false;
    private boolean isMagnetReady = false;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private float mockAngle = 0f;

    private final Runnable simulationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAccelReady || !isMagnetReady) {
                mockAngle = (mockAngle + 5) % 360;
                updateDisplay(mockAngle, "Simulated Direction");
                handler.postDelayed(this, 100);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        statusDisplay = new TextView(requireContext());
        statusDisplay.setTextSize(24);
        statusDisplay.setPadding(35, 35, 35, 35);
        statusDisplay.setText("Calibrating compass...");

        sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnet = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return statusDisplay;
    }

    @Override
    public void onResume() {
        super.onResume();
        isAccelReady = false;
        isMagnetReady = false;
        if (accel != null) sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        if (magnet != null) sm.registerListener(this, magnet, SensorManager.SENSOR_DELAY_UI);

        if (accel == null || magnet == null) {
            statusDisplay.setText("Sensors missing. Starting simulation...");
            handler.post(simulationRunnable);
        } else {
            // Start simulation if no data after 2 seconds
            handler.postDelayed(simulationRunnable, 2000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sm.unregisterListener(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelReadings, 0, 3);
            isAccelReady = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetReadings, 0, 3);
            isMagnetReady = true;
        }

        if (isAccelReady && isMagnetReady) {
            handler.removeCallbacks(simulationRunnable);
            processOrientation();
        }
    }

    private void processOrientation() {
        float[] rMat = new float[9];
        float[] orientation = new float[3];

        if (SensorManager.getRotationMatrix(rMat, null, accelReadings, magnetReadings)) {
            SensorManager.getOrientation(rMat, orientation);

            float azRad = orientation[0];
            float azDeg = (float) Math.toDegrees(azRad);
            if (azDeg < 0) azDeg += 360;

            updateDisplay(azDeg, computeCardinal(azDeg));
        }
    }
    
    private void updateDisplay(float degrees, String direction) {
        statusDisplay.setText(String.format("Heading: %.1f°\nDirection: %s", degrees, direction));
    }

    private String computeCardinal(float angle) {
        if (angle >= 337.5 || angle < 22.5) return "North";
        if (angle < 67.5) return "North-East";
        if (angle < 112.5) return "East";
        if (angle < 157.5) return "South-East";
        if (angle < 202.5) return "South";
        if (angle < 247.5) return "South-West";
        if (angle < 292.5) return "West";
        return "North-West";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
