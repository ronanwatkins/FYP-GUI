package application.monitor.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

public class NetworkMonitor {
    private static NetworkMonitor instance = new NetworkMonitor();

    private LongProperty applicationReceivedBytes;
    private LongProperty applicationSentBytes;
    private LongProperty applicationReceivedPackets;
    private LongProperty applicationSentPackets;
    private DoubleProperty applicationSentKBps;
    private DoubleProperty applicationReceivedKBps;

    private LongProperty systemReceivedBytes;
    private LongProperty systemSentBytes;
    private LongProperty systemReceivedPackets;
    private LongProperty systemSentPackets;
    private DoubleProperty systemSentKBps;
    private DoubleProperty systemReceivedKBps;

    public static NetworkMonitor getInstance() {
        return instance;
    }

    private NetworkMonitor() {
        if(applicationReceivedBytes == null) applicationReceivedBytes = new SimpleLongProperty();
        if(applicationSentBytes == null) applicationSentBytes = new SimpleLongProperty();
        if(applicationReceivedPackets == null) applicationReceivedPackets = new SimpleLongProperty();
        if(applicationSentPackets == null) applicationSentPackets = new SimpleLongProperty();
        if(applicationSentKBps == null) applicationSentKBps = new SimpleDoubleProperty();
        if(applicationReceivedKBps == null) applicationReceivedKBps = new SimpleDoubleProperty();

        if(systemReceivedBytes == null) systemReceivedBytes = new SimpleLongProperty();
        if(systemSentBytes == null) systemSentBytes = new SimpleLongProperty();
        if(systemReceivedPackets == null) systemReceivedPackets = new SimpleLongProperty();
        if(systemSentPackets == null) systemSentPackets = new SimpleLongProperty();
        if(systemSentKBps == null) systemSentKBps = new SimpleDoubleProperty();
        if(systemReceivedKBps == null) systemReceivedKBps = new SimpleDoubleProperty();
    }

    //Setters
    public void setApplicationReceivedBytes(long applicationReceivedBytes) {
        this.applicationReceivedBytes.set(applicationReceivedBytes);
    }

    public void setApplicationSentBytes(long applicationSentBytes) {
        this.applicationSentBytes.set(applicationSentBytes);
    }

    public void setApplicationReceivedPackets(long applicationReceivedPackets) {
        this.applicationReceivedPackets.set(applicationReceivedPackets);
    }

    public void setApplicationSentPackets(long applicationSentPackets) {
        this.applicationSentPackets.set(applicationSentPackets);
    }

    public void setApplicationSentKBps(double applicationSentKBps) {
        this.applicationSentKBps.set(applicationSentKBps);
    }

    public void setApplicationReceivedKBps(double applicationReceivedKBps) {
        this.applicationReceivedKBps.set(applicationReceivedKBps);
    }

    public void setSystemReceivedBytes(long systemReceivedBytes) {
        this.systemReceivedBytes.set(systemReceivedBytes);
    }

    public void setSystemSentBytes(long systemSentBytes) {
        this.systemSentBytes.set(systemSentBytes);
    }

    public void setSystemReceivedPackets(long systemReceivedPackets) {
        this.systemReceivedPackets.set(systemReceivedPackets);
    }

    public void setSystemSentPackets(long systemSentPackets) {
        this.systemSentPackets.set(systemSentPackets);
    }

    public void setSystemSentKBps(double systemSentKBps) {
        this.systemSentKBps.set(systemSentKBps);
    }

    public void setSystemReceivedKBps(double systemReceivedKBps) {
        this.systemReceivedKBps.set(systemReceivedKBps);
    }

    //Getters
    public long getApplicationReceivedBytes() {
        return applicationReceivedBytes.get();
    }

    public long getApplicationSentBytes() {
        return applicationSentBytes.get();
    }

    public long getApplicationReceivedPackets() {
        return applicationReceivedPackets.get();
    }

    public long getApplicationSentPackets() {
        return applicationSentPackets.get();
    }

    public double getApplicationSentKBps() {
        return applicationSentKBps.get();
    }

    public double getApplicationReceivedKBps() {
        return applicationReceivedKBps.get();
    }

    public long getSystemReceivedBytes() {
        return systemReceivedBytes.get();
    }

    public long getSystemSentBytes() {
        return systemSentBytes.get();
    }

    public long getSystemReceivedPackets() {
        return systemReceivedPackets.get();
    }

    public long getSystemSentPackets() {
        return systemSentPackets.get();
    }

    public double getSystemReceivedKBps() {
        return systemReceivedKBps.get();
    }

    public double getSystemSentKBps() {
        return systemSentKBps.get();
    }

    //Properties
    public LongProperty applicationReceivedBytesProperty() {
        return applicationReceivedBytes;
    }

    public LongProperty applicationSentBytesProperty() {
        return applicationSentBytes;
    }

    public LongProperty applicationReceivedPacketsProperty() {
        return applicationReceivedPackets;
    }

    public LongProperty applicationSentPacketsProperty() {
        return applicationSentPackets;
    }

    public DoubleProperty applicationReceivedKBpsProperty() {
        return applicationReceivedKBps;
    }

    public DoubleProperty applicationSentKBpsProperty() {
        return applicationSentKBps;
    }

    public LongProperty systemReceivedBytesProperty() {
        return systemReceivedBytes;
    }

    public LongProperty systemSentBytesProperty() {
        return systemSentBytes;
    }

    public LongProperty systemReceivedPacketsProperty() {
        return systemReceivedPackets;
    }

    public LongProperty systemSentPacketsProperty() {
        return systemSentPackets;
    }

    public DoubleProperty systemReceivedKBpsProperty() {
        return systemReceivedKBps;
    }

    public DoubleProperty systemSentKBpsProperty() {
        return systemSentKBps;
    }
}
