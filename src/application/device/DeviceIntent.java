package application.device;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class DeviceIntent extends Intent {
    private StringProperty action;

    public DeviceIntent(String action, String component, IntentType intentType, boolean isMimeTyped) {
        this.action = new SimpleStringProperty(action);
        this.component = new SimpleStringProperty(component);
        this.intentType = intentType;
        this.isMimeTyped = isMimeTyped;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.component, this.action, this.intentType, this.isMimeTyped);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeviceIntent && toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return "Component: " + component + "\n"
                + "Actions: " + action + "\n"
                + "Categories: " + "android.intent.category.DEFAULT" + "\n"
                + "Intent Type: " + intentType.toString().toLowerCase() + "\n"
                + "is Mime Type: " + isMimeTyped + "\n";
    }
}
