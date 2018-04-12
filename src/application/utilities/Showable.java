package application.utilities;

import javafx.fxml.Initializable;

import java.io.File;
import java.io.IOException;

public interface Showable<T extends Initializable> {
    Initializable newWindow(T controller, Object object) throws IOException;
}