package ensa.ma.sensors.beans;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class  SensorItem {
        public final String id;
        public final String name;
        public final String type;
        public final String vendor;
        public final String version;
        public final String resolution;
        public final String range;
        public final String power;
        public final String max_speed;


        public SensorItem(String id, String name, String type, String vendor, String version, String resolution, String range, String power, String max_speed) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.vendor = vendor;
            this.version = version;
            this.resolution = resolution;
            this.range = range;
            this.power = power;
            this.max_speed = max_speed;
        }

        @Override
        public String toString() {
            return name;
        }

}
