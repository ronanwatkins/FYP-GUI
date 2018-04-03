package application.applications;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class Intent {
    private StringProperty component;
    private ObservableList<StringProperty> actions;
    private ObservableList<StringProperty> categories;
    private ObservableList<StringProperty> mimeTypes;

    public Intent(StringProperty component, ArrayList<StringProperty> actions, ArrayList<StringProperty> categories, ArrayList<StringProperty> mimeTypes) {
        this.component = component;
        this.actions = FXCollections.observableArrayList(actions);
        this.categories = FXCollections.observableArrayList(categories);
        this.mimeTypes = FXCollections.observableArrayList(mimeTypes);
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
}
