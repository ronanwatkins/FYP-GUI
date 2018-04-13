package application.device;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

public class DeviceIntent extends Intent {
    private StringProperty action;
    private StringProperty category;
    private ObservableList<StringProperty> components;

    public DeviceIntent(String action, ObservableList<StringProperty> components, IntentType intentType, boolean isMimeTyped) {
        this.action = new SimpleStringProperty(action);
        this.category = new SimpleStringProperty("android.intent.category.DEFAULT");
        this.components = components;
        this.intentType = intentType;
        this.isMimeTyped = isMimeTyped;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.components, this.action, this.intentType, this.isMimeTyped);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeviceIntent && toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(StringProperty component : components)
            stringBuilder.append(component.get()).append("\n");

        return  "Action: " + action.get() + "\n"
                + "Component: " + stringBuilder.toString()
                + "Category: " + category.get() + "\n"
                + "Intent Type: " + intentType.toString().toLowerCase() + "\n"
                + "is Mime Type: " + isMimeTyped + "\n";
    }
}
