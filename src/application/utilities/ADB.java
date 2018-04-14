package application.utilities;

import application.ADBUtil;
import application.device.Device;
import application.device.DeviceIntent;
import application.device.Intent;
import application.device.IntentType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADB {
    private static final Logger Log = Logger.getLogger(ADB.class.getName());

    private static final String ACTIVITY_RESOLVER_TABLE = "Activity Resolver Table:";
    private static final String SERVICE_RESOLVER_TABLE = "Service Resolver Table:";
    private static final String RECEIVER_RESOLVER_TABLE = "Receiver Resolver Table:";
    private static final String FULL_MIME_TYPES = "Full MIME Types:";
    private static final String BASE_MIME_TYPES = "Base MIME Types:";

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

    public static ArrayList<String> listApplications() {
        return ADBUtil.listApplications();
    }

    public static Map<String, Set<String>> getMimeMap(String app) {
        Log.info("Command: " + "shell dumpsys package " + app);
        Map<String, Set<String>> map = new HashMap<>();

        String details = ADBUtil.consoleCommand("shell dumpsys package " + app);
        Log.info("Result: " + details);
        if(details.contains(ACTIVITY_RESOLVER_TABLE)) {
            String temp = details;
            if (temp.contains(FULL_MIME_TYPES) && temp.contains(BASE_MIME_TYPES)) {
                Log.info(ACTIVITY_RESOLVER_TABLE + " Getting details...");
                temp = temp.substring(temp.indexOf(FULL_MIME_TYPES), temp.indexOf(BASE_MIME_TYPES)).replace(FULL_MIME_TYPES, "");
                Log.info("Got temp, now to get map...");
                map.putAll(mimeMap(temp));
            }
        }

        if(details.contains(RECEIVER_RESOLVER_TABLE)) {
            String temp = details.substring(details.indexOf(RECEIVER_RESOLVER_TABLE));
            if (temp.contains(FULL_MIME_TYPES) && temp.contains(BASE_MIME_TYPES)) {
                Log.info(RECEIVER_RESOLVER_TABLE + " Getting details...");
                temp = temp.substring(temp.indexOf(FULL_MIME_TYPES), temp.indexOf(BASE_MIME_TYPES)).replace(FULL_MIME_TYPES, "");
                Log.info("Got temp, now to get map...");
                map.putAll(mimeMap(temp));



            }
        }

        if(details.contains(SERVICE_RESOLVER_TABLE)) {
            String temp = details.substring(details.indexOf(SERVICE_RESOLVER_TABLE));
            if (temp.contains(FULL_MIME_TYPES) && temp.contains(BASE_MIME_TYPES)) {
                Log.info(SERVICE_RESOLVER_TABLE + " Getting details...");
                temp = temp.substring(temp.indexOf(FULL_MIME_TYPES), temp.indexOf(BASE_MIME_TYPES)).replace(FULL_MIME_TYPES, "");
                Log.info("Got temp, now to get map...");
                map.putAll(mimeMap(temp));
            }
        }

        Log.info("Full map: ");
        for(String string : map.keySet())
            System.out.println("Key: " + string + ", Value: " + map.get(string));

        return map;
    }

    private static Map<String, Set<String>> mimeMap(String input) {
        Map<String, Set<String>> map = new HashMap<>();

        String[] split = input.split("\\n {6}(?! )");
        for(String string : split) {
            if(string.isEmpty())
                continue;
            string = string.trim();

            Log.info("Loop 1: " + string);
            String[] split2 = string.split("\n");
            String mimeType = split2[0].replace(":", "");
            Log.info("mimeType: " + mimeType);
            Set<String> components = new TreeSet<>();
            for(int i=1; i<split2.length; i++) {
                split2[i] = split2[i].trim();
                Log.info("Loop 2: " + split2[i]);
                String component = split2[i].split(" ")[1];
                Log.info("component: " + component);
                components.add(component);
            }

            map.put(mimeType, components);
        }
        Log.info("Details: " + input);
        for(String string : map.keySet())
            System.out.println("Key: " + string + ", Value: " + map.get(string));

        return map;
    }

    public static Set<DeviceIntent> getIntents(String app) {
        //if(device.isEmulator()) return getEmulatorIntents(app);
        //else
            return getDeviceIntents(app);
    }

    private static Set<DeviceIntent> getDeviceIntents(String app) {
  //      Log.info("Command: " + "shell dumpsys package " + app);
        Set<DeviceIntent> set = new TreeSet<>();

        String packageDetails = ADBUtil.consoleCommand("shell dumpsys package " + app);
       // System.out.println("PACKAGE DETAILS: " + packageDetails);

        if(packageDetails.contains(ACTIVITY_RESOLVER_TABLE))
            set.addAll(deviceIntents(packageDetails, IntentType.ACTIVITY));
        if(packageDetails.contains(RECEIVER_RESOLVER_TABLE))
            set.addAll(deviceIntents(packageDetails, IntentType.BROADCAST));
        if(packageDetails.contains(SERVICE_RESOLVER_TABLE))
            set.addAll(deviceIntents(packageDetails, IntentType.SERVICE));

     //   for(DeviceIntent deviceIntent : set)
       //     System.out.println("FINISHED: " + deviceIntent);

        return set;
    }

    private static Set<DeviceIntent> deviceIntents(String input, IntentType intentType) {
        Set<DeviceIntent> intents = new TreeSet<>();

        String data = "";
        int position = 0;

        Pattern p = Pattern.compile("\\n[A-Za-z]");
        Matcher m = p.matcher(input);

        int i=0;
        while (m.find()) {
            try {
                if(i > intentType.ordinal())
                    break;
                position = m.start();
                i++;
            } catch (IllegalStateException|IndexOutOfBoundsException e) {
                Log.error(e.getMessage(), e);
            }
        }

        switch (intentType) {
            case ACTIVITY:
                data = input.substring(input.indexOf(ACTIVITY_RESOLVER_TABLE),position);
                break;
            case SERVICE:
                data = input.substring(input.indexOf(SERVICE_RESOLVER_TABLE),position);
                break;
            case BROADCAST:
                data = input.substring(input.indexOf(RECEIVER_RESOLVER_TABLE),position);
                break;
        }

        int s1 = data.indexOf("Non-Data Actions:");
        int s2 = data.indexOf("MIME Typed Actions:");

        boolean containsNonData = s1 > -1;
        boolean containsMimeTypes = s2 > -1;

        if(containsNonData && containsMimeTypes) {
        //    System.out.println("----------------FIRST------------------");
            boolean isNonDataFirst = s1 < s2;

            int start = isNonDataFirst ? s1 : s2;
            data = data.substring(start);

            String[] actions = data.split(!isNonDataFirst ? "Non-Data Actions:" : "MIME Typed Actions:");
            for (i = 0; i < actions.length; i++)
                actions[i] = actions[i].replace("Non-Data Actions:", "").replace("MIME Typed Actions:", "");

            String nonDataActions = actions[isNonDataFirst ? 0 : 1];
            intents.addAll(intents(nonDataActions, intentType, false));

            String mimeTypedActions = actions[isNonDataFirst ? 1 : 0];
            intents.addAll(intents(mimeTypedActions, intentType, true));
        } else if (containsNonData && !containsMimeTypes) {
        //    System.out.println("----------------SECOND------------------");
            String nonDataActions = data.substring(s1).replace("Non-Data Actions:", "");
        //    System.out.println("nonDataActions: " + nonDataActions);
            intents.addAll(intents(nonDataActions, intentType, false));
        } else if (!containsNonData && containsMimeTypes) {
         //   System.out.println("----------------THIRD------------------");
            String mimeTypedActions = data.substring(s2).replace("MIME Typed Actions:", "");
            intents.addAll(intents(mimeTypedActions, intentType, true));
        }

        return intents;
    }

    private static Set<DeviceIntent> intents(String input, IntentType intentType, boolean isMimeTyped) {
        boolean canBreak = false;
        Set<DeviceIntent> intents = new TreeSet<>();

        String[] split = input.trim().split("\\n {6}(?! )");
        for (String string : split) {
            if(string.isEmpty())
                continue;

            String[] temp = string.trim().split("\n");
            String action = temp[0].replace(":", "").trim();
           // System.out.println("ACTION: " + action);
            ObservableList<StringProperty> components = FXCollections.observableArrayList();
            for(int i=1; i<temp.length; i++) {
              //  System.out.println("temp" + i + " " +temp[i]);
                if(temp[i].startsWith("Key Set Manager:") || temp[i].startsWith("Permissions:") || temp[i].startsWith("Registered ContentProviders:")) {
               //     System.out.println("fuck: " + temp[i]);
                    canBreak = true;
                    break;
                }

                String component = temp[i].trim().split(" ")[1].trim();
            //    System.out.println("COMPONENT: " + component);
                components.add(new SimpleStringProperty(component));
            }

            if(canBreak)
                break;

            intents.add(new DeviceIntent(action, components, intentType, isMimeTyped));
        }

     //   for(DeviceIntent intent : intents)
     //       System.out.println("RESULT:\t" + intent);

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
