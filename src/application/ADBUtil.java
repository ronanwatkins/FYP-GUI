package application;

import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class ADBUtil {

    private static File adbLocation = new File(System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools");
    private static String adbPath;
    private static boolean isADBFound = false;

    public static void findADB() {
        try {
            for (File file : adbLocation.listFiles()) {
                if (file.getName().equalsIgnoreCase("adb.exe")) {
                    System.out.println("ADB correct");
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

    public static String consoleCommand(String[] parameters) {

        StringBuilder result = new StringBuilder();
        try {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(adbPath);
            for(String parameter : parameters)
                arrayList.add(parameter);

            Process process = new ProcessBuilder(arrayList).start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                result.append(line).append("\n");
            }

        } catch (IOException ee) {
            ee.printStackTrace();
        }

        return result.toString();
    }
}
