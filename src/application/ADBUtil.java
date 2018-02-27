package application;

import application.commands.GetTouchPositionController;
import application.commands.RecordInputsController;
import application.utilities.ADBConnectionController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ADBUtil {

    private static File adbLocation = new File(System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools");
    private static String DIRECTORY = System.getProperty("user.dir") + "\\misc\\commands\\";
    private static String adbPath;
    private static boolean isADBFound = false;
    private static Integer decimal = 0;
    private static String lineGlobal = "";
    private static double resolutionX;
    private static double resolutionY;
    private static double maxPositionX;
    private static double maxPositionY;

    private static double xStart = 0.0;
    private static double yStart = 0.0;
    private static double xEnd = 0.0;
    private static double yEnd = 0.0;

    private static StringBuilder sendEventBuilder;
    private static AtomicBoolean stopRecordingFlag = new AtomicBoolean(false);
    private static Task recordValuesTask;

    private static HashMap<String, String> keyMap;

    private static AtomicBoolean swipeFlag = new AtomicBoolean(false);

    public static void setSwipeFlag(Boolean flag) {
            swipeFlag.set(flag);

            if(flag) {
                xStart = 0.0;
                yStart = 0.0;
            }
    }

    public static void findADB() {
        try {
            for (File file : adbLocation.listFiles()) {
                if (file.getName().equalsIgnoreCase("adb.exe")) {
                    adbPath = adbLocation.getAbsolutePath() + "\\adb.exe";
                    isADBFound = true;
                    Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            checkDevices();
                            getResolution();
                            getKeyMaps();

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
            npe.printStackTrace();
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
                for (File file : possibleADBLocation.listFiles()) {
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
                    getResolution();
                    getKeyMaps();

                    return null;
                }
            };
            new Thread(task).start();
        }
    }

    private static void getResolution() {
        String[] response = consoleCommand(new String[] {"shell", "wm", "size"}, false).split(" ");
        String[] size = response[2].split("x");
        resolutionX = Double.parseDouble(size[0]);
        resolutionY = Double.parseDouble(size[1]);

        response = consoleCommand(new String[] {"shell", "\"getevent -il | grep ABS_MT_POSITION\""}, false).split("\n");

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

        String[] response = consoleCommand(new String[] {"shell", "cat", "/system/usr/keylayout/Generic.kl"}, false).split("\n");

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

    public synchronized static void setStopRecordingFlag(boolean flag) {
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
                    Process process = Runtime.getRuntime().exec(adbPath + " shell getevent -t");
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
                    Process process = Runtime.getRuntime().exec(adbPath + " shell getevent -lt");
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

                                        if(swipeFlag.get()) {
                                            if (xStart == 0.0) {
                                                xStart = x;
                                                System.out.println("XStart: " + xStart);
                                                controller.setXField(xStart);
                                                startTime = System.currentTimeMillis();
                                            //    System.out.println("sssssssssssssssssStart time X: " + startTime);
                                            } else {
                                                xEnd = x;
                                                controller.setXEndField(xEnd);
                                            }

                                            long timeNow;
                                            if((timeNow = System.currentTimeMillis()) - startTime > 2000) {
                                                long difference = timeNow-startTime;
                                                //System.out.println("timenow X: " + timeNow + "\tStartTime X: " + startTime + "\tDifference X: " + difference);
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
            System.out.println(consoleCommand(new String[] {"devices"}, false));
            String[] result = consoleCommand(new String[] {"devices"}, false).split("\n");

            if(result[1] != null) {
                System.out.println("OOOHHHH SSSHMMMMMMMMM");
                ADBConnectionController adbConnectionController = new ADBConnectionController();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            adbConnectionController.showScreen();
                            wait();
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    }
                });


            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
    public static String consoleCommand(String[] parameters, boolean runInBackground) {

        //System.out.println("In console command");
        StringBuilder result = new StringBuilder();

        try {
            String[] params = new String[parameters.length+1];
            params[0] = adbPath;

            System.arraycopy(parameters, 0, params, 1, parameters.length);

            for (int i = 0; i < params.length; i++) {
                //System.out.println("param["+i+"]: " + params[i]);
            }

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    Process process = Runtime.getRuntime().exec(params);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        //System.out.println(line);
                        result.append(line).append("\n");
                    }

                    bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = bufferedReader.readLine()) != null) {
                        //System.out.println(line);
                        result.append(line).append("\n");
                    }

                    bufferedReader.close();
                    process.waitFor();

                    return null;
                }
            };

            if(runInBackground)
                new Thread(task).start();
            else
                new Thread(task).run();

           // System.out.println("Finished console commands");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        finally {
           // tmp.delete();
        }
        return result.toString();
    }
}
