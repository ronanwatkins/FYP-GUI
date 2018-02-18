package application;

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

    public static String consoleCommand(String[] parameters) {

        System.out.println("In console command");
        StringBuilder result = new StringBuilder();

       // String filename = DIRECTORY + "log.txt";

       // File tmp = null;

        try {
//            tmp = File.createTempFile("out", null);
//            tmp.deleteOnExit();

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(adbPath);
            //arrayList.addAll(parameters);
            for(String parameter : parameters) {
                System.out.println("Param: "+ parameter);
                arrayList.add(parameter);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File tmp = File.createTempFile("out", null);
                        tmp.deleteOnExit();
                        final ProcessBuilder processBuilder = new ProcessBuilder();
                        processBuilder.command(arrayList).redirectErrorStream(true)
                                .redirectOutput(tmp);

                        final Process process = processBuilder.start();

                        final StringBuilder out = new StringBuilder();

                        try (final InputStream is = new FileInputStream(tmp)) {
                            int c;
                            while ((c = is.read()) != -1) {

                                out.append((char) c);
                            }
                        }
                    }catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }).start();



//            System.out.println("Starting process");
//            Process process = new ProcessBuilder(arrayList).start();
//            System.out.println("Process started");

//            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//             String line;
//            System.out.println("Created buffered reader");
//            while ((line = br.readLine()) != null) {
//                System.out.println("reading......");
//                System.out.println(line);
//                result.append(line).append("\n");
//            }

//            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

//            while ((line = br.readLine()) != null) {
//                System.out.println("Error: " + line);
//                result.append(line).append("\n");
//            }

        } catch (Exception ee) {
            ee.printStackTrace();
        }
        finally {
           // tmp.delete();
        }
        return result.toString();
    }
}
