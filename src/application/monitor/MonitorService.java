package application.monitor;

import application.ADBUtil;
import application.device.Device;
import application.monitor.model.CPUMonitor;
import application.monitor.model.MemoryMonitor;
import application.monitor.model.NetworkMonitor;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitorService implements Runnable {
    private static final Logger Log = Logger.getLogger(MonitorService.class.getName());

    private Device device = Device.getInstance();
    private CPUMonitor cpuMonitor = CPUMonitor.getInstance();
    private NetworkMonitor networkMonitor = NetworkMonitor.getInstance();
    private MemoryMonitor memoryMonitor = MemoryMonitor.getInstance();

    private static MonitorService instance = new MonitorService();
    private AtomicBoolean isRunning;

    public static MonitorService getInstance() {
        return instance;
    }

    private MonitorService() {
    }

    @Override
    public void run() {
        /////MEMORY/////
        String totalMemory = getResponseLine("shell \"cat /proc/meminfo | grep MemTotal:\"").replaceAll("[a-zA-Z]" ,"").replace(":", "").trim();
        try {
            memoryMonitor.setTotalMemory(Integer.parseInt(totalMemory));
            System.out.println("Total Memory: " + memoryMonitor.getTotalMemory());
        } catch (NumberFormatException nfe) {
            Log.error(nfe.getMessage(), nfe);
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + device.getName() + " shell \"vmstat -r 0\"");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.isEmpty())
                        continue;

                    line = line.trim();
                    if(line.startsWith("procs") || line.startsWith("r"))
                        continue;

                    line = line.replaceAll(" {2,3}" , " ").split(" ")[2];

                    try {
                        memoryMonitor.setFreeMemory(Integer.parseInt(line));
                        System.out.println("Free Memory: " + memoryMonitor.getFreeMemory());
                    } catch (NumberFormatException nfe) {
                        Log.error(nfe.getMessage(), nfe);
                    }
                }
                return null;
            }
        };
        new Thread(task).start();

        /////CPU/////
        cpuMonitor.setCPUVendor(getResponseLine("shell \"cat /proc/cpuinfo | grep Hardware\"").replace("Hardware", "").replace(":", "").trim());
        System.out.println("Vendor: " + cpuMonitor.getCPUVendor());
        int numberOfCores = 8;
        try {
            numberOfCores = Integer.parseInt(getResponseLine("shell \"cat /proc/cpuinfo | grep 'CPU architecture'\"").replaceAll("[a-zA-Z]" ,"").replace(":", "").trim());
        } catch (NumberFormatException nfe) {
            Log.error(nfe.getMessage(), nfe);
        }
        cpuMonitor.setNumberOfCores(numberOfCores);
        System.out.println("Number of cores: " + cpuMonitor.getNumberOfCores());

//        while (isRunning.get()) {

  //      }
    }

    private String getResponseLine(String input) {
        String response = "";

        try {
            Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + device.getName() + " " + input);
            response =  new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
        } catch (IOException ioe) {
            Log.error(ioe.getMessage(), ioe);
        }

        return response;
    }
}
