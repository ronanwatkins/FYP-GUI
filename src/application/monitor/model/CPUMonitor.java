package application.monitor.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CPUMonitor {
    private static CPUMonitor instance = new CPUMonitor();

    private IntegerProperty applicationCPUPercentageUtilization;
    private IntegerProperty systemCPUPercentageUtilization;
    private IntegerProperty runningProcesses;
    private IntegerProperty runningThreads;
    private IntegerProperty rangeOfFrequencies;
    private IntegerProperty minimumFrequency;
    private IntegerProperty maximumFrequency;
    private IntegerProperty currentFrequency;
    private IntegerProperty numberOfCores;
    private StringProperty CPUVendor;

    public static CPUMonitor getInstance() {
        return instance;
    }

    private CPUMonitor() {
        if(applicationCPUPercentageUtilization == null) applicationCPUPercentageUtilization = new SimpleIntegerProperty();
        if(systemCPUPercentageUtilization == null) systemCPUPercentageUtilization = new SimpleIntegerProperty();
        if(runningProcesses == null) runningProcesses = new SimpleIntegerProperty();
        if(runningThreads == null) runningThreads = new SimpleIntegerProperty();
        if(rangeOfFrequencies == null) rangeOfFrequencies = new SimpleIntegerProperty();
        if(currentFrequency == null) currentFrequency = new SimpleIntegerProperty();
        if(minimumFrequency == null) minimumFrequency = new SimpleIntegerProperty();
        if(maximumFrequency == null) maximumFrequency = new SimpleIntegerProperty();
        if(numberOfCores == null) numberOfCores = new SimpleIntegerProperty();
        if(CPUVendor == null) CPUVendor = new SimpleStringProperty();
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

    public void setRangeOfFrequencies(int rangeOfFrequencies) {
        this.rangeOfFrequencies.set(rangeOfFrequencies);
    }

    public void setMaximumFrequency(int maximumFrequency) {
        this.maximumFrequency.setValue(maximumFrequency);
    }

    public void setMinimumFrequency(int minimumFrequency) {
        this.minimumFrequency.setValue(minimumFrequency);
    }

    public void setCPUVendor(String CPUVendor) {
        this.CPUVendor.setValue(CPUVendor);
    }

    public void setNumberOfCores(int numberOfCores) {
        this.numberOfCores.setValue(numberOfCores);
    }

    public void setCurrentFrequency(int currentFrequency) {
        this.currentFrequency.setValue(currentFrequency);
    }

    //Getters
    public int getApplicationCPUPercentageUtilization() {
        return applicationCPUPercentageUtilization.get();
    }

    public int getSystemCPUPercentageUtilization() {
        return systemCPUPercentageUtilization.get();
    }

    public int getRangeOfFrequencies() {
        return rangeOfFrequencies.get();
    }

    public int getMaximumFrequency() {
        return maximumFrequency.get();
    }

    public int getMinimumFrequency() {
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

    public int getCurrentFrequency() {
        return currentFrequency.get();
    }

    //Properties
    public StringProperty CPUVendorProperty() {
        return CPUVendor;
    }

    public IntegerProperty rangeOfFrequenciesProperty() {
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

    public IntegerProperty currentFrequencyProperty() {
        return currentFrequency;
    }

    public IntegerProperty maximumFrequencyProperty() {
        return maximumFrequency;
    }

    public IntegerProperty minimumFrequencyProperty() {
        return minimumFrequency;
    }

    public IntegerProperty numberOfCoresProperty() {
        return numberOfCores;
    }

    public IntegerProperty systemCPUPercentageUtilizationProperty() {
        return systemCPUPercentageUtilization;
    }
}
