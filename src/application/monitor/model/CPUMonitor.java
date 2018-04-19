package application.monitor.model;

import java.util.concurrent.atomic.AtomicInteger;

public class CPUMonitor {
    private static CPUMonitor instance = new CPUMonitor();

    private AtomicInteger CPUPercentage;
    private String CPUVendor;
    private int numberOfCores;

    public static CPUMonitor getInstance() {
        return instance;
    }

    private CPUMonitor() {
        if(CPUPercentage == null)
            CPUPercentage = new AtomicInteger();
    }

    //Setters
    public void setCPUPercentage(int CPUPercentage) {
        this.CPUPercentage.set(CPUPercentage);
    }

    public void setCPUVendor(String CPUVendor) {
        this.CPUVendor = CPUVendor;
    }

    public void setNumberOfCores(int numberOfCores) {
        this.numberOfCores = numberOfCores;
    }

    //Getters
    public int getCPUPercentage() {
        return CPUPercentage.get();
    }

    public String getCPUVendor() {
        return CPUVendor;
    }

    public int getNumberOfCores() {
        return numberOfCores;
    }
}
