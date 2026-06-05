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

public class SensorGraphFragment extends Fragment implements SensorEventListener {

    private static final String KEY_TYPE = "s_type";
    private static final String KEY_LABEL = "s_label";
    private static final String KEY_COMPUTATION = "s_computation";

    private SensorManager sensorMgr;
    private Sensor activeSensor;

    private TextView monitorText;
    private RealTimeChartView plottingView;

    private int sensorTypeCode;
    private String screenTitle;
    private String dataMode;

    private final Handler mockHandler = new Handler(Looper.getMainLooper());
    private float mockStep = 0f;
    private boolean dataReceived = false;

    // Watchdog to start simulation if real sensor is silent (common on emulators)
    private final Runnable simulationWatchdog = new Runnable() {
        @Override
        public void run() {
            if (!dataReceived) {
                monitorText.setText("Real sensor silent. Switching to simulation...");
                beginMocking();
            }
        }
    };

    public static SensorGraphFragment build(int type, String label, String computation) {
        SensorGraphFragment frag = new SensorGraphFragment();
        Bundle b = new Bundle();
        b.putInt(KEY_TYPE, type);
        b.putString(KEY_LABEL, label);
        b.putString(KEY_COMPUTATION, computation);
        frag.setArguments(b);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            sensorTypeCode = getArguments().getInt(KEY_TYPE);
            screenTitle = getArguments().getString(KEY_LABEL);
            dataMode = getArguments().getString(KEY_COMPUTATION);
        }

        sensorMgr = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        activeSensor = sensorMgr.getDefaultSensor(sensorTypeCode);

        LinearLayout mainLayout = new LinearLayout(requireContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        TextView header = new TextView(requireContext());
        header.setText(screenTitle);
        header.setTextSize(24);
        header.setPadding(0, 0, 0, 25);

        monitorText = new TextView(requireContext());
        monitorText.setText("Waiting for sensor data...");
        monitorText.setTextSize(18);
        monitorText.setPadding(0, 0, 0, 25);

        plottingView = new RealTimeChartView(requireContext());
        plottingView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 650));

        mainLayout.addView(header);
        mainLayout.addView(monitorText);
        mainLayout.addView(plottingView);

        return mainLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        dataReceived = false;
        if (activeSensor != null) {
            sensorMgr.registerListener(this, activeSensor, SensorManager.SENSOR_DELAY_UI);
            // Start watchdog: if no data after 3 seconds, start simulation
            mockHandler.postDelayed(simulationWatchdog, 3000);
        } else {
            monitorText.setText("Sensor not found. Starting emulation...");
            beginMocking();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
        mockHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!dataReceived) {
            dataReceived = true;
            mockHandler.removeCallbacks(simulationWatchdog);
        }
        float processedVal = parseSensorData(event.values);
        refreshDisplay(processedVal);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private float parseSensorData(float[] rawData) {
        if ("VECTOR_MAGNITUDE".equalsIgnoreCase(dataMode)) {
            return (float) Math.sqrt(rawData[0] * rawData[0] + rawData[1] * rawData[1] + rawData[2] * rawData[2]);
        }
        return rawData[0];
    }

    private void refreshDisplay(float value) {
        monitorText.setText("Current Reading: " + String.format("%.2f", value));
        plottingView.appendValue(value);
    }

    private void beginMocking() {
        mockHandler.removeCallbacksAndMessages(null);
        mockHandler.post(new Runnable() {
            @Override
            public void run() {
                mockStep++;
                float mockVal;
                switch (sensorTypeCode) {
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        mockVal = 22f + (float) Math.cos(mockStep / 6f) * 4f;
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        mockVal = 50f + (float) Math.sin(mockStep / 8f) * 10f;
                        break;
                    case Sensor.TYPE_PROXIMITY:
                        mockVal = (mockStep % 10 < 5) ? 1f : 8f;
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mockVal = 40f + (float) (Math.random() * 5);
                        break;
                    default:
                        mockVal = (float) Math.sin(mockStep / 2f);
                }
                refreshDisplay(mockVal);
                mockHandler.postDelayed(this, 800);
            }
        });
    }
}
