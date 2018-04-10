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
}
