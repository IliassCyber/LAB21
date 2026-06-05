package ensa.ma.sensors.utils;

import android.hardware.Sensor;
import android.os.Build;

public class SensorDataMapper {

    public static String getSensorDescription(Sensor s) {
        StringBuilder sb = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sb.append("Identifier: ").append(s.getId()).append("\n");
        }
        sb.append("Device Name: ").append(s.getName()).append("\n");
        sb.append("Manufacturer: ").append(s.getVendor()).append("\n");
        sb.append("Version: ").append(s.getVersion()).append("\n");
        sb.append("String Type: ").append(s.getStringType()).append("\n");
        sb.append("Numeric Type: ").append(s.getType()).append("\n");
        sb.append("Res: ").append(s.getResolution()).append("\n");
        sb.append("Power Consumption: ").append(s.getPower()).append(" mA\n");
        sb.append("Max Range: ").append(s.getMaximumRange()).append("\n");
        sb.append("Latency (Min): ").append(s.getMinDelay()).append(" µs\n");
        return sb.toString();
    }
}
