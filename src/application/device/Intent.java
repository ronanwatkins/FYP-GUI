package application.device;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class Intent implements Comparable<Intent> {
    protected StringProperty component;
    private ObservableList<StringProperty> actions;
    private ObservableList<StringProperty> categories;
    private ObservableList<StringProperty> mimeTypes;
    protected IntentType intentType;
    protected Boolean isMimeTyped;

    public Intent() {}

    public Intent(StringProperty component, ArrayList<StringProperty> actions, ArrayList<StringProperty> categories, ArrayList<StringProperty> mimeTypes, boolean isMimeTyped) {
        this.component = component;
        this.actions = FXCollections.observableArrayList(actions);
        this.categories = FXCollections.observableArrayList(categories);
        this.mimeTypes = FXCollections.observableArrayList(mimeTypes);
        this.isMimeTyped = isMimeTyped;
    }

    public StringProperty componentProperty() {
        return component;
    }

    public ObservableList<StringProperty> actionProperty() {
        return actions;
    }

    public ObservableList<StringProperty> categoryProperty() {
        return categories;
    }

    public ObservableList<StringProperty> mimeTypeProperty() {
        return mimeTypes;
    }

    //Getters
    public String getComponent() {
        return component.get();
    }

    @Override
    public int hashCode() {
       // System.out.println("hashCode");
        return Objects.hash(this.component, this.actions, this.categories, this.mimeTypes, this.isMimeTyped);
       // Objects.
    }

    @Override
    public boolean equals(Object obj) {
        //System.out.println("equals");
        return obj instanceof Intent && toString().equals(obj.toString());
    }



    @Override
    public String toString() {
        StringBuilder action = new StringBuilder();
        for(StringProperty s : actions) {
            action.append(s.get()).append(" ");
        }
        StringBuilder category = new StringBuilder();
        for(StringProperty s : categories) {
            category.append(s.get()).append(" ");
        }
        StringBuilder type = new StringBuilder();
        for(StringProperty s : mimeTypes) {
            type.append(s.get()).append(" ");
        }
        return "Component: " + component.get() + "\n"
                + "Actions: " + action + "\n"
                + "Categories: " + category + "\n"
                + "Types: " + type + "\n";
    }

    @Override
    public int compareTo(@NotNull Intent o) {
        return this.isMimeTyped.compareTo(o.isMimeTyped);
    }
}
