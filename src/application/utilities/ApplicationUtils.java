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
import java.util.Iterator;

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
        ObservableList<String> newList = FXCollections.observableArrayList();

        if(filter.getSearchText() != null) {

            for (String s : list) {
                if (matchesFilter(filter, s))
                    newList.add(s);
            }
        }

        return newList;
    }

    default boolean matchesFilter(Filter filter, String s) {
        if (s.startsWith("-"))
            return false;

        s = s.replace("  ", " ");
        String[] split = s.split(" ");
        String pid = split[2];
        String level = split[4];
        String tag = split[5];
        String message = s.substring(s.indexOf(tag));
        message = message.substring(message.indexOf(":")+1);

        String logLevel1 = filter.getLogLevel().substring(0, 1).trim();
        String logLevel2 = filter.getLogLevel2().substring(0, 1).trim();

        if (!logLevel1.equals("N"))
            if (!level.equals(logLevel1))
                return false;

        if (!logLevel2.equals("N"))
            if (!level.equals(logLevel2))
                return false;

        return s.contains(filter.getSearchText()) &&
                s.contains(filter.getApplicationName()) &&
                (filter.getPID().isEmpty() || pid.contains(filter.getPID())) &&
                (filter.getLogTag().isEmpty() || tag.contains(filter.getLogTag())) &&
                (filter.getLogMessage().isEmpty() || message.contains(filter.getLogMessage()));
    }
}
