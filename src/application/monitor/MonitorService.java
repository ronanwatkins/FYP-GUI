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
                "shell \"cat /proc/cpuinfo | grep 'CPU architecture'\"");

        if(device.getSelectedApplication() != null) {
            tasks.add(new CPUUsageTask(startOfCommand + " shell \"top | grep " + device.getSelectedApplication().getName() + "\"", TaskType.APPLICATION_CPU_USAGE));
            tasks.add(new memoryUsageTask("shell \"cat /proc/meminfo | grep MemFree:\"",
                    "shell \"dumpsys meminfo " + device.getSelectedApplication().getName() + " | grep TOTAL\""));
            tasks.add(new updateNetworkStatsTask("shell cat /proc/net/xt_qtaguid/stats | grep " + device.getSelectedApplication().getUserID() + " | grep wlan0"));
        }

        tasks.add(new CPUUsageTask(startOfCommand + " -s " + device.getName() + " shell \"top | grep System\"", TaskType.SYSTEM_CPU_USAGE));
        tasks.add(new runningThreadsTask(startOfCommand + " -s " + device.getName() + " shell \"top -t\""));
        tasks.add(new updateCPUStatsTask("shell ps | /system/xbin/busybox wc -l",
                "shell cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"));

        for(Task<Void> task : tasks)
            new Thread(task).start();
    }

    public boolean cancel() {
        boolean result = true;

        Log.info("Cancelling all tasks...");
        for(Task<Void> task : tasks)
            result = task.cancel();

        Log.info("All tasks " + (result ? "cancelled" : "not cancelled"));

        return result;
    }

    private void runOnce(String totalMemoryCommand, String CPUVendorCommand, String numberOfCoresCommand) {
        try {
            /////MEMORY/////
            String totalMemory = getResponseLine(totalMemoryCommand).replaceAll("[a-zA-Z]", "").replace(":", "").trim();
            Platform.runLater(() -> memoryMonitor.setTotalMemory(Integer.parseInt(totalMemory)));

            /////CPU/////
            //CPU Vendor name
            String CPUVendor = getResponseLine(CPUVendorCommand).replace("Hardware", "").replace(":", "").trim();
            Platform.runLater(() -> cpuMonitor.setCPUVendor(CPUVendor));

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
        String command;
        TaskType taskType;

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
                        case APPLICATION_CPU_USAGE:
                            final String newLine = line.trim().replaceAll(" {2,}" , " ").split(" ")[2].replace("%", "");
                            Platform.runLater(() -> cpuMonitor.setApplicationCPUPercentageUtilization(Integer.parseInt(newLine)));
                            System.out.println("Application CPU Percentage Utilization: " + cpuMonitor.getApplicationCPUPercentageUtilization());
                            break;
                        case SYSTEM_CPU_USAGE:
                            String s = line.trim().split(",")[1].trim().split(" ")[1].replace("%", "");
                            Platform.runLater(() -> cpuMonitor.setSystemCPUPercentageUtilization(Integer.parseInt(s)));
                            System.out.println("System CPU Percentage Utilization: " + cpuMonitor.getSystemCPUPercentageUtilization());
                            break;
                    }
                } catch (NumberFormatException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                }
            }
            return null;
        }
    }

    private class updateNetworkStatsTask extends Task<Void> {
        private String command;

        private updateNetworkStatsTask(String command) {
            this.command = command;
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

                    if (device.getSelectedApplication() != null) {
                        String response = ADBUtil.consoleCommand(command).split("\n")[1];//.split("\n")[0];
                        String[] split = response.split(" ");

                        receivedBytes = split[5];
                        receivedPackets = split[6];
                        sentBytes = split[7];
                        sentPackets = split[8];
                    }

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

                } catch (IndexOutOfBoundsException ee) {
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

    private class memoryUsageTask extends Task<Void> {
        private String freeMemoryCommand;
        private String applicationMemoryUsageCommand;

        private memoryUsageTask(String freeMemoryCommand, String applicationMemoryUsageCommand) {
            this.freeMemoryCommand = freeMemoryCommand;
            this.applicationMemoryUsageCommand = applicationMemoryUsageCommand;
        }

        @Override
        protected Void call() throws Exception {
            while (true) {
                if(isCancelled())
                    return null;

                String freeMemory = getResponseLine(freeMemoryCommand).replaceAll("[a-zA-Z]", "").replace(":", "").trim();

                //Total application memory usage
                String applicationMemoryUsage = "0";
                if (device.getSelectedApplication() != null) {
                    String response = getResponseLine(applicationMemoryUsageCommand);
                    Log.info("response 1: " + response);
                    response = response != null ? response.trim().replaceAll(" {2,}", " ") : "0 0";
                    Log.info("Response: " + response);
                    applicationMemoryUsage = response.split(" ")[1];
                }
                final String newApplicationMemoryUsage = applicationMemoryUsage;


                try {
                    Platform.runLater(() -> memoryMonitor.setFreeMemory(Integer.parseInt(freeMemory)));
                    System.out.println("Free Memory: " + memoryMonitor.getTotalMemory());

                    Platform.runLater(() -> memoryMonitor.setApplicationMemoryUsage(Integer.parseInt(newApplicationMemoryUsage)));
                    System.out.println("Total application memory usage " + memoryMonitor.getApplicationMemoryUsage());
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
                    Platform.runLater(() -> cpuMonitor.setRunningProcesses(Integer.parseInt(runningProcesses)));
                    System.out.println("Running processes: " + cpuMonitor.getRunningProcesses());

                    Platform.runLater(() -> cpuMonitor.setCurrentFrequency(Integer.parseInt(currentFrequency)));
                    System.out.println("Current frequency: " + cpuMonitor.getCurrentFrequency());
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
        APPLICATION_CPU_USAGE,
        SYSTEM_CPU_USAGE,
    }
}
