package application.device;

import application.utilities.ADB;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DeviceIntent extends Intent {
    private StringProperty action;
    private StringProperty category;
    private ObservableList<StringProperty> components;

    public DeviceIntent(String action, ObservableList<StringProperty> components, int intentType, boolean isMimeTyped) {
        this.action = new SimpleStringProperty(action);
        this.category = new SimpleStringProperty("android.intent.category.DEFAULT");
        this.components = components;
        this.intentType = intentType;
        this.isMimeTyped = isMimeTyped;
    }

    public static ObservableList<String> getAssociatedMimeTypes(String packageName, String componentName, int intentType) {
        return ADB.getAssociatedMimeTypes(packageName, componentName, intentType);
    }

    //Properties
    public StringProperty actionProperty() {
        return action;
    }

    @Override
    public StringProperty componentProperty() {
        StringBuilder stringBuilder = new StringBuilder();
        for(StringProperty component : components)
            stringBuilder.append(component.get()).append("\n");

        return new SimpleStringProperty(stringBuilder.toString());
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty isMimeTypedProperty() {
        return new SimpleStringProperty(this.isMimeTyped.toString());
    }

    public StringProperty intentTypeProperty() {
        SimpleStringProperty value = null;

        switch (this.intentType) {
           case ACTIVITY:
               value = new SimpleStringProperty("Activity");
               break;
           case BROADCAST:
               value = new SimpleStringProperty("Broadcast");
               break;
           case SERVICE:
               value = new SimpleStringProperty("Service");
               break;
        }

        return value;
    }

    //Getters
    public ObservableList<StringProperty> getComponentPropertiess() {
        return components;
    }

    public ObservableList<String> getComponents() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for(StringProperty stringProperty : components)
            list.add(stringProperty.get());

        return list;
    }

    public int getIntentType() {
        return intentType;
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
                + "Intent Type: " + this.intentTypeProperty().get() + "\n"
                + "is Mime Type: " + isMimeTyped + "\n";
    }
}
