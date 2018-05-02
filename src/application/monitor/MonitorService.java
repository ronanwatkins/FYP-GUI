package application.monitor;

import application.utilities.ADBUtil;
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

public class MonitorService implements Runnable {
    private static final Logger Log = Logger.getLogger(MonitorService.class.getName());

    private Device device = Device.getInstance();
    private CPUMonitor cpuMonitor = CPUMonitor.getInstance();
    private NetworkMonitor networkMonitor = NetworkMonitor.getInstance();
    private MemoryMonitor memoryMonitor = MemoryMonitor.getInstance();

    private static MonitorService instance = new MonitorService();
    private ArrayList<Task<Void>> tasks = new ArrayList<>();
    private AtomicBoolean isRunning;

    private String startOfCommand;

    private LastBytes system = new LastBytes();
    private LastBytes application = new LastBytes();

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

        system.isFirstRun = true;
        system.lastReceivedKiloBytes = 0;
        system.lastSentKiloBytes = 0;
        application.isFirstRun = true;
        application.lastReceivedKiloBytes = 0;
        application.lastSentKiloBytes = 0;

        if(device.getSelectedApplication() != null) {
            tasks.add(new CPUUsageTask(startOfCommand + " shell \"top | grep " + device.getSelectedApplication().getName() + "\"", TaskType.APPLICATION));
            tasks.add(new memoryUsageTask("shell \"dumpsys meminfo " + device.getSelectedApplication().getName() + " | grep TOTAL\"", TaskType.APPLICATION));
            tasks.add(new updateNetworkStatsTask("shell cat /proc/net/xt_qtaguid/stats | grep " + device.getSelectedApplication().getUserID() + " | grep wlan0", TaskType.APPLICATION));
        }

        tasks.add(new CPUUptimeTask("shell cat /proc/uptime"));
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
        protected String command;

        protected runningThreadsTask(String command) {
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

    private class CPUUptimeTask extends runningThreadsTask {

        private CPUUptimeTask(String command) {
            super(command);
        }

        private String formatTime(long totalSeconds) {
            long days = 0;
            long hours = 0;
            long minutes = 0;
            long seconds = 0;
            if (totalSeconds >= 3600) {
                hours = totalSeconds / 3600;
                totalSeconds -= hours * 3600;
            }
            if (totalSeconds >= 60) {
                minutes = totalSeconds / 60;
                totalSeconds -= minutes * 60;
            }
            if(hours >= 24) {
                days = hours / 24;
                hours -= days * 24;
            }
            seconds = totalSeconds;

            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        }

        @Override
        protected Void call() throws Exception {
            while(true) {
                if(isCancelled())
                    return null;

                try {
                    String response = getResponseLine(command).split(" ")[0].split("\\.")[0];
                    long totalSeconds = Long.parseLong(response.trim());

                    Platform.runLater(() -> cpuMonitor.setUpTime(formatTime(totalSeconds)));

                } catch (Exception ee) {
                    Log.error(ee.getMessage(), ee);
                    return null;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    throw new InterruptedException();
                }
            }
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
                    int percentage;
                    switch (taskType) {
                        case APPLICATION:
                            final String newLine = line.trim().replaceAll(" {2,}" , " ").split(" ")[2].replace("%", "");
                            percentage = Integer.parseInt(newLine);
                            Platform.runLater(() -> cpuMonitor.setApplicationCPUPercentageUtilization(percentage));
                            //Log.info("Application CPU Percentage Utilization: " + cpuMonitor.getApplicationCPUPercentageUtilization());
                            break;
                        case SYSTEM:
                            String s = line.trim().split(",")[0].trim().split(" ")[1].replace("%", "");
                            percentage = Integer.parseInt(s);
                            Platform.runLater(() -> cpuMonitor.setSystemCPUPercentageUtilization(percentage));
                            break;
                    }
                } catch (NumberFormatException nfe) {
                 //   Log.error(nfe.getMessage(), nfe);
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

                    double sentKiloBytes = 0;
                    double sentKBps = 0;
                    double receivedKiloBytes = 0;
                    double receivedKBps = 0;

                    switch (taskType) {
                        case APPLICATION:
                            response = ADBUtil.consoleCommand(command).split("\n")[1];//.split("\n")[0];

                            String[] split = response.split(" ");

                            receivedBytes = split[5];
                            receivedPackets = split[6];
                            sentBytes = split[7];
                            sentPackets = split[8];

                            sentKiloBytes = ((Double.parseDouble(sentBytes)))/100;
                            sentKBps = (application.isFirstRun ? 0 : (((sentKiloBytes - application.lastSentKiloBytes)/100)));
                            application.lastSentKiloBytes = sentKiloBytes;

                            receivedKiloBytes = (Double.parseDouble(receivedBytes))/100;
                            receivedKBps = (application.isFirstRun ? 0 : (((receivedKiloBytes - application.lastReceivedKiloBytes)/100)));
                            application.lastReceivedKiloBytes = receivedKiloBytes;
                            break;
                        case SYSTEM:
                            response = getResponseLine(command).replaceAll(" {2,}", " ").trim();

                            split = response.split(" ");

                            receivedBytes = split[1];
                            receivedPackets = split[2];
                            sentBytes = split[9];
                            sentPackets = split[10];

                            sentKiloBytes = ((Double.parseDouble(sentBytes)))/100;
                            sentKBps = (system.isFirstRun ? 0 : (((sentKiloBytes - system.lastSentKiloBytes)/100)));
                            system.lastSentKiloBytes = sentKiloBytes;

                            receivedKiloBytes = (Double.parseDouble(receivedBytes))/100;
                            receivedKBps = (system.isFirstRun ? 0 : (((receivedKiloBytes - system.lastReceivedKiloBytes)/100)));
                            system.lastReceivedKiloBytes = receivedKiloBytes;
                            break;
                    }

                    final String newReceivedBytes = receivedBytes;
                    final String newReceivedPackets = receivedPackets;
                    final String newSentBytes = sentBytes;
                    final String newSentPackets = sentPackets;

                    final double newReceivedKBps = receivedKBps;
                    final double newSentKBps = sentKBps;

                    if(application.isFirstRun) {
                        application.isFirstRun = false;
                        continue;
                    }

                    if(system.isFirstRun) {
                        system.isFirstRun = false;
                        continue;
                    }

                    switch (taskType) {
                        case APPLICATION:
                            Platform.runLater(() -> {
                                networkMonitor.setApplicationReceivedBytes(Long.parseLong(newReceivedBytes));
                                networkMonitor.setApplicationReceivedPackets(Long.parseLong(newReceivedPackets));
                                networkMonitor.setApplicationReceivedKBps(newReceivedKBps);
                                networkMonitor.setApplicationSentBytes(Long.parseLong(newSentBytes));
                                networkMonitor.setApplicationSentPackets(Long.parseLong(newSentPackets));
                                networkMonitor.setApplicationSentKBps(newSentKBps);
                            });
                            break;
                        case SYSTEM:
                            Platform.runLater(() -> {
                                networkMonitor.setSystemReceivedBytes(Long.parseLong(newReceivedBytes));
                                networkMonitor.setSystemReceivedPackets(Long.parseLong(newReceivedPackets));
                                networkMonitor.setSystemReceivedKBps(newReceivedKBps);
                                networkMonitor.setSystemSentBytes(Long.parseLong(newSentBytes));
                                networkMonitor.setSystemSentPackets(Long.parseLong(newSentPackets));
                                networkMonitor.setSystemSentKBps(newSentKBps);
                            });
                            break;
                    }

                } catch (NumberFormatException ee) {
                    Log.error(ee.getMessage(), ee);
                    continue;
                } catch (ArrayIndexOutOfBoundsException ee) {
                    continue;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
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
                            //Log.info("Free Memory: " + memoryMonitor.getTotalMemory());
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
                            //Log.info("Total application memory usage " + memoryMonitor.getApplicationMemoryUsage());
                            break;
                    }
                } catch (NumberFormatException nfe) {
                    Log.error(nfe.getMessage(), nfe);
                    continue;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
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

    private class LastBytes {
        private boolean isFirstRun;
        private double lastSentKiloBytes;
        private double lastReceivedKiloBytes;
    }

}
