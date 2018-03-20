package application.location;

import application.TelnetServer;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class LocationTabController implements Initializable {

    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;

    @FXML
    private GoogleMapView googleMapView;

    @FXML
    private Button sendButton;

    private GoogleMap map;

    private DecimalFormat formatter = new DecimalFormat("###.00000");

    //Initalize to latitude of GMIT
    private double latitude = 53.278458;
    private double longitude = -9.009833;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        latitudeField.setText(formatter.format(latitude));
        longitudeField.setText(formatter.format(longitude));
        googleMapView.addMapInializedListener(() -> configureMap());
        sendButton.setOnAction(event -> TelnetServer.setLocation(longitude + " " + latitude));
    }

    private void configureMap() {
        MapOptions mapOptions = new MapOptions();

        mapOptions.streetViewControl(false);

        mapOptions.center(new LatLong(latitude, longitude))
                .mapType(MapTypeIdEnum.ROADMAP)
                .zoom(9);
        map = googleMapView.createMap(mapOptions, false);

        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();

            latitude = Double.parseDouble(formatter.format(latLong.getLatitude()));
            longitude = Double.parseDouble(formatter.format(latLong.getLongitude()));

            latitudeField.setText(formatter.format(latitude));
            longitudeField.setText(formatter.format(longitude));
        });
    }
}
