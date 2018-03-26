package application.utilities;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KML {
    private StringProperty name;
    private StringProperty description;
    private DoubleProperty latitude;
    private DoubleProperty longitude;
//    private Point point;
    private DoubleProperty altitude;

    public KML(String name, String description, double latitude, double longitude, double altitude) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
//        point = new Point(latitude, longitude);
        this.latitude = new SimpleDoubleProperty(latitude);
        this.longitude = new SimpleDoubleProperty(longitude);
        this.altitude = new SimpleDoubleProperty(altitude);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public DoubleProperty altitudeProperty() {
        return altitude;
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    public DoubleProperty longitudeProperty() {
        return longitude;
    }

        @Override
    public String toString() {
        return name.get() + " " + description.get() + " " + latitude.get() + " " + longitude.get() + " " + altitude.get();
    }

//    private class Point {
//        private double latitude;
//        private double longitude;
//
//        public Point(double latitude, double longitude) {
//            this.latitude = latitude;
//            this.longitude = longitude;
//        }
//
//        @Override
//        public String toString() {
//            return latitude  + ", " + longitude;
//        }
//    }
}
