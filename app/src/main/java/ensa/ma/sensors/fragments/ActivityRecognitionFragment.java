package ensa.ma.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.Queue;

public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private SensorManager sm;
    private Sensor accelerometer;

    private TextView statusView;

    private final float[] gravityEstimate = new float[3];
    private final Queue<Float> dataWindow = new LinkedList<>();

    private static final int BUFFER_CAPACITY = 40;
    private static final float FILTER_SMOOTHING = 0.75f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        statusView = new TextView(requireContext());
        statusView.setTextSize(20);
        statusView.setPadding(35, 35, 35, 35);

        sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return statusView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        } else {
            statusView.setText("No accelerometer found.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float rawX = event.values[0];
        float rawY = event.values[1];
        float rawZ = event.values[2];

        // Low-pass filter to isolate gravity
        gravityEstimate[0] = FILTER_SMOOTHING * gravityEstimate[0] + (1 - FILTER_SMOOTHING) * rawX;
        gravityEstimate[1] = FILTER_SMOOTHING * gravityEstimate[1] + (1 - FILTER_SMOOTHING) * rawY;
        gravityEstimate[2] = FILTER_SMOOTHING * gravityEstimate[2] + (1 - FILTER_SMOOTHING) * rawZ;

        // High-pass filter to get linear acceleration
        float linX = rawX - gravityEstimate[0];
        float linY = rawY - gravityEstimate[1];
        float linZ = rawZ - gravityEstimate[2];

        float totalMotion = (float) Math.sqrt(linX * linX + linY * linY + linZ * linZ);
        trackMovement(totalMotion);

        String currentActivity = analyzePattern(rawX, rawY, rawZ);

        StringBuilder info = new StringBuilder();
        info.append("Raw X: ").append(String.format("%.2f", rawX)).append("\n");
        info.append("Raw Y: ").append(String.format("%.2f", rawY)).append("\n");
        info.append("Raw Z: ").append(String.format("%.2f", rawZ)).append("\n\n");
        info.append("Motion Intensity: ").append(String.format("%.2f", totalMotion)).append("\n\n");
        info.append("Estimated State: ").append(currentActivity);

        statusView.setText(info.toString());
    }

    private void trackMovement(float val) {
        if (dataWindow.size() >= BUFFER_CAPACITY) {
            dataWindow.poll();
        }
        dataWindow.add(val);
    }

    private String analyzePattern(float x, float y, float z) {
        if (dataWindow.size() < BUFFER_CAPACITY) {
            return "Initializing...";
        }

        float avg = 0f;
        float peak = 0f;
        for (float f : dataWindow) {
            avg += f;
            if (f > peak) peak = f;
        }
        avg /= dataWindow.size();

        float varianceSum = 0f;
        for (float f : dataWindow) {
            varianceSum += (f - avg) * (f - avg);
        }
        float stdDev = (float) Math.sqrt(varianceSum / dataWindow.size());

        if (peak > 11f) return "Jumping / Sudden Impact";
        if (stdDev > 1.3f) return "Walking / Moving";
        if (Math.abs(z) > 8.5f) return "Static (Face up/down)";
        if (Math.abs(y) > 7.5f || Math.abs(x) > 7.5f) return "Standing or Sitting (Vertical)";

        return "Idle / Balanced";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
