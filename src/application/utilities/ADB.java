package application.utilities;

import application.ADBUtil;

import java.io.File;

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
        return ADBUtil.consoleCommand("shell am force-stop " + app);
    }

    public static String getAPK(String app, String destination) {
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
}
