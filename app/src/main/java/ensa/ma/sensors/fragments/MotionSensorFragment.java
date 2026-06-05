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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.views.RealTimeChartView;

public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String PARAM_TYPE = "motion_type";
    private static final String PARAM_LABEL = "motion_label";

    private SensorManager sensorManager;
    private Sensor motionSensor;

    private TextView detailView;
    private RealTimeChartView chartView;

    private int typeCode;
    private String label;

    private boolean dataReceived = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private float mockStep = 0f;

    private final Runnable simulationWatchdog = new Runnable() {
        @Override
        public void run() {
            if (!dataReceived) {
                detailView.setText("No hardware data. Simulating motion...");
                startSimulation();
            }
        }
    };

    public static MotionSensorFragment create(int type, String label) {
        MotionSensorFragment fragment = new MotionSensorFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_TYPE, type);
        args.putString(PARAM_LABEL, label);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            typeCode = getArguments().getInt(PARAM_TYPE);
            label = getArguments().getString(PARAM_LABEL);
        }

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        motionSensor = sensorManager.getDefaultSensor(typeCode);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        TextView titleView = new TextView(requireContext());
        titleView.setText(label);
        titleView.setTextSize(22);

        detailView = new TextView(requireContext());
        detailView.setText("Waiting for sensor...");
        detailView.setTextSize(16);
        detailView.setPadding(0, 30, 0, 30);

        chartView = new RealTimeChartView(requireContext());
        chartView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));

        root.addView(titleView);
        root.addView(detailView);
        root.addView(chartView);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        dataReceived = false;
        if (motionSensor != null) {
            sensorManager.registerListener(this, motionSensor, SensorManager.SENSOR_DELAY_UI);
            handler.postDelayed(simulationWatchdog, 1500); // 1.5s watchdog
        } else {
            startSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!dataReceived) {
            dataReceived = true;
            handler.removeCallbacks(simulationWatchdog);
        }
        updateMotionData(event.values[0], event.values[1], event.values[2]);
    }

    private void updateMotionData(float x, float y, float z) {
        float mag = (float) Math.sqrt(x * x + y * y + z * z);
        String info = String.format("X: %.2f | Y: %.2f | Z: %.2f\nMagnitude: %.2f", x, y, z, mag);
        detailView.setText(info);
        chartView.appendValue(mag);
    }

    private void startSimulation() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mockStep += 0.2f;
                float x = (float) Math.sin(mockStep) * 2;
                float y = (float) Math.cos(mockStep * 0.7f) * 3;
                float z = 9.81f + (float) Math.sin(mockStep * 1.2f);
                updateMotionData(x, y, z);
                handler.postDelayed(this, 500);
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
