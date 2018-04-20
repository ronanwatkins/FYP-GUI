package application.monitor;

import application.ADBUtil;
import application.device.Device;
import application.monitor.model.CPUMonitor;
import application.monitor.model.MemoryMonitor;
import application.monitor.model.NetworkMonitor;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitorService extends Thread {
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
        //Application CPU usage
        Task<Void> applicationCPUUsageTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if(device.getSelectedApplication() == null)
                    return null;

                final String newCommand = ADBUtil.getAdbPath() + " -s " + device.getName() + " shell \"top | grep " + device.getSelectedApplication().getName() + "\"";
                BufferedReader bufferedReader = getResponse(newCommand);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.isEmpty())
                        continue;

                    final String newLine = line.trim().replaceAll(" {2,}" , " ").split(" ")[2].replace("%", "");

                    try {
                        Platform.runLater(() -> cpuMonitor.setApplicationCPUPercentageUtilization(Integer.parseInt(newLine)));
                        System.out.println("Application CPU Percentage Utilization: " + cpuMonitor.getApplicationCPUPercentageUtilization());
                    } catch (NumberFormatException nfe) {
                        Log.error(nfe.getMessage(), nfe);
                    }
                }
                return null;
            }
        };
        new Thread(applicationCPUUsageTask).start();

        //System CPU usage
        Task<Void> systemCPUUsageTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final String newCommand = ADBUtil.getAdbPath() + " -s " + device.getName() + " shell \"top | grep System\"";
                BufferedReader bufferedReader = getResponse(newCommand);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.isEmpty())
                        continue;

                    final String newLine = line.trim().split(",")[1].trim().split(" ")[1].replace("%", "");

                    try {
                        Platform.runLater(() -> cpuMonitor.setSystemCPUPercentageUtilization(Integer.parseInt(newLine)));
                        System.out.println("System CPU Percentage Utilization:" + cpuMonitor.getSystemCPUPercentageUtilization());
                        System.out.println("AM I INTERRUPTED??????????" + MonitorService.this.isInterrupted());
                        System.out.println("AM I ALIVE??????????" + MonitorService.this.isAlive());
                    } catch (NumberFormatException nfe) {
                        Log.error(nfe.getMessage(), nfe);
                    }
                }
                return null;
            }
        };
        new Thread(systemCPUUsageTask).start();

        //Number of running threads
        Task<Void> runningThreadsTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final String newCommand = ADBUtil.getAdbPath() + " -s " + device.getName() + " shell \"top -t\"";
                BufferedReader bufferedReader = getResponse(newCommand);

                String line;
                int i=0;
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.startsWith("User") || line.isEmpty()) {
                        continue;
                    }

                    line = line.trim();

                    if(line.startsWith("PID")) {
                        final int runningThreads = i;
                        Platform.runLater(() -> cpuMonitor.setRunningThreads(runningThreads));
                        i=0;
                    } else
                        i++;
                }
                return null;
            }
        };
        new Thread(runningThreadsTask).start();

        ArrayList<Task<Void>> tasks = new ArrayList<>();
        tasks.add(applicationCPUUsageTask);
        tasks.add(systemCPUUsageTask);
        tasks.add(runningThreadsTask);

        try {
            runContinuously();
        } catch (InterruptedException ie) {
            for(Task<Void> task : tasks)
                task.cancel();

            System.out.println("THEY ARE ALL DEAD HAHAHAHAHAHA");
        }
    }

    private void runOnce() {
        /////MEMORY/////
        String command = "shell \"cat /proc/meminfo | grep MemTotal:\"";
        String totalMemory = getResponseLine(command).replaceAll("[a-zA-Z]" ,"").replace(":", "").trim();
        try {
            Platform.runLater(() -> memoryMonitor.setTotalMemory(Integer.parseInt(totalMemory)));
            System.out.println("Total Memory: " + memoryMonitor.getTotalMemory());
        } catch (NumberFormatException nfe) {
            Log.error(nfe.getMessage(), nfe);
        }

        /////CPU/////
        //CPU Vendor name
        final String newCommand = "shell \"cat /proc/cpuinfo | grep Hardware\"";

        Platform.runLater(() -> cpuMonitor.setCPUVendor(getResponseLine(newCommand).replace("Hardware", "").replace(":", "").trim()));
        System.out.println("Vendor: " + cpuMonitor.getCPUVendor());

        //Number of cores
        int numberOfCores = 8;
        command = "shell \"cat /proc/cpuinfo | grep 'CPU architecture'\"";
        try {
            numberOfCores = Integer.parseInt(getResponseLine(command).replaceAll("[a-zA-Z]" ,"").replace(":", "").trim());
        } catch (NumberFormatException nfe) {
            Log.error(nfe.getMessage(), nfe);
        }
        final int newNumberOfCores = numberOfCores;
        Platform.runLater(() -> cpuMonitor.setNumberOfCores(newNumberOfCores));
        System.out.println("Number of cores: " + cpuMonitor.getNumberOfCores());
    }

    private void runContinuously() throws InterruptedException {
        String command;

        //Run continuously
        while (true) {
            /////MEMORY/////
            //Total system free memory
            try {

                command = "shell \"cat /proc/meminfo | grep MemFree:\"";
                String freeMemory = getResponseLine(command).replaceAll("[a-zA-Z]", "").replace(":", "").trim();

                //Total application memory usage
                String applicationMemoryUsage = "0";
                if (device.getSelectedApplication() != null) {
                    command = "shell \"dumpsys meminfo " + device.getSelectedApplication().getName() + " | grep TOTAL\"";
                    Log.info("Command: " + command);
                    String response = getResponseLine(command);
                    Log.info("response 1: " + response);
                    response = response != null ? response.trim().replaceAll(" {2,}", " ") : "0 0";
                    Log.info("Response: " + response);
                    applicationMemoryUsage = response.split(" ")[1];
                }
                final String newApplicationMemoryUsage = applicationMemoryUsage;

                /////CPU/////
                //Number of running processes
                command = "shell ps | /system/xbin/busybox wc -l";
                String runningProcesses = getResponseLine(command).trim();
                Log.info("response: " + runningProcesses);

                command = "shell cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
                String currentFrequency = getResponseLine(command).trim();

                /////NETWORK/////
                String receivedBytes = "0";
                String receivedPackets = "0";
                String sentBytes = "0";
                String sentPackets = "0";

                if (device.getSelectedApplication() != null) {
                    command = "shell cat /proc/net/xt_qtaguid/stats | grep " + device.getSelectedApplication().getUserID() + " | grep wlan0";
                    Log.info("Command: " + command);
                    String response = ADBUtil.consoleCommand(command).split("\n")[1];//.split("\n")[0];
                    String[] split = response.split(" ");
                    Log.info("Response: " + response);
                    receivedBytes = split[5];
                    receivedPackets = split[6];
                    sentBytes = split[7];
                    sentPackets = split[8];
                }

                try {
                    Platform.runLater(() -> memoryMonitor.setFreeMemory(Integer.parseInt(freeMemory)));
                    System.out.println("Free Memory: " + memoryMonitor.getTotalMemory());

                    Platform.runLater(() -> memoryMonitor.setApplicationMemoryUsage(Integer.parseInt(newApplicationMemoryUsage)));
                    System.out.println("Total application memory usage " + memoryMonitor.getApplicationMemoryUsage());

                    Platform.runLater(() -> cpuMonitor.setRunningProcesses(Integer.parseInt(runningProcesses)));
                    System.out.println("Running processes: " + cpuMonitor.getRunningProcesses());

                    Platform.runLater(() -> cpuMonitor.setCurrentFrequency(Integer.parseInt(currentFrequency)));
                    System.out.println("Current frequency: " + cpuMonitor.getCurrentFrequency());

                    final String newReceivedBytes = receivedBytes;
                    Platform.runLater(() -> networkMonitor.setApplicationReceivedBytes(Integer.parseInt(newReceivedBytes)));
                    System.out.println("Received bytes: " + networkMonitor.getApplicationReceivedBytes());

                    final String newReceivedPackets = receivedPackets;
                    Platform.runLater(() -> networkMonitor.setApplicationReceivedPackets(Integer.parseInt(newReceivedPackets)));
                    System.out.println("Received packets: " + networkMonitor.getApplicationReceivedPackets());

                    final String newSentBytes = sentBytes;
                    Platform.runLater(() -> networkMonitor.setApplicationSentBytes(Integer.parseInt(newSentBytes)));
                    System.out.println("Sent bytes: " + networkMonitor.getApplicationSentBytes());

                    final String newSentPackets = sentPackets;
                    Platform.runLater(() -> networkMonitor.setApplicationSentPackets(Integer.parseInt(newSentPackets)));
                    System.out.println("Sent packets: " + networkMonitor.getApplicationSentPackets());

                } catch (NumberFormatException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                }



            } catch (Exception ee) { //Catch any exception that may occur to avoid loop breaking
                Log.error(ee.getMessage(), ee);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Log.error(ie.getMessage(), ie);
                throw new InterruptedException();
            }
        }
    }

    private BufferedReader getResponse(String input) throws IOException {
        Process process = Runtime.getRuntime().exec(input);
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    private String getResponseLine(String input) {
        Log.info("input: " + input);
        String response = "";

        try {
            Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + device.getName() + " " + input);
            response = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
        } catch (IOException ioe) {
            Log.error(ioe.getMessage(), ioe);
        }

        return response;
    }
}
