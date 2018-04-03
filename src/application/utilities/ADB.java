package application.utilities;

import application.ADBUtil;
import application.applications.Intent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ADB {

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
        return Integer.parseInt(ADBUtil.consoleCommand( "shell \"dumpsys package " + app + " | grep userId\"").split("\n")[0].split("=")[1].trim());
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

    public static ArrayList<Intent> getIntents(String app) {
        String[] temp = ADBUtil.consoleCommand("shell \"dumpsys package " + app + " | egrep ' filter|Action:|Category:|Type:' | sed '/[a-zA-Z0-9] com/i \\nDIVISIONHERE' | sed 's/^[ \\t]*//;s/[ \\t]*$//'\"").split("DIVISIONHERE");

        for(String s : temp) {
            if(s.isEmpty())
                continue;

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
            Intent intent = new Intent(component, actions, categories, types);
            System.out.println(intent.toString());
        }

        return null;
    }

}
