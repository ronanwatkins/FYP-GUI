package application.utilities;

import javafx.fxml.Initializable;

import java.io.File;
import java.io.IOException;

public interface Showable<T extends Initializable> {
    void newWindow(T controller, File file) throws IOException;
}