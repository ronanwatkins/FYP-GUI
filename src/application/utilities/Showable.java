package application.utilities;

import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;

/**
 * Any class that implements this interface can pop out of the main GUI, usually through the click of a button
 * on it's parent controller
 * @author Ronan Watkins
 * @param <T> any class that implements in Initializable interface
 */
public interface Showable<T extends Initializable> {
    /**
     * Provide an implementation of this method to allow your controller to pop out separate to the main GUI
     * @param controller
     * @param object
     * @return Instance of self attached to FXML view once created from the {@link javafx.fxml.FXMLLoader#load(URL)} method
     * @throws IOException if {@link URL} is incorrect
     */
    Initializable newWindow(T controller, Object object) throws IOException;
}