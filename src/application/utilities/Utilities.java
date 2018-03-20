package application.utilities;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Utilities {

    public static void setImage(String URL, Button button){
        Image image = new Image(URL,20,20,true,true);
        ImageView imageView = new ImageView(image);
        button.setGraphic(imageView);
    }
}
