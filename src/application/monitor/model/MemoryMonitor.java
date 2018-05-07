package application.monitor.model;

import application.utilities.Singleton;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MemoryMonitor implements Singleton {
    private static MemoryMonitor instance = new MemoryMonitor();

    private IntegerProperty totalMemory;
    private IntegerProperty freeMemory;

    private IntegerProperty applicationMemoryUsage;

    public static MemoryMonitor getInstance() {
        return instance;
    }

    private MemoryMonitor() {
        if(totalMemory == null) totalMemory = new SimpleIntegerProperty();
        if(freeMemory == null) freeMemory = new SimpleIntegerProperty();
        if(applicationMemoryUsage == null) applicationMemoryUsage = new SimpleIntegerProperty();
    }

    //Setters
    public void setTotalMemory(int totalMemory) {
        this.totalMemory.setValue(totalMemory);
    }

    public void setFreeMemory(int freeMemory) {
        this.freeMemory.setValue(freeMemory);
    }

    public void setApplicationMemoryUsage(int applicationMemoryUsage) {
        this.applicationMemoryUsage.setValue(applicationMemoryUsage);
    }

    //Getters
    public int getTotalMemory() {
        return totalMemory.get();
    }

    public int getFreeMemory() {
        return freeMemory.get();
    }

    public int getApplicationMemoryUsage() {
        return applicationMemoryUsage.get();
    }

    //Properties
    public IntegerProperty applicationMemoryUsageProperty() {
        return applicationMemoryUsage;
    }

    public IntegerProperty freeMemoryProperty() {
        return freeMemory;
    }

    public IntegerProperty totalMemoryProperty() {
        return totalMemory;
    }
}
