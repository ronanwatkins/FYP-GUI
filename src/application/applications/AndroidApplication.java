package application.applications;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

import static application.utilities.ADB.*;

public class AndroidApplication {
    private StringProperty packageName;
    private StringProperty APKName;
    private StringProperty APKPath;
    private VersionCode versionCode;
    private IntegerProperty userId;
    private StringProperty dataDir;
    private ObservableList<StringProperty> flags;
    private ObservableList<StringProperty> permissions;
    private ObservableList<Intent> intents;
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
    }

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

    public ObservableList<Intent> intents() { return intents; }

    public class VersionCode {
        private String versionName;
        private int code;
        private int minSdk;
        private int targetSdk;

        public VersionCode() {
            versionName = getVersionName(packageName.get());
            String[] version = getVersionCode(packageName.get()).split(" ");
            code = Integer.parseInt(version[0].split("=")[1]);
            minSdk = Integer.parseInt(version[1].split("=")[1]);
            targetSdk = Integer.parseInt(version[2].split("=")[1]);
        }

        @Override
        public String toString() {
            return "Ver Name: " + versionName  +
                    "\nVer code: " + code +
                    "\nminSdk: " + minSdk +
                    "\ntargetSdk: " + targetSdk;
        }
    }
}
