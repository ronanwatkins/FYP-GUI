package application.monitor.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class NetworkMonitor {
    private static NetworkMonitor instance = new NetworkMonitor();

    private IntegerProperty applicationReceivedBytes;
    private IntegerProperty applicationSentBytes;
    private IntegerProperty applicationReceivedPackets;
    private IntegerProperty applicationSentPackets;

    private IntegerProperty systemReceivedBytes;
    private IntegerProperty systemSentBytes;
    private IntegerProperty systemReceivedPackets;
    private IntegerProperty systemSentPackets;

    public static NetworkMonitor getInstance() {
        return instance;
    }

    private NetworkMonitor() {
        if(applicationReceivedBytes == null) applicationReceivedBytes = new SimpleIntegerProperty();
        if(applicationSentBytes == null) applicationSentBytes = new SimpleIntegerProperty();
        if(applicationReceivedPackets == null) applicationReceivedPackets = new SimpleIntegerProperty();
        if(applicationSentPackets == null) applicationSentPackets = new SimpleIntegerProperty();

        if(systemReceivedBytes == null) systemReceivedBytes = new SimpleIntegerProperty();
        if(systemSentBytes == null) systemSentBytes = new SimpleIntegerProperty();
        if(systemReceivedPackets == null) systemReceivedPackets = new SimpleIntegerProperty();
        if(systemSentPackets == null) systemSentPackets = new SimpleIntegerProperty();
    }

    //Setters
    public void setApplicationReceivedBytes(int applicationReceivedBytes) {
        this.applicationReceivedBytes.set(applicationReceivedBytes);
    }

    public void setApplicationSentBytes(int applicationSentBytes) {
        this.applicationSentBytes.set(applicationSentBytes);
    }

    public void setApplicationReceivedPackets(int applicationReceivedPackets) {
        this.applicationReceivedPackets.set(applicationReceivedPackets);
    }

    public void setApplicationSentPackets(int applicationSentPackets) {
        this.applicationSentPackets.set(applicationSentPackets);
    }
    public void setSystemReceivedBytes(int systemReceivedBytes) {
        this.systemReceivedBytes.set(systemReceivedBytes);
    }

    public void setSystemSentBytes(int systemSentBytes) {
        this.systemSentBytes.set(systemSentBytes);
    }

    public void setSystemReceivedPackets(int systemReceivedPackets) {
        this.systemReceivedPackets.set(systemReceivedPackets);
    }

    public void setSystemSentPackets(int systemSentPackets) {
        this.systemSentPackets.set(systemSentPackets);
    }

    //Getters
    public int getApplicationReceivedBytes() {
        return applicationReceivedBytes.get();
    }

    public int getApplicationSentBytes() {
        return applicationSentBytes.get();
    }

    public int getApplicationReceivedPackets() {
        return applicationReceivedPackets.get();
    }

    public int getApplicationSentPackets() {
        return applicationSentPackets.get();
    }

    public int getSystemReceivedBytes() {
        return systemReceivedBytes.get();
    }

    public int getSystemSentBytes() {
        return systemSentBytes.get();
    }

    public int getSystemReceivedPackets() {
        return systemReceivedPackets.get();
    }

    public int getSystemSentPackets() {
        return systemSentPackets.get();
    }

    //Properties
    public IntegerProperty applicationReceivedBytesProperty() {
        return applicationReceivedBytes;
    }

    public IntegerProperty applicationSentBytesProperty() {
        return applicationSentBytes;
    }

    public IntegerProperty applicationReceivedPacketsProperty() {
        return applicationReceivedPackets;
    }

    public IntegerProperty applicationSentPacketsProperty() {
        return applicationSentPackets;
    }

    public IntegerProperty systemReceivedBytesProperty() {
        return systemReceivedBytes;
    }

    public IntegerProperty systemSentBytesProperty() {
        return systemSentBytes;
    }

    public IntegerProperty systemReceivedPacketsProperty() {
        return systemReceivedPackets;
    }

    public IntegerProperty systemSentPacketsProperty() {
        return systemSentPackets;
    }
}
