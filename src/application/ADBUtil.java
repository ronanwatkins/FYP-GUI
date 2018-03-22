package application;

import application.commands.GetTouchPositionController;
import application.commands.RecordInputsController;
import application.utilities.ADBConnectionController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ADBUtil {

    private static File adbLocation;
    private static String DIRECTORY;
    private static String adbPath;
    private static String[] params;
    private static boolean isDeviceNameSet;
    private static boolean isADBFound;
    private static Integer decimal;
    private static String lineGlobal;
    private static double resolutionX;
    private static double resolutionY;
    private static double maxPositionX;
    private static double maxPositionY;

    private static String deviceName;
    private static final Object lock;
    private static AtomicBoolean isFirstRun;

    private static double xStart;
    private static double yStart;
    private static double xEnd;
    private static double yEnd;

    private static int deviceCount;
    private static ADBConnectionController controller;

    private static StringBuilder sendEventBuilder;
    private static AtomicBoolean stopRecordingFlag;
    private static Task recordValuesTask;

    private static HashMap<String, String> keyMap;

    private static AtomicBoolean swipeFlag;

    static {
        adbLocation = new File(System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools");
        DIRECTORY = System.getProperty("user.dir") + "\\misc\\commands\\";
        params = null;
        isDeviceNameSet = false;
        isADBFound = false;
        decimal = 0;
        lineGlobal = "";
        deviceName = "";
        lock = new Object();
        isFirstRun = new AtomicBoolean(true);

        xStart = 0.0;
        yStart = 0.0;
        xEnd = 0.0;
        yEnd = 0.0;

        deviceCount = 0;
        controller = null;

        stopRecordingFlag = new AtomicBoolean(false);
        swipeFlag = new AtomicBoolean(false);
    }

    public static void setSwipeFlag(Boolean flag) {
            swipeFlag.set(flag);

            if(flag) {
                xStart = 0.0;
                yStart = 0.0;
            }
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
                           // checkDevices();
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
                        System.out.println("Found ADB, path: " + file.getAbsolutePath());
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
                npe.printStackTrace();
                showInputDialog();
            }
        } else {
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
        }
    }

    private static void getResolution() {
        System.out.println("in getResolution");

        String[] response = consoleCommand(new String[] {"-s", deviceName, "shell", "wm", "size"}, false).split(" ");
        String[] size = response[2].split("x");
        resolutionX = Double.parseDouble(size[0]);
        resolutionY = Double.parseDouble(size[1]);

        response = consoleCommand(new String[] {"-s", deviceName, "shell", "\"getevent -il | grep ABS_MT_POSITION\""}, false).split("\n");

        for(String res : response) {
            if(res.contains("ABS_MT_POSITION_X")) {
                String temp = res.split(",")[2];
                maxPositionX =  Double.parseDouble(temp.substring(temp.length()-5, temp.length()).trim());
            }
            if(res.contains("ABS_MT_POSITION_Y")) {
                String temp = res.split(",")[2];
                maxPositionY =  Double.parseDouble(temp.substring(temp.length()-5, temp.length()).trim());
            }
        }

        System.out.println("resolution X: " + resolutionX);
        System.out.println("resolution Y: " + resolutionY);

        System.out.println("MaxX: " + maxPositionX);
        System.out.println("MaxY: " + maxPositionY);
    }

    public static void getKeyMaps() {
        keyMap = new HashMap<>();

        String[] response = consoleCommand(new String[] {"-s", deviceName, "shell", "cat", "/system/usr/keylayout/Generic.kl"}, false).split("\n");

        for(String line: response) {
            if(line.contains("VOLUME_UP") && !keyMap.containsKey("VOLUME_UP"))
                keyMap.put("VOLUME_UP", line.split(" ")[1]);
            if(line.contains("VOLUME_DOWN") && !keyMap.containsKey("VOLUME_DOWN"))
                keyMap.put("VOLUME_DOWN", line.split(" ")[1]);
            if(line.contains("POWER") && !keyMap.containsKey("POWER"))
                keyMap.put("POWER", line.split(" ")[1]);
            if(line.contains("CAMERA") && !keyMap.containsKey("CAMERA"))
                keyMap.put("CAMERA", line.split(" ")[1]);
        }
    }

    public static void setStopRecordingFlag(boolean flag) {
        if(flag) {
            recordValuesTask.cancel();

            System.out.println(sendEventBuilder.toString());
        }
    }

    public static void recordInputValues(RecordInputsController controller) throws Exception{
        System.out.println("Recording....");
        sendEventBuilder = new StringBuilder();
        try {

            recordValuesTask = new Task() {
                @Override
                protected Object call() throws Exception {
                    Process process = Runtime.getRuntime().exec(adbPath + " -s " + deviceName + " shell getevent -t");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if(line.contains("/dev/input/") && line.startsWith("[")) {
                            line = line.substring(line.indexOf("]")+1, line.length()).replace(":", "").trim();
                            String[] lineSplit = line.split(" ");
                            line = lineSplit[0] + " " + Integer.parseInt(lineSplit[1],16) + " " + Integer.parseInt(lineSplit[2],16) + " " + Long.parseLong(lineSplit[3],16);

                            sendEventBuilder.append(line).append("\n");
                        }
                    }

                    bufferedReader.close();
                    process.waitFor();

                    return null;
                }
            };
            new Thread(recordValuesTask).start();

        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public static void getCursorPosition(GetTouchPositionController controller) throws Exception{

        try {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    Process process = Runtime.getRuntime().exec(adbPath + " -s " + deviceName + " shell getevent -lt");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    long startTime = 0;
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {


                //    while ((line = consoleCommand(new String[] {"shell", "getevent", "-lt"}, false)) != null) {

                        if(line.contains("ABS_MT_POSITION")) {
                            lineGlobal = line;
                            String position = line.substring(line.trim().length()-8).trim();
                            decimal = 0;
                            try {
                                decimal = Integer.parseInt(position, 16);
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }

                            if(decimal != null) {
                                try {

                                    if(lineGlobal.contains("ABS_MT_POSITION_X")) {
                                        double x = decimal.doubleValue()*(resolutionX/maxPositionX);
                                        System.out.println("decimal: " + decimal);
                                        System.out.println("resolutionX: " + resolutionX);
                                        System.out.println("maxpositioX: " + maxPositionX);

                                        if(swipeFlag.get()) {
                                            if (xStart == 0.0) {
                                                xStart = x;
                                                System.out.println("XStart: " + xStart);
                                                controller.setXField(xStart);
                                                startTime = System.currentTimeMillis();
                                            } else {
                                                xEnd = x;
                                                controller.setXEndField(xEnd);
                                            }

                                            if(System.currentTimeMillis() - startTime > 2000) {
                                                startTime = 0;
                                                xStart = 0.0;
                                                yStart = 0.0;
                                            }
                                        } else {
                                            System.out.println("X: " + x);
                                            controller.setXField(x);
                                        }
                                    }
                                    else if(lineGlobal.contains("ABS_MT_POSITION_Y")) {
                                        double y = decimal.doubleValue()*(resolutionY/maxPositionY);

                                        if(swipeFlag.get()) {
                                            if (yStart == 0.0) {
                                                yStart = y;
                                                System.out.println("YStart: " + yStart);
                                                controller.setYField(yStart);
                                            } else {
                                                yEnd = y;
                                                controller.setYEndField(yEnd);
                                            }
                                        } else {
                                            System.out.println("Y: " + y);
                                            controller.setYField(y);
                                        }
                                    }
                                } catch (Exception ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    }

                    bufferedReader.close();
                    process.waitFor();

                    return null;
                }
            };
            new Thread(task).start();

        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public static void checkDevices() {
        while(true) {
            String[] result = consoleCommand(new String[] {"devices"}, false).split("\n");
            //System.out.println(result.length);

            if(result.length == 2 && isFirstRun.get()) {
                deviceName = result[1].split("\t")[0].trim();
                System.out.println("Device name: " + deviceName);
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        getResolution();
                        getKeyMaps();
                        return null;
                    }
                };
                new Thread(task).start();
            }
            else if(result.length > 2 && !isDeviceNameSet){
                deviceName = result[1].split("\t")[0].trim();
                Platform.runLater(() -> {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(ADBConnectionController.class.getClass().getResource("/application/utilities/ADBConnection.fxml"));
                        Parent root = fxmlLoader.load();

                        controller = fxmlLoader.getController();
                        controller.initDevices(result);

                        Stage stage = new Stage();
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setScene(new Scene(root));

                        stage.show();
                    } catch (IOException ioe) {
                       ioe.printStackTrace();
                    }
                });

                isDeviceNameSet = true;
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        getResolution();
                        getKeyMaps();
                        return null;
                    }
                };
                new Thread(task).start();
            }

            if(deviceCount != result.length) {
                if(controller != null) {
                    Platform.runLater(() -> controller.initDevices(result));
                }

                deviceCount = result.length;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            isFirstRun.set(false);
        }
    }

    public static ArrayList<String> listApplications() {
        String[] applications = consoleCommand(new String[] {"shell", "pm", "list" ,"packages"}, false).replace("package:", "").trim().split("\n");

        List<String> apps = Arrays.asList(applications);

        return new ArrayList<>(apps);
    }

    public static String connectOverWifi(String name) {
        deviceName = name;
        deviceName = consoleCommand(new String[] {"shell", "ifconfig", "wlan0"}, false).split(" ")[2] + ":5555";
        return consoleCommand(new String[] {"connect", deviceName}, false);
    }

    public static void setDeviceName(String name) {
        deviceName = name;
    }

    public static String getDeviceName() {
        return deviceName;
    }

    public static String consoleCommand(String[] parameters, boolean runInBackground) {
        if(!isFirstRun.get() && !deviceName.equals("")) {
            params = new String[parameters.length+3];
            params[0] = adbPath;
            params[1] = "-s";
            params[2] = deviceName;

            System.arraycopy(parameters, 0, params, 3, parameters.length);
        } else {
            params = new String[parameters.length+1];
            params[0] = adbPath;

            System.arraycopy(parameters, 0, params, 1, parameters.length);
        }
        StringBuilder result = new StringBuilder();
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                //for(String param : params)
                    //System.out.println("param: " + param);

                Process process = Runtime.getRuntime().exec(params);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(!line.isEmpty()) {
                        System.out.println(Thread.currentThread().getId() + " InputStream >> " + line);
                        result.append(line).append("\n");
                    }
                }

                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = bufferedReader.readLine()) != null) {
                    if(!line.isEmpty()) {
                        System.out.println(Thread.currentThread().getId() + " ErrorStream >> " + line);
                        result.append(line).append("\n");
                    }
                }

                bufferedReader.close();
                //process.waitFor();
                //process.waitFor(2, TimeUnit.SECONDS);

                System.out.println("going to return now");
                return result.toString();
            }
        };

        Thread thread = new Thread(task);
        if(runInBackground)
            thread.start();
        else
            thread.run();

//        try {
//            thread.join(2000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }

       // System.out.println("here now hey");
//        if(runInBackground) {
//            CountDownLatch latch = new CountDownLatch(1);
//            try {
//                latch.await();
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//        }
        System.out.println("is it runnnong? " + task);
        System.out.println("is it runnnong? " + task.isRunning());

        task.setOnSucceeded(event -> {
            //return result.toString();
            //return;
            //return task.getValue();
            System.out.println("succeeded");
        });
        System.out.println("Im finished");
        task.setOnFailed(event -> System.out.println("failed"));

        System.out.println(Thread.currentThread().getId() + " result: " + result);
        return result.toString();
    }

    public static String consoleCommand(String[] parameters) {
        if(!isFirstRun.get() && !deviceName.equals("")) {
            params = new String[parameters.length+3];
            params[0] = adbPath;
            params[1] = "-s";
            params[2] = deviceName;

            System.arraycopy(parameters, 0, params, 3, parameters.length);
        } else {
            params = new String[parameters.length+1];
            params[0] = adbPath;

            System.arraycopy(parameters, 0, params, 1, parameters.length);
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

            process.waitFor(10, TimeUnit.SECONDS);
            bufferedReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        return result.toString();
    }

    public static void disconnect() {
        System.out.println("In disconnect yo");
        System.out.println(consoleCommand(new String[] {"disconnect"}, false));
    }
}
