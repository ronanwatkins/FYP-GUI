package application.logcat.filter;

import org.jetbrains.annotations.NotNull;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class Bundle extends ResourceBundle {

    private String bundle;

    public Bundle(String bundle) {
        this.bundle = bundle;
    }

    public String getBundle() {
        return bundle;
    }

    @Override
    public String toString() {
        return bundle;
    }

    @Override
    protected Object handleGetObject(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public Enumeration<String> getKeys() {
        return null;
    }
}
