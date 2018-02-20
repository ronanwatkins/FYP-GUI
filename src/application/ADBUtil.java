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

public class ADBUtil {

    private static File adbLocation = new File(System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools");
    private static String DIRECTORY = System.getProperty("user.dir") + "\\misc\\commands\\";
    private static String adbPath;
    private static boolean isADBFound = false;
    private static Integer x = 0;
    private static Integer y = 0;

    public static void findADB() {
        try {
            for (File file : adbLocation.listFiles()) {
                if (file.getName().equalsIgnoreCase("adb.exe")) {
                    //System.out.println("ADB correct");
                    adbPath = adbLocation.getAbsolutePath() + "\\adb.exe";
                    isADBFound = true;
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
            return;
        }
    }

    public static void getCursorPosition(Class callingClass) throws Exception{

        System.out.println("In get cursor position");
        FXMLLoader fxmlLoader = new FXMLLoader(callingClass.getResource("/application/commands/GetTouchPosition.fxml"));

        GetTouchPositionController controller = fxmlLoader.getController();

        try {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    Process process = Runtime.getRuntime().exec(adbPath + " shell getevent -lt");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {

                        if(line.contains("ABS_MT_POSITION_X")) {
                            String xPosition = line.substring(line.trim().length()-8).trim();
                            x = 0;
                            try {
                                x = Integer.parseInt(xPosition, 16);
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                            System.out.println("Xposition decimal: " + x);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(x != null) {
                                        try {
                                            controller.setXField(x);
                                        } catch (Exception ee) {
                                            System.out.println("Exception: " + ee.getMessage());
                                        }
                                    }
                                }
                            });
                        }

                        if(line.contains("ABS_MT_POSITION_Y")) {
                            String yPosition = line.substring(line.trim().length()-8).trim();
                            try {
                                y = Integer.parseInt(yPosition, 16);
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                            System.out.println("Yposition decimal: " + y);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(y != null) {
                                        try {
                                            controller.setXField(y);
                                        } catch (Exception ee) {
                                            System.out.println("Exception: " + ee.getMessage());
                                        }
                                    }
                                }
                            });
                        }
                    }

                    bufferedReader.close();
                    process.waitFor();

                    return null;
                }
            };
            new Thread(task).start();

            System.out.println("Finished getCursorPosition");
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
            new Thread(task).start();

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
