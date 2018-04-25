package application.device;

import application.utilities.ADB;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Intent implements Comparable<Intent> {
    public static final int ACTIVITY = 0;
    public static final int BROADCAST = 1;
    public static final int SERVICE = 2;

    private StringProperty action;
    private StringProperty category;
    private ObservableList<StringProperty> components;
    protected Integer intentType;
    protected Boolean isMimeTyped;

    public Intent(String action, ObservableList<StringProperty> components, int intentType, boolean isMimeTyped) {
        this.action = new SimpleStringProperty(action);
        this.category = new SimpleStringProperty("android.intent.category.DEFAULT");
        this.components = components;
        this.intentType = intentType;
        this.isMimeTyped = isMimeTyped;
    }


    public static String send(String action, String component, String category, String mimeType, String data, int type) {
        return ADB.sendIntent(action, component, category, mimeType, data, type);
    }

    public static ObservableList<String> getAssociatedMimeTypes(String packageName, String componentName, int intentType) {
        return ADB.getAssociatedMimeTypes(packageName, componentName, intentType);
    }

    public static ObservableList<String> getAssociatedSchemes(String packageName, String componentName, int intentType) {
        return ADB.getAssociatedSchemes(packageName, componentName, intentType);
    }

    //Properties
    public StringProperty actionProperty() {
        return action;
    }

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

    public boolean takesData() {
        return isMimeTyped;
    }

    public int getIntentType() {
        return intentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.category, this.components, this.action, this.intentType, this.isMimeTyped);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Intent && toString().equals(obj.toString());
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

    @Override
    public int compareTo(@NotNull Intent o) {
        return this.toString().compareTo(o.toString());
    }
}
