package application.utilities;

import application.ADBUtil;
import application.device.Device;
import application.device.DeviceIntent;
import application.device.Intent;
import application.device.IntentType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADB {
    private static final Logger Log = Logger.getLogger(ADB.class.getName());

    private static final String ACTIVITY_RESOLVER_TABLE = "Activity Resolver Table:";
    private static final String SERVICE_RESOLVER_TABLE = "Service Resolver Table:";
    private static final String RECEIVER_RESOLVER_TABLE = "Receiver Resolver Table:";

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

    public static Set<? extends Intent> getIntents(String app) {
        String egrep = device.isEmulator() ? "egrep" : "/system/xbin/busybox egrep";
        String sed = device.isEmulator() ? "sed" : "/system/xbin/busybox sed";

        if(device.isEmulator()) return getEmulatorIntents(app);
        else return getDeviceIntents(app);
    }

    private static Set<DeviceIntent> getDeviceIntents(String app) {
        Log.info("Command: " + "shell dumpsys package " + app);
        Set<DeviceIntent> set = new TreeSet<>();

        String temp = ADBUtil.consoleCommand("shell dumpsys package " + app);
        //System.out.println("temp: " + temp);

        if(temp.contains(ACTIVITY_RESOLVER_TABLE))
            set.addAll(intents(temp, IntentType.ACTIVITY));
       // if(temp.contains(RECEIVER_RESOLVER_TABLE))
        //    set.addAll(intents(temp, IntentType.BROADCAST));
       // if(temp.contains(SERVICE_RESOLVER_TABLE))
        //    set.addAll(intents(temp, IntentType.SERVICE));


        //String receiver = temp.substring(temp.indexOf("Receiver Resolver Table:"), temp.indexOf("Service Resolver Table:"));
        //System.out.println("\nReceiver: " + receiver);
        //String service = temp.substring(temp.indexOf("Service Resolver Table:"), temp.indexOf("Preferred Activities"));
        //System.out.println("\nService: " + service);

        return set;
    }

    private static Set<DeviceIntent> intents(String input, IntentType intentType) {
        Log.info("input: \n" + input);
        Log.info("Intent type: " + intentType.toString());

        Set<DeviceIntent> intents = new TreeSet<>();

        String data = "";
        int position = 0;

//        Pattern p = Pattern.compile("\\n[A-Za-z]");
        Pattern p = Pattern.compile(".");
        Matcher m = p.matcher(data);
        Log.info("Finding...");
        while (m.find()) {
            Log.info("In find");
            Log.info("sham " + m.end());
            Log.info("Group: " + m.group());
           // position = m.start();
           // Log.info("Position: " + position);

        }
        Log.info("Done");
        m = m.reset();

        Log.info("Looking at...");
        while (m.lookingAt()) {
            Log.info("in Look at");
            Log.info("hey " + m.start());
            Log.info("group: " + m.group());
        }
        Log.info("Done 2");
        //else {
        //    Log.warn("Me no find");
       // }

        switch (intentType) {
            case ACTIVITY:
                data = input.substring(input.indexOf(ACTIVITY_RESOLVER_TABLE),input.indexOf(position));
                break;
            case SERVICE:
                data = input.substring(input.indexOf(SERVICE_RESOLVER_TABLE),input.indexOf(position));
                break;
            case BROADCAST:
                data = input.substring(input.indexOf(RECEIVER_RESOLVER_TABLE),input.indexOf(position));
                break;
        }

        System.out.println("DATA: " + data);

        int s1 = data.indexOf("Non-Data Actions:");
        int s2 = data.indexOf("MIME Typed Actions:");

        boolean containsNonData = s1 > -1;
        boolean containsMimeTypes = s2 > -1;

        if(containsNonData && containsMimeTypes) {
            System.out.println("----------------FIRST------------------");
            boolean isNonDataFirst = s1 < s2;

            int start = isNonDataFirst ? s1 : s2;
            System.out.println("s1: " + s1);
            System.out.println("s2: " + s2);
            System.out.println("isNonDataFirst: " + isNonDataFirst);
            System.out.println("start: " + start);
            data = data.substring(start);

            System.out.println("ACTIVITY: \n" + data);

            String[] actions = data.split(!isNonDataFirst ? "Non-Data Actions:" : "MIME Typed Actions:");
            for (int i = 0; i < actions.length; i++)
                actions[i] = actions[i].replace("Non-Data Actions:", "").replace("MIME Typed Actions:", "");

            String nonDataActions = actions[isNonDataFirst ? 0 : 1];
            String mimeTypedActions = actions[isNonDataFirst ? 1 : 0];

            System.out.println("no data: " + nonDataActions);
            System.out.println("mime type: " + mimeTypedActions);
        } else if (containsNonData && !containsMimeTypes) {
            System.out.println("----------------SECOND------------------");
            String nonDataActions = data.substring(s1).replace("Non-Data Actions:", "");
            System.out.println(nonDataActions);
        } else if (!containsNonData && containsMimeTypes) {
            System.out.println("----------------THIRD------------------");
            String mimeTypedActions = data.substring(s2).replace("MIME Typed Actions:", "");
            System.out.println(mimeTypedActions);
        }

        return intents;
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

            set.add(new Intent(component, actions, categories, types, true));
        }

        return set;
    }
}
