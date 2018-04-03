package application.location;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KML {
    private StringProperty name;
    private StringProperty description;
    private DoubleProperty latitude;
    private DoubleProperty longitude;
    private DoubleProperty altitude;

    public KML(String name, String description, double latitude, double longitude, double altitude) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.latitude = new SimpleDoubleProperty(latitude);
        this.longitude = new SimpleDoubleProperty(longitude);
        this.altitude = new SimpleDoubleProperty(altitude);
    }

    public String getCoordinate() {
        return longitude.get() + " " + latitude.get();
    }

    public String getAllValues() {
        return longitude.get() + "," + latitude.get() + "," + altitude.get();
    }

    //Property Getters
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

    //Value Getters
    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public double getLatitude() {
        return latitude.get();
    }

    public double getLongitude() {
        return longitude.get();
    }

    public double getAltitude() {
        return altitude.get();
    }

    //Setters
    public void setName(String name) {
        this.name.set(name);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setLatitude(double latitude) {
        this.latitude.set(latitude);
    }

    public void setLongitude(double longitude) {
        this.longitude.set(longitude);
    }

    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
    }

    //ToString
    @Override
    public String toString() {
        return name.get() + " " + description.get() + " " + latitude.get() + " " + longitude.get() + " " + altitude.get();
    }
}
