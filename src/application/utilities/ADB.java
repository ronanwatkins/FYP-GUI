package application.utilities;

import application.device.Device;
import application.device.Intent;
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
    private static final String SCHEMES = "Schemes:";

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
        String APKName =  app + "." + APKPath.substring(APKPath.lastIndexOf("/")+1);

        ADBUtil.consoleCommand("shell cp "+ APKPath +" /sdcard/" + APKName);
        String result = ADBUtil.consoleCommand("pull /sdcard/" +  APKName + " " + destination);

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

    public static ObservableList<String> getAssociatedSchemes(String app, String component, int intentType) {
        Log.info("Command: " + "shell dumpsys package " + app);
        ObservableList<String> schemes = FXCollections.observableArrayList();

        String details = ADBUtil.consoleCommand("shell dumpsys package " + app);
        String temp = "";
        //Log.info("Result: " + details);

        switch (intentType) {
            case Intent.ACTIVITY:
                if(details.contains(ACTIVITY_RESOLVER_TABLE))
                    temp = details;
                break;
            case Intent.BROADCAST:
                if(details.contains(RECEIVER_RESOLVER_TABLE))
                    temp = details.substring(details.indexOf(RECEIVER_RESOLVER_TABLE));
                break;
            case Intent.SERVICE:
                if(details.contains(SERVICE_RESOLVER_TABLE))
                    temp = details.substring(details.indexOf(SERVICE_RESOLVER_TABLE));
                break;
        }

        if (temp.contains(SCHEMES)) {
            temp = temp.substring(temp.indexOf(SCHEMES)).split("\\n {2}(?! )")[0];
            schemes.addAll(associatedSchemes(temp, component));
        }

        Log.info("Full list: ");
        for(String string : schemes)
            System.out.println("Value: " + string);

        return schemes;
    }

    private static ObservableList<String> associatedSchemes(String input, String component) {
        Log.info("Component: " + component);
        Log.info("Input: " + input);

        ObservableList<String> schemes = FXCollections.observableArrayList();

        String[] split = input.split("\\n {6}(?! )");

        for(String string : split) {
            if(string.isEmpty())
                continue;
            string = string.trim();

            String[] split2 = string.split("\n");
            String scheme = split2[0] + "//";

            for(int i=1; i<split2.length; i++) {
                split2[i] = split2[i].trim();
                String tempComponent = split2[i].split(" ")[1];
                if(tempComponent.equals(component))
                    schemes.add(scheme);
            }
        }

        FXCollections.sort(schemes);
        return schemes;
    }

    public static ObservableList<String> getAssociatedMimeTypes(String app, String component, int intentType) {
        Log.info("Command: " + "shell dumpsys package " + app);
        ObservableList<String> mimeTypes = FXCollections.observableArrayList();

        String details = ADBUtil.consoleCommand("shell dumpsys package " + app);
        String temp = "";
        //Log.info("Result: " + details);

        switch (intentType) {
            case Intent.ACTIVITY:
                if(details.contains(ACTIVITY_RESOLVER_TABLE))
                    temp = details;
                break;
            case Intent.BROADCAST:
                if(details.contains(RECEIVER_RESOLVER_TABLE))
                    temp = details.substring(details.indexOf(RECEIVER_RESOLVER_TABLE));
                break;
            case Intent.SERVICE:
                if(details.contains(SERVICE_RESOLVER_TABLE))
                    temp = details.substring(details.indexOf(SERVICE_RESOLVER_TABLE));
                break;
        }

        if (temp.contains(FULL_MIME_TYPES) && temp.contains(BASE_MIME_TYPES)) {
            Log.info("Getting details...");
            temp = temp.substring(temp.indexOf(FULL_MIME_TYPES), temp.indexOf(BASE_MIME_TYPES)).replace(FULL_MIME_TYPES, "");
            Log.info("Got temp, now to get map...");
            mimeTypes.addAll(associatedMimeTypes(temp, component));
        }

        Log.info("Full list: ");
        for(String string : mimeTypes)
            System.out.println("Value: " + string);

        return mimeTypes;
    }

    private static ObservableList<String> associatedMimeTypes(String input, String component) {
        Log.info("Component: " + component);
        Log.info("Input: " + input);

        ObservableList<String> mimeTypes = FXCollections.observableArrayList();

        String[] split = input.split("\\n {6}(?! )");
        for(String string : split) {
            if(string.isEmpty())
                continue;
            string = string.trim();

            String[] split2 = string.split("\n");
            String mimeType = split2[0].replace(":", "");

            for(int i=1; i<split2.length; i++) {
                split2[i] = split2[i].trim();
                String tempComponent = split2[i].split(" ")[1];
                if(tempComponent.equals(component))
                    mimeTypes.add(mimeType);
            }
        }

        FXCollections.sort(mimeTypes);
        return mimeTypes;
    }

    public static String sendIntent(String action, String component, String category, String mimeType, String data, int type) {
        StringBuilder stringBuilder = new StringBuilder("shell am ");
        switch (type) {
            case Intent.ACTIVITY:
                stringBuilder.append("start ");
                break;
            case Intent.BROADCAST:
                stringBuilder.append("broadcast ");
                break;
            case Intent.SERVICE:
                stringBuilder.append("startservice ");
                break;
        }

        if(!action.isEmpty())
            stringBuilder.append("-a ").append(action);
        if(!category.isEmpty())
            stringBuilder.append(" -c ").append(category);
        if(!component.isEmpty())
            stringBuilder.append(" -n ").append(component);
        if(!mimeType.isEmpty())
            stringBuilder.append(" -t ").append(mimeType.replace("*/*", "."));
        if(!data.isEmpty())
            stringBuilder.append(" -d ").append(data.replace("//", ""));

        Log.info("Starting intent with command: " + stringBuilder.toString());

        String result = ADBUtil.consoleCommand(stringBuilder.toString());
        Log.info("Response: " + result);

        return result;
    }

    public static Set<Intent> getIntents(String app) {
        Set<Intent> set = new TreeSet<>();

        String packageDetails = ADBUtil.consoleCommand("shell dumpsys package " + app);

        if(packageDetails.contains(ACTIVITY_RESOLVER_TABLE))
            set.addAll(intents(packageDetails, Intent.ACTIVITY));
        if(packageDetails.contains(RECEIVER_RESOLVER_TABLE))
            set.addAll(intents(packageDetails, Intent.BROADCAST));
        if(packageDetails.contains(SERVICE_RESOLVER_TABLE))
            set.addAll(intents(packageDetails, Intent.SERVICE));

        return set;
    }

    private static Set<Intent> intents(String input, int intentType) {
        Set<Intent> intents = new TreeSet<>();

        String data = "";
        int position = 0;

        Pattern p = Pattern.compile("\\n[A-Za-z]");
        Matcher m = p.matcher(input);

        int i=0;
        while (m.find()) {
            try {
                if(i > intentType)
                    break;
                position = m.start();
                i++;
            } catch (IllegalStateException|IndexOutOfBoundsException e) {
                Log.error(e.getMessage(), e);
            }
        }

        switch (intentType) {
            case Intent.ACTIVITY:
                data = input.substring(input.indexOf(ACTIVITY_RESOLVER_TABLE),position);
                break;
            case Intent.BROADCAST:
                data = input.substring(input.indexOf(RECEIVER_RESOLVER_TABLE),position);
                break;
            case Intent.SERVICE:
                data = input.substring(input.indexOf(SERVICE_RESOLVER_TABLE),position);
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

    private static Set<Intent> intents(String input, int intentType, boolean isMimeTyped) {
        boolean canBreak = false;
        Set<Intent> intents = new TreeSet<>();

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
                components.add(new SimpleStringProperty(component));
            }

            if(canBreak)
                break;

            intents.add(new Intent(action, components, intentType, isMimeTyped));
        }

        return intents;
    }
}
