package application;

import application.commands.GetTouchPositionController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
                    getResolution();
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
            getResolution();
            return;
        }
    }

    private static void getResolution() {
        String[] response = consoleCommand(new String[] {"shell", "wm", "size"}).split(" ");
        String[] size = response[2].split("x");
        resolutionX = Double.parseDouble(size[0]);
        resolutionY = Double.parseDouble(size[1]);

        response = consoleCommand(new String[] {"shell", "\"getevent -il | grep ABS_MT_POSITION\""}).split("\n");
        String posX = response[0].split(",")[2];
        String posY = response[1].split(",")[2];

        maxPositionX =  Double.parseDouble(posX.substring(posX.length()-5, posX.length()));
        maxPositionY =  Double.parseDouble(posY.substring(posY.length()-5, posY.length()));
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
                                                controller.setXField(xStart);
                                                startTime = System.currentTimeMillis();
                                                System.out.println("sssssssssssssssssStart time X: " + startTime);
                                            } else {
                                                xEnd = x;
                                                controller.setXEndField(xEnd);
                                            }

                                            long timeNow;
                                            if((timeNow = System.currentTimeMillis()) - startTime > 2000) {
                                                long difference = timeNow-startTime;
                                                System.out.println("timenow X: " + timeNow + "\tStartTime X: " + startTime + "\tDifference X: " + difference);
                                                startTime = 0;
                                                xStart = 0.0;
                                                yStart = 0.0;
                                            }
                                        } else {
                                            double y = decimal.doubleValue()*(resolutionY/maxPositionY);
                                            controller.setYField(y);
                                        }
                                    }
                                    else if(lineGlobal.contains("ABS_MT_POSITION_Y")) {
                                        double y = decimal.doubleValue()*(resolutionY/maxPositionY);
                                        if(swipeFlag.get()) {
                                            if (yStart == 0.0) {
                                                yStart = y;
                                                controller.setYField(yStart);
                                            } else {
                                                yEnd = y;
                                                controller.setYEndField(yEnd);
                                            }
                                        } else {
                                            double x = decimal.doubleValue()*(resolutionX/maxPositionX);
                                            controller.setXField(x);
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

    public static String consoleCommand(String[] parameters) {

        System.out.println("In console command");
        StringBuilder result = new StringBuilder();

        try {
            String[] params = new String[parameters.length+1];
            params[0] = adbPath;

            System.arraycopy(parameters, 0, params, 1, parameters.length);

            for (int i = 0; i < params.length; i++) {
                System.out.println("param["+i+"]: " + params[i]);
            }

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    Process process = Runtime.getRuntime().exec(params);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        result.append(line).append("\n");
                    }

                    bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        result.append(line).append("\n");
                    }

                    bufferedReader.close();
                    process.waitFor();

                    return null;
                }
            };
            new Thread(task).run();

            System.out.println("Finished console commands");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        finally {
           // tmp.delete();
        }
        return result.toString();
    }
}
