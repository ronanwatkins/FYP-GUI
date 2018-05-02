package application.device;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.util.Map;

import static application.utilities.ADB.*;

public class AndroidApplication {
    private static final Logger Log = Logger.getLogger(AndroidApplication.class.getName());

    private StringProperty packageName;
    private StringProperty APKName;
    private StringProperty APKPath;
    private VersionCode versionCode;
    private IntegerProperty userId;
    private StringProperty dataDir;
    private ObservableList<StringProperty> flags;
    private ObservableList<StringProperty> permissions;
    private ObservableList<Intent> intents;
    private Map<String, String> mimeTypeMap;

    private boolean isRunning;
    private boolean isSystem;

    public AndroidApplication(String packageName) {
        this.packageName = new SimpleStringProperty(packageName);
        APKName = new SimpleStringProperty(getAPKName(packageName));
        APKPath = new SimpleStringProperty(getAPKPath(packageName));
        versionCode = new VersionCode();
        userId = new SimpleIntegerProperty(getUserId(packageName));
        dataDir = new SimpleStringProperty(getDataDir(packageName));
        flags = FXCollections.observableArrayList(getFlags(packageName));
        permissions = FXCollections.observableArrayList(getPermissions(packageName));
        intents = FXCollections.observableArrayList(getIntents(packageName));
        //mimeTypeMap = Intent.mimeMap(packageName);
        //Log.info(6);
    }

    //Getters
    public String getName() {
        return packageName.get();
    }

    public int getUserID() { return userId.get(); }


    public boolean canOpen() {
        return intents.size() > 0;
    }

    //Properties
    public StringProperty packageNameProperty() {
        return packageName;
    }

    public StringProperty APKNameProperty() {
        return APKName;
    }

    public StringProperty APKPathProperty() {
        return APKPath;
    }

    public StringProperty versionCodeProperty() {
        return new SimpleStringProperty(versionCode.toString());
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public StringProperty dataDirProperty() {
        return dataDir;
    }

    public ObservableList<StringProperty> flagsProperty() {
        return flags;
    }

    public ObservableList<StringProperty> permissionsProperty() {
        return permissions;
    }

    public ObservableList<Intent> intents() {
        FXCollections.sort(intents, (o1, o2) -> o1.intentType.compareTo(o2.intentType));
        return intents;
    }

    public class VersionCode {
        private String versionName;
        private int code;
        private int targetSdk;

        private VersionCode() {
            versionName = getVersionName(packageName.get());
            String[] version = getVersionCode(packageName.get()).split(" ");

            if(version[0].split("=").length > 0) {
                try {
                    code = Integer.parseInt(version[0].split("=")[1]);
                } catch (NumberFormatException e) {
                    Log.error(e.getMessage(), e);
                }
            }

            if(version[1].split("=").length > 0) {
                try {
                    targetSdk = Integer.parseInt(version[1].split("=")[1].trim());
                } catch (NumberFormatException e) {
                    Log.error(e.getMessage(), e);
                }
            }
        }

        @Override
        public String toString() {
            return "Version Name: " + versionName.replace("versionName", "").trim()  +
                    "\nVersion Code: " + code +
                    "\nTarget SDK: " + targetSdk;
        }
    }
}
