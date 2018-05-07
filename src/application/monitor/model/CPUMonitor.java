package application.monitor.model;

import application.utilities.Singleton;
import javafx.beans.property.*;

public class CPUMonitor implements Singleton {
    private static CPUMonitor instance = new CPUMonitor();

    private IntegerProperty applicationCPUPercentageUtilization;
    private IntegerProperty systemCPUPercentageUtilization;
    private IntegerProperty runningProcesses;
    private IntegerProperty runningThreads;
    private DoubleProperty rangeOfFrequencies;
    private DoubleProperty minimumFrequency;
    private DoubleProperty maximumFrequency;
    private DoubleProperty currentFrequency;
    private IntegerProperty numberOfCores;
    private StringProperty CPUVendor;
    private StringProperty upTime;

    public static CPUMonitor getInstance() {
        return instance;
    }

    private CPUMonitor() {
        if(applicationCPUPercentageUtilization == null) applicationCPUPercentageUtilization = new SimpleIntegerProperty();
        if(systemCPUPercentageUtilization == null) systemCPUPercentageUtilization = new SimpleIntegerProperty();
        if(runningProcesses == null) runningProcesses = new SimpleIntegerProperty();
        if(runningThreads == null) runningThreads = new SimpleIntegerProperty();
        if(rangeOfFrequencies == null) rangeOfFrequencies = new SimpleDoubleProperty();
        if(currentFrequency == null) currentFrequency = new SimpleDoubleProperty();
        if(minimumFrequency == null) minimumFrequency = new SimpleDoubleProperty();
        if(maximumFrequency == null) maximumFrequency = new SimpleDoubleProperty();
        if(numberOfCores == null) numberOfCores = new SimpleIntegerProperty();
        if(CPUVendor == null) CPUVendor = new SimpleStringProperty();
        if(upTime == null) upTime = new SimpleStringProperty();
    }

    //Setters
    public void setApplicationCPUPercentageUtilization(int applicationCPUPercentageUtilization) {
        this.applicationCPUPercentageUtilization.setValue(applicationCPUPercentageUtilization);
    }

    public void setSystemCPUPercentageUtilization(int SystemCPUPercentageUtilization) {
        this.systemCPUPercentageUtilization.setValue(SystemCPUPercentageUtilization);
    }

    public void setRunningProcesses(int runningProcesses) {
        this.runningProcesses.setValue(runningProcesses);
    }

    public void setRunningThreads(int runningThreads) {
        this.runningThreads.set(runningThreads);
    }

    public void setRangeOfFrequencies(double rangeOfFrequencies) {
        this.rangeOfFrequencies.set(rangeOfFrequencies);
    }

    public void setMaximumFrequency(double maximumFrequency) {
        this.maximumFrequency.setValue(maximumFrequency);
    }

    public void setMinimumFrequency(double minimumFrequency) {
        this.minimumFrequency.setValue(minimumFrequency);
    }

    public void setCPUVendor(String CPUVendor) {
        this.CPUVendor.setValue(CPUVendor);
    }

    public void setNumberOfCores(int numberOfCores) {
        this.numberOfCores.setValue(numberOfCores);
    }

    public void setCurrentFrequency(double currentFrequency) {
        this.currentFrequency.setValue(currentFrequency);
    }

    public void setUpTime(String upTime) {
        this.upTime.set(upTime);
    }

    //Getters
    public int getApplicationCPUPercentageUtilization() {
        return applicationCPUPercentageUtilization.get();
    }

    public int getSystemCPUPercentageUtilization() {
        return systemCPUPercentageUtilization.get();
    }

    public double getRangeOfFrequencies() {
        return rangeOfFrequencies.get();
    }

    public double getMaximumFrequency() {
        return maximumFrequency.get();
    }

    public double getMinimumFrequency() {
        return minimumFrequency.get();
    }

    public int getRunningProcesses() {
        return runningProcesses.get();
    }

    public int getRunningThreads() {
        return runningThreads.get();
    }

    public String getCPUVendor() {
        return CPUVendor.get();
    }

    public int getNumberOfCores() {
        return numberOfCores.get();
    }

    public double getCurrentFrequency() {
        return currentFrequency.get();
    }

    //Properties
    public StringProperty CPUVendorProperty() {
        return CPUVendor;
    }

    public DoubleProperty rangeOfFrequenciesProperty() {
        return rangeOfFrequencies;
    }

    public IntegerProperty applicationCPUPercentageUtilizationProperty() {
        return applicationCPUPercentageUtilization;
    }

    public IntegerProperty runningProcessesProperty() {
        return runningProcesses;
    }

    public IntegerProperty runningThreadsProperty() {
        return runningThreads;
    }

    public DoubleProperty currentFrequencyProperty() {
        return currentFrequency;
    }

    public DoubleProperty maximumFrequencyProperty() {
        return maximumFrequency;
    }

    public DoubleProperty minimumFrequencyProperty() {
        return minimumFrequency;
    }

    public IntegerProperty numberOfCoresProperty() {
        return numberOfCores;
    }

    public IntegerProperty systemCPUPercentageUtilizationProperty() {
        return systemCPUPercentageUtilization;
    }

    public StringProperty upTimeProperty() {
        return upTime;
    }
}
