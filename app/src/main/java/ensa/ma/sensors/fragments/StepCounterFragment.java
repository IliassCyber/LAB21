package ensa.ma.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StepCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager sm;
    private Sensor stepSensor;
    private TextView infoDisplay;

    private float startOffset = -1f;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    activateSensor();
                } else {
                    infoDisplay.setText("Step counting permission was denied.");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoDisplay = new TextView(requireContext());
        infoDisplay.setTextSize(20);
        infoDisplay.setPadding(40, 40, 40, 40);

        sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        return infoDisplay;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (stepSensor == null) {
            infoDisplay.setText("Step counter hardware is missing on this device.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
            } else {
                activateSensor();
            }
        } else {
            activateSensor();
        }
    }

    private void activateSensor() {
        sm.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float absoluteCount = event.values[0];

        if (startOffset < 0) {
            startOffset = absoluteCount;
        }

        int currentSessionSteps = (int) (absoluteCount - startOffset);

        StringBuilder report = new StringBuilder();
        report.append("Total steps since reboot: ").append((int) absoluteCount).append("\n\n");
        report.append("Steps in this session: ").append(currentSessionSteps);
        
        infoDisplay.setText(report.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
