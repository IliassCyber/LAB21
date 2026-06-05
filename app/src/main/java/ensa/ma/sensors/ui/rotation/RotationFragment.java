package ensa.ma.sensors.ui.rotation;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Date;

import ensa.ma.sensors.R;


public class RotationFragment extends Fragment implements SensorEventListener {


    private LineChart chart;
    private SensorManager mSensorManager;
    private Sensor mAcceleratorSensor;
    static ArrayList<Entry> entries = new ArrayList<>();
    static ArrayList<Entry> entries1 = new ArrayList<>();
    static ArrayList<Entry> entries2 = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAcceleratorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAcceleratorSensor == null){
            Toast.makeText(getContext(), R.string.message_neg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_rotation, container, false);
        chart = (LineChart) root.findViewById(R.id.chart);
        return root;
    }

    private void addEntry(SensorEvent event) {
        Date d = new Date();
        entries.add(new Entry(entries.size(), event.values[0]));
        entries1.add(new Entry(entries1.size(), event.values[1]));
        entries2.add(new Entry(entries2.size(), event.values[2]));

        LineDataSet dataSet = new LineDataSet(entries, "Rotation - X");
        dataSet.setDrawFilled(true);
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        LineDataSet dataSet1 = new LineDataSet(entries1, "Rotation - Z");
        dataSet1.setDrawFilled(true);
        dataSet1.setColor(Color.GRAY);
        dataSet1.setCircleColor(Color.GRAY);
        LineDataSet dataSet2 = new LineDataSet(entries2, "Rotation - Y");
        dataSet2.setDrawFilled(true);
        dataSet2.setColor(Color.YELLOW);
        dataSet2.setCircleColor(Color.YELLOW);


        ArrayList<ILineDataSet> lines = new ArrayList<ILineDataSet> ();
        lines.add(dataSet);
        lines.add(dataSet1);
        lines.add(dataSet2);
        Log.d("size", entries.size()+"");
        XAxis xAxis = chart.getXAxis();
        chart.setData(new LineData(lines));
        chart.notifyDataSetChanged();
        //refresh
        chart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAcceleratorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        entries.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        entries.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        addEntry(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}