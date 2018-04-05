package application.utilities;

import application.logcat.Filter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.net.URI;

public interface ApplicationUtils {
    void initializeButtons();

    default void browse(String URL) {
        try {
            URI uri = new URI(URL);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    default void setImage(String URL, String text, Button button) {
        if(URL != null) {
            Image image = new Image(URL, 20, 20, true, true);
            ImageView imageView = new ImageView(image);
            button.setGraphic(imageView);
        }
        if(text != null) {
            button.setTooltip(new Tooltip(text));
        }
    }

    default ObservableList<String> filter(String searchText, ObservableList<String> list) {
        ObservableList<String> newList = null;
        if(searchText != null) {
            newList = FXCollections.observableArrayList();

            for (String s : list) {
                if (s.contains(searchText)) {
                    newList.add(s);
                }
            }
        }

        return newList;
    }

    default ObservableList<String> filter(Filter filter, ObservableList<String> list) {
        ObservableList<String> newList = null;
        if(filter.getSearchText() != null) {
            newList = FXCollections.observableArrayList();

            for (String s : list) {
                if(s.startsWith("-"))
                    continue;

                s = s.replace("  ", " ");
                String level = s.split(" ")[4].trim();

                String logLevel1 = filter.getLogLevel().substring(0,1).trim();
                String logLevel2 = filter.getLogLevel2().substring(0,1).trim();

                if(!logLevel1.equals("N"))
                    if(!level.equals(logLevel1))
                        continue;

                if(!logLevel2.equals("N"))
                    if(!level.equals(logLevel2))
                        continue;

                if (s.contains(filter.getSearchText()) &&
                    s.contains(filter.getApplicationName()) &&
                    s.contains(filter.getPID()) &&
                    s.contains(filter.getLogTag()) &&
                    s.contains(filter.getLogMessage())) {
                    newList.add(s);
                }
            }
        }

        return newList;
    }

    default boolean isOK(Filter filter, String string) {
        boolean result = false;

        if(string.startsWith("-"))
            return true;

        string = string.replace("  ", " ");
        String level = string.split(" ")[4].trim();

        String filter1 = filter.getLogLevel().substring(0,1).trim();
        String filter2 = filter.getLogLevel2().substring(0,1).trim();

        if(!filter1.equals("N"))
            if(!level.equals(filter1))
                return false;

        if(!filter2.equals("N"))
            if(!level.equals(filter2))
                return false;

        if (string.contains(filter.getSearchText()) &&
                string.contains(filter.getApplicationName()) &&
                string.contains(filter.getPID()) &&
                string.contains(filter.getLogTag()) &&
                string.contains(filter.getLogMessage())) {
            result = true;
        }
        return result;
    }
}
