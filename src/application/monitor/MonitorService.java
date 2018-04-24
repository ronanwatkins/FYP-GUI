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
    private ArrayList<Task<Void>> tasks = new ArrayList<>();
    private AtomicBoolean isRunning;

    private String startOfCommand;

    private boolean isFirstRun = true;
    private long lastSentKiloBytes;
    private long lastReceivedKiloBytes;

    public static MonitorService getInstance() {
        return instance;
    }

    private MonitorService() {
    }

    @Override
    public void run() {
        startOfCommand = ADBUtil.getAdbPath() + " -s " + device.getName();

        runOnce("shell \"cat /proc/meminfo | grep MemTotal:\"",
                "shell \"cat /proc/cpuinfo | grep Hardware\"",
                "shell cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies",
                "shell \"cat /proc/cpuinfo | grep 'CPU architecture'\"");

        if(device.getSelectedApplication() != null) {
            tasks.add(new CPUUsageTask(startOfCommand + " shell \"top | grep " + device.getSelectedApplication().getName() + "\"", TaskType.APPLICATION));
            tasks.add(new memoryUsageTask("shell \"dumpsys meminfo " + device.getSelectedApplication().getName() + " | grep TOTAL\"", TaskType.APPLICATION));
         //   tasks.add(new updateNetworkStatsTask("shell cat /proc/net/xt_qtaguid/stats | grep " + device.getSelectedApplication().getUserID() + " | grep wlan0", TaskType.APPLICATION));
        }

        tasks.add(new memoryUsageTask("shell \"cat /proc/meminfo | grep -E 'MemFree:|Buffers:|Cached:|SwapFree:'\"", TaskType.SYSTEM));
        tasks.add(new CPUUsageTask(startOfCommand + " -s " + device.getName() + " shell \"top | grep System\"", TaskType.SYSTEM));
        tasks.add(new runningThreadsTask(startOfCommand + " -s " + device.getName() + " shell \"top -t\""));
        tasks.add(new updateCPUStatsTask("shell ps | /system/xbin/busybox wc -l",
                "shell cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"));
        tasks.add(new updateNetworkStatsTask("shell cat /proc/net/dev | grep wlan0", TaskType.SYSTEM));

        for(Task<Void> task : tasks)
            new Thread(task).start();
    }

    public boolean cancel() {
        boolean result = true;

        Log.info("Cancelling all tasks...");
        for(Task<Void> task : tasks)
            if(!task.cancel()) result = false;

        Log.info("All tasks " + (result ? "cancelled" : "not cancelled"));

        return result;
    }

    private void runOnce(String totalMemoryCommand, String CPUVendorCommand, String rangeOfFrequenciesCommand, String numberOfCoresCommand) {
        try {
            /////MEMORY/////
            String totalMemory = getResponseLine(totalMemoryCommand).replaceAll("[a-zA-Z]", "").replace(":", "").trim();
            Platform.runLater(() -> memoryMonitor.setTotalMemory(Integer.parseInt(totalMemory)));

            /////CPU/////
            //CPU Range of frequencies
            String[] availableFrequencies = getResponseLine(rangeOfFrequenciesCommand).trim().split(" ");
            final double minimumFrequency = Double.parseDouble(availableFrequencies[0])/1000000;
            final double maximumFrequency = Double.parseDouble(availableFrequencies[availableFrequencies.length-1])/1000000;
            final double rangeOfFrequencies = maximumFrequency - minimumFrequency;
            Platform.runLater(() -> {
                cpuMonitor.setMinimumFrequency(minimumFrequency);
                cpuMonitor.setMaximumFrequency(maximumFrequency);
                cpuMonitor.setRangeOfFrequencies(rangeOfFrequencies);
            });

            //CPU Vendor name
            String CPUVendor = getResponseLine(CPUVendorCommand).replace("Hardware", "").replace(":", "").trim();
            CPUVendor = CPUVendor.split(",")[0].replace(",", "").trim();
            final String newCPUVendor = CPUVendor;
            Platform.runLater(() -> cpuMonitor.setCPUVendor(newCPUVendor));

            //Number of cores
            String numberOfCores = getResponseLine(numberOfCoresCommand).replaceAll("[a-zA-Z]", "").replace(":", "").trim();
            Platform.runLater(() -> cpuMonitor.setNumberOfCores(Integer.parseInt(numberOfCores)));
        } catch (NullPointerException | NumberFormatException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private class runningThreadsTask extends Task<Void> {
        private String command;

        private runningThreadsTask(String command) {
            this.command = command;
        }

        @Override
        protected Void call() throws Exception {
            BufferedReader bufferedReader = getResponse(command);

            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (isCancelled())
                    break;

                if (line.startsWith("User") || line.isEmpty()) {
                    continue;
                }

                line = line.trim();

                if (line.startsWith("PID")) {
                    final int runningThreads = i;
                    Platform.runLater(() -> cpuMonitor.setRunningThreads(runningThreads));
                    i = 0;
                } else
                    i++;
            }
            return null;
        }
    }

    private class CPUUsageTask extends Task<Void> {
        protected String command;
        protected TaskType taskType;

        private CPUUsageTask(String command, TaskType taskType) {
            this.command = command;
            this.taskType = taskType;
        }

        @Override
        protected Void call() throws Exception {
            BufferedReader bufferedReader = getResponse(command);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(isCancelled())
                    break;

                if(line.isEmpty())
                    continue;

                try {
                    switch (taskType) {
                        case APPLICATION:
                            final String newLine = line.trim().replaceAll(" {2,}" , " ").split(" ")[2].replace("%", "");
                            Platform.runLater(() -> cpuMonitor.setApplicationCPUPercentageUtilization(Integer.parseInt(newLine)));
                            //System.out.println("Application CPU Percentage Utilization: " + cpuMonitor.getApplicationCPUPercentageUtilization());
                            break;
                        case SYSTEM:
                            String s = line.trim().split(",")[1].trim().split(" ")[1].replace("%", "");
                            Platform.runLater(() -> cpuMonitor.setSystemCPUPercentageUtilization(Integer.parseInt(s)));
                            //System.out.println("System CPU Percentage Utilization: " + cpuMonitor.getSystemCPUPercentageUtilization());
                            break;
                    }
                } catch (NumberFormatException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                }
            }
            return null;
        }
    }

    private class updateNetworkStatsTask extends CPUUsageTask {

        private updateNetworkStatsTask(String command, TaskType taskType) {
            super(command, taskType);
            this.command = command;
            this.taskType = taskType;
        }

        @Override
        protected Void call() throws Exception {
            while(true) {
                if(isCancelled())
                    return null;

                try {
                    String receivedBytes = "0";
                    String receivedPackets = "0";
                    String sentBytes = "0";
                    String sentPackets = "0";

                    String response = "";

                    switch (taskType) {
                        case APPLICATION:
                            response = ADBUtil.consoleCommand(command).split("\n")[1];//.split("\n")[0];
                            String[] split = response.split(" ");

                            receivedBytes = split[5];
                            receivedPackets = split[6];
                            sentBytes = split[7];
                            sentPackets = split[8];
                            break;
                        case SYSTEM:
                            response = getResponseLine(command).replaceAll(" {2,}", " ").trim();
                            split = response.split(" ");

                            receivedBytes = split[1];
                            receivedPackets = split[2];
                            sentBytes = split[9];
                            sentPackets = split[10];
                            break;
                    }

                    long sentKiloBytes = ((Long.parseLong(sentBytes)))/1000;
                    double sentKBps = (isFirstRun ? 0 : ((double)((sentKiloBytes - lastSentKiloBytes)/1000)));
                    lastSentKiloBytes = sentKiloBytes;

                    long receivedKiloBytes = (Long.parseLong(receivedBytes))/1000;
                    double receivedKBps = (isFirstRun ? 0 : ((double)((receivedKiloBytes - lastReceivedKiloBytes)/1000)));
                    lastReceivedKiloBytes = receivedKiloBytes;

                    if(isFirstRun)
                        isFirstRun = false;

                    System.out.println(taskType + " receivedKBps: " + receivedKBps);
                    System.out.println(taskType + " lastReceivedKiloBytes: " + lastReceivedKiloBytes);
                    System.out.println(taskType + " sentKBps: " + sentKBps);
                    System.out.println(taskType + " lastSentKiloBytes: " + lastSentKiloBytes);

                    final String newReceivedBytes = receivedBytes;
                    final String newReceivedPackets = receivedPackets;
                    final String newSentBytes = sentBytes;
                    final String newSentPackets = sentPackets;

                    switch (taskType) {
                        case APPLICATION:
                            Platform.runLater(() -> {
                                networkMonitor.setApplicationReceivedBytes(Integer.parseInt(newReceivedBytes));
                                networkMonitor.setApplicationReceivedPackets(Integer.parseInt(newReceivedPackets));
                                networkMonitor.setApplicationReceivedKBps(receivedKBps);
                                networkMonitor.setApplicationSentBytes(Integer.parseInt(newSentBytes));
                                networkMonitor.setApplicationSentPackets(Integer.parseInt(newSentPackets));
                                networkMonitor.setApplicationSentKBps(sentKBps);
                            });
                            break;
                        case SYSTEM:
                            Platform.runLater(() -> {
                                networkMonitor.setSystemReceivedBytes(Integer.parseInt(newReceivedBytes));
                                networkMonitor.setSystemReceivedPackets(Integer.parseInt(newReceivedPackets));
                                networkMonitor.setSystemReceivedKBps(receivedKBps);
                                networkMonitor.setSystemSentBytes(Integer.parseInt(newSentBytes));
                                networkMonitor.setSystemSentPackets(Integer.parseInt(newSentPackets));
                                networkMonitor.setSystemSentKBps(sentKBps);
                            });
                            break;
                    }

                } catch (Exception ee) {
                    Log.error(ee.getMessage(), ee);
                    return null;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Log.error(ie.getMessage(), ie);
                    throw new InterruptedException();
                }
            }
        }
    }

    private class memoryUsageTask extends CPUUsageTask {

        private memoryUsageTask(String command, TaskType taskType) {
            super(command, taskType);
            this.command = command;
            this.taskType = taskType;
        }

        @Override
        protected Void call() throws Exception {
            while (true) {
                if(isCancelled())
                    return null;

                try {
                    switch (taskType) {
                        case SYSTEM:
                            String memory = ADBUtil.consoleCommand(command).replaceAll("[a-zA-Z]", "").replace(":", "").trim();

                            int freeMemory = 0;

                            String split[] = memory.split("\n");
                            for(String str : split)
                                freeMemory += Integer.parseInt(str.trim());

                            final int newFreeMemory = freeMemory;

                            Platform.runLater(() -> memoryMonitor.setFreeMemory(newFreeMemory));
                            //System.out.println("Free Memory: " + memoryMonitor.getTotalMemory());
                            break;
                        case APPLICATION:
                            //Total application memory usage
                            String applicationMemoryUsage = "0";
                            if (device.getSelectedApplication() != null) {
                                String response = getResponseLine(command);
                                //Log.info("response 1: " + response);
                                response = response != null ? response.trim().replaceAll(" {2,}", " ") : "0 0";
                                //Log.info("Response: " + response);
                                applicationMemoryUsage = response.split(" ")[1];
                            }
                            final String newApplicationMemoryUsage = applicationMemoryUsage;

                            Platform.runLater(() -> memoryMonitor.setApplicationMemoryUsage(Integer.parseInt(newApplicationMemoryUsage)));
                            //System.out.println("Total application memory usage " + memoryMonitor.getApplicationMemoryUsage());
                            break;
                    }
                } catch (NumberFormatException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Log.error(ie.getMessage(), ie);
                    throw new InterruptedException();
                }
            }
        }
    }

    private class updateCPUStatsTask extends Task<Void> {
        private String runningProcessesCommand;
        private String currentFrequencyCommand;

        private updateCPUStatsTask(String runningProcessesCommand, String currentFrequencyCommand) {
            this.runningProcessesCommand = runningProcessesCommand;
            this.currentFrequencyCommand = currentFrequencyCommand;
        }

        @Override
        protected Void call() throws Exception {
            while (true) {
                if(isCancelled())
                    return null;

                String runningProcesses = getResponseLine(runningProcessesCommand).trim();
                String currentFrequency = getResponseLine(currentFrequencyCommand).trim();

                try {
                    Platform.runLater(() -> {
                        cpuMonitor.setRunningProcesses(Integer.parseInt(runningProcesses));
                        cpuMonitor.setCurrentFrequency(Double.parseDouble(currentFrequency)/1000000);
                    });
                } catch (NumberFormatException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Log.error(ie.getMessage(), ie);
                    throw new InterruptedException();
                }
            }
        }
    }

    private BufferedReader getResponse(String input) throws IOException {
        Process process = Runtime.getRuntime().exec(input);
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    private String getResponseLine(String input) {
        //Log.info("input: " + input);
        //Log.info("full command: " + startOfCommand + " " + input);
        String response = "";

        try {
            Process process = Runtime.getRuntime().exec(startOfCommand + " " + input);
            response = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
        } catch (IOException ioe) {
            Log.error(ioe.getMessage(), ioe);
        }

        return response;
    }

    private enum TaskType {
        APPLICATION,
        SYSTEM
    }
}
