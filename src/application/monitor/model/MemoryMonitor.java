package application.monitor.model;

import java.util.concurrent.atomic.AtomicInteger;

public class MemoryMonitor {
    private static MemoryMonitor instance = new MemoryMonitor();

    private int totalMemory;
    private AtomicInteger freeMemory;

    public static MemoryMonitor getInstance() {
        return instance;
    }

    private MemoryMonitor() {
        if(freeMemory == null)
            freeMemory = new AtomicInteger();
    }

    //Setters
    public void setTotalMemory(int totalMemory) {
        this.totalMemory = totalMemory;
    }

    public void setFreeMemory(int freeMemory) {
        this.freeMemory.set(freeMemory);
    }

    //Getters
    public int getTotalMemory() {
        return totalMemory;
    }

    public int getFreeMemory() {
        return freeMemory.get();
    }
}
