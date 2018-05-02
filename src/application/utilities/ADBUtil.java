package application.utilities;

import application.device.Device;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextInputDialog;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ADBUtil {
    private static final Logger Log = Logger.getLogger(ADBUtil.class.getName());

    private static Device device = Device.getInstance();
    private static File adbLocation;
    private static String adbPath;
    private static String[] params;
    private static boolean isADBFound = false;

    private static final Object lock = new Object();
    private static AtomicBoolean isFirstRun;

    private static int deviceCount;
    private static ADBConnectionController controller;

    private static StringBuilder sendEventBuilder;
    private static Task recordValuesTask;

    static {
        adbLocation = new File(System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools");
        isFirstRun = new AtomicBoolean(true);
    }

    public static void initADB() {
        try {
            for (File file : Objects.requireNonNull(adbLocation.listFiles())) {
                if (file.getName().equalsIgnoreCase("adb.exe")) {
                    adbPath = adbLocation.getAbsolutePath() + "\\adb.exe";
                    isADBFound = true;
                    Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            checkDevices();
                            return null;
                        }
                    };
                    new Thread(task).start();
                    return;
                } else {
                    if (!isADBFound)
                        showInputDialog();
                }
            }
        } catch (NullPointerException npe) {
            showInputDialog();
        }
    }

    private static void showInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("ADB not found");
        dialog.setHeaderText("ADB location not found");
        dialog.setContentText("Please enter path to adb.exe\n\nIf you press cancel, this application will not be\nable to make use of features that the ADB tool\nprovides");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            File possibleADBLocation = new File(result.get());

            try {
                for (File file : Objects.requireNonNull(possibleADBLocation.listFiles())) {
                    if (file.getName().equalsIgnoreCase("adb.exe")) {
                        Log.info("Found ADB, path: " + file.getAbsolutePath());
                        adbLocation = new File(file.getAbsolutePath());
                        adbPath = adbLocation.getAbsolutePath() + "\\adb.exe";
                        isADBFound = true;
                        return;
                    } else {
                        if(!isADBFound)
                            showInputDialog();
                    }
                }
            } catch (NullPointerException npe) {
                Log.error(npe.getMessage(), npe);
                showInputDialog();
            }
        } else {
            Log.info("CANCEL CLICKED");
            adbPath = adbLocation.getAbsolutePath() + "\\adb.exe";
            isADBFound = true;
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    checkDevices();
                    return null;
                }
            };
            new Thread(task).start();
        }
    }

    public static void setStopRecordingFlag(boolean flag) {
        if(flag) {
            recordValuesTask.cancel();
            Log.info(sendEventBuilder.toString());
        }
    }

    private static void checkDevices() {
        while(true) {
            String[] result = connectedDevices();

            if(!result[0].startsWith("List"))
                continue;

            if(result.length == 2 && isFirstRun.get()) {
                device.setName(result[1].replace("device", "").trim());
                device.handleNewConnection();
            }
            else if(result.length > 2 && deviceCount!=result.length){
                deviceCount = result.length;

                Platform.runLater(() -> {
                    try {
                        if (controller == null) {
                            Log.info("here 1");
                            synchronized (lock) {
                                controller = new ADBConnectionController();
                                controller = (ADBConnectionController) controller.newWindow(null, null);
                                controller.initDevices(result);
                            }
                        } else Log.info("not null " + controller);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ie) {
                            Log.error(ie.getMessage(), ie);
                        }
                    } catch (IOException ioe) {
                       Log.error(ioe.getMessage());
                    }
                });
                Log.info("here 2");
            }

            isFirstRun.set(false);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Log.error(ie.getMessage(), ie);
            }
        }
    }

    public static ArrayList<String> listApplications() {
        String[] applications = consoleCommand("shell pm list packages").replace("package:", "").trim().split("\n");

        List<String> apps = Arrays.asList(applications);

        return new ArrayList<>(apps);
    }

    public static String[] connectedDevices() {
        return consoleCommand("devices").split("\n");
    }

    public static String getAdbPath() {
        return adbPath;
    }

    public static String consoleCommand(String command) {
        String[] parameters = command.split(" ");
        if(!isFirstRun.get() && !device.getName().isEmpty()) {
            params = new String[parameters.length+3];
            params[0] = adbPath;
            params[1] = "-s";
            params[2] = device.getName();

            System.arraycopy(parameters, 0, params, 3, parameters.length);
        } else {
            params = new String[parameters.length+1];
            params[0] = adbPath;

            System.arraycopy(parameters, 0, params, 1, parameters.length);
        }

        if(!command.contains("devices")) {
            StringBuilder sb = new StringBuilder("Command: ");
            for (String string : params)
                sb.append(string).append(" ");
        }
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(params);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    result.append(line).append("\n");
                }
            }

            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    result.append(line).append("\n");
                }
            }

            process.waitFor();
            bufferedReader.close();
        }
        catch (IOException|InterruptedException|NullPointerException ee) {
            Log.error(ee.getMessage(), ee);
        }

        return result.toString();
    }

    public static void disconnect() {
        Log.info(consoleCommand("disconnect"));
    }
}
