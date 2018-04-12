package application.utilities;

import application.ADBUtil;
import application.Main;
import application.applications.Intent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

import java.util.*;

public class ADB {
    private static final Logger Log = Logger.getLogger(ADB.class.getName());

    private static Device device = Device.getInstance();

    public static String openApp(String app) {
        return ADBUtil.consoleCommand("shell monkey -p " + app + " 1");
    }

    public static String installApp(String app) {
        return ADBUtil.consoleCommand("install " + app);
    }

    public static String uninstallApp(String app) {
        return ADBUtil.consoleCommand("shell pm uninstall " + app);
    }

    public static String closeApp(String app) {
        String result = ADBUtil.consoleCommand("shell am force-stop " + app);
        return result.isEmpty() ? "Application closed" : result;
    }

    public static String getAPKFile(String app, String destination) {
        String APKPath = ADBUtil.consoleCommand("shell pm path " + app).replace("package:", "").trim();
        String APKName = APKPath.substring(APKPath.lastIndexOf("/")+1);

        ADBUtil.consoleCommand("shell cp "+ APKPath +" /sdcard/" + APKName);
        String result = ADBUtil.consoleCommand("pull /sdcard/" + APKName + " " + destination);

        if(result.startsWith("["))
            result = APKName + " copied to " + destination;
        else
            result = "Could not copy APK\n" + result;

        return result;
    }

    public static String getAPKName(String app) {
        String path = getAPKPath(app);
        return path.substring(path.lastIndexOf("/")+1);
    }

    public static String getAPKPath(String app) {
        return ADBUtil.consoleCommand("shell pm path " + app).replace("package:", "").trim();
    }

    public static String getVersionName(String app) {
        return (ADBUtil.consoleCommand( "shell \"dumpsys package " + app + " | grep versionName\"").split("=")[1].trim());
    }

    public static String getVersionCode(String app) {
        return ADBUtil.consoleCommand( "shell \"dumpsys package " + app + " | grep versionCode\"").trim();
    }

    public static int getUserId(String app) {
        int userId = 0;

        try {
            userId = Integer.parseInt(ADBUtil.consoleCommand("shell \"dumpsys package " + app + " | grep userId\"").split("\n")[0].split("=")[1].split(" ")[0].trim());
        } catch (NumberFormatException e) {
            Log.error(e.getMessage());
        }

        return userId;
    }

    public static String getDataDir(String app) {
        return ADBUtil.consoleCommand( "shell \"dumpsys package " + app + " | grep dataDir\"").split("=")[1].trim();
    }

    public static ArrayList<StringProperty> getFlags(String app) {
        String[] flags = ADBUtil.consoleCommand( "shell \"dumpsys package " + app + " | grep pkgFlags\"").replaceAll("(\\[|\\])", "").split("=")[1].trim().split(" ");
        StringProperty[] values = new SimpleStringProperty[flags.length];
        int i=0;
        for(String flag : flags)
            values[i++] = new SimpleStringProperty(flag);

        return new ArrayList<>(Arrays.asList(values));
    }

    public static ArrayList<StringProperty> getPermissions(String app) {
        String[] permissions = ADBUtil.consoleCommand("shell \"dumpsys package "+ app +" | grep android.permission | grep -v :\"").split("\n");
        StringProperty[] values = new SimpleStringProperty[permissions.length];
        int i=0;
        for(String permission : permissions)
            values[i++] = new SimpleStringProperty(permission);

        return new ArrayList<>(Arrays.asList(values));
    }

    public static ArrayList<StringProperty> listApplications() {
        ArrayList<StringProperty> values = new ArrayList<>();
        for(String application : ADBUtil.listApplications())
            values.add(new SimpleStringProperty(application));

        return values;
    }

    public static Set<Intent> getIntents(String app) {
        String egrep = device.isEmulator() ? "egrep" : "/system/xbin/busybox egrep";
        String sed = device.isEmulator() ? "sed" : "/system/xbin/busybox sed";

        if(device.isEmulator()) return getEmulatorIntents(app);
        else return getDeviceIntents(app);
    }

    private static Set<Intent> getDeviceIntents(String app) {
        Log.info("Command: " + "shell \"dumpsys package " + app);
        Set<Intent> set = new TreeSet<>();

        String temp = ADBUtil.consoleCommand("shell \"dumpsys package " + app);
        System.out.println("temp: " + temp);
        String activity = temp.substring(1,temp.indexOf("Receiver Resolver Table:"));
        System.out.println("Activity: " + activity);
        String receiver = temp.substring(temp.indexOf("Receiver Resolver Table:"), temp.indexOf("Service Resolver Table:"));
        System.out.println("\nReceiver: " + receiver);
        String service = temp.substring(temp.indexOf("Service Resolver Table:"), temp.indexOf("Preferred Activities"));
        System.out.println("\nService: " + service);

        return set;
    }

    private static Set<Intent> getEmulatorIntents(String app) {
        String[] temp = ADBUtil.consoleCommand("shell \"dumpsys package " + app + " | egrep ' filter|Action:|Category:|Type:' | sed '/[a-zA-Z0-9] com/i \\nDIVISIONHERE' | sed 's/^[ \\t]*//;s/[ \\t]*$//'\"").split("DIVISIONHERE");

        Set<Intent> set = new TreeSet<>();
        for(String s : temp) {
            if(s.isEmpty())
                continue;

            System.out.println(s);

            ArrayList<StringProperty> actions = new ArrayList<>();
            ArrayList<StringProperty> categories = new ArrayList<>();
            ArrayList<StringProperty> types = new ArrayList<>();
            String[] temp2 = s.trim().split("\n");

            StringProperty component = new SimpleStringProperty(temp2[0].split(" ")[1]);

            for(String temp3 : temp2) {
                if(temp3.startsWith("Action: ")) {
                    actions.add(new SimpleStringProperty(temp3.replace("Action: ", "").replace("\"", "")));
                }
                else if(temp3.startsWith("Category: ")) {
                    categories.add(new SimpleStringProperty(temp3.replace("Category: ", "").replace("\"", "")));
                }
                else if(temp3.startsWith("Type: ")) {
                    types.add(new SimpleStringProperty(temp3.replace("Type: ", "").replace("\"", "")));
                }
            }

            set.add(new Intent(component, actions, categories, types));
        }

        return set;
    }
}
