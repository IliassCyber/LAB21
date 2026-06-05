package ensa.ma.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.utils.SensorDataMapper;

import java.util.List;

public class SensorsListFragment extends Fragment {

    private SensorManager sm;
    private LinearLayout listContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView root = new ScrollView(requireContext());
        listContainer = new LinearLayout(requireContext());
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(30, 30, 30, 30);
        root.addView(listContainer);

        sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        populateSensorList();

        return root;
    }

    private void populateSensorList() {
        List<Sensor> deviceSensors = sm.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s : deviceSensors) {
            TextView infoText = new TextView(requireContext());
            infoText.setText(SensorDataMapper.getSensorDescription(s));
            infoText.setTextSize(15);
            infoText.setPadding(10, 20, 10, 20);

            listContainer.addView(infoText);

            View divider = new View(requireContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3));
            divider.setBackgroundColor(0xFFCCCCCC);
            listContainer.addView(divider);
        }
    }
}
