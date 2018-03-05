package application.connect;

import application.HTTPServer;
import application.TelnetServer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectTabController implements Initializable {

    private final String LIGHT = "light";
    private final String ACCELEROMETER = "accelerometer";
    private final String HUMIDITY = "humidity";
    private final String PRESSURE = "pressure";
    private final String MAGNETOMETER = "magnetic-field";
    private final String PROXIMITY = "pressure";
    private final String TEMPERATURE = "temperature";
    private final String LOCATION = "location";
    private final String BATTERY = "battery";

    private final ArrayList<String> sensors = new ArrayList<String>();

    private final int PORT = 80;
    @FXML
    private Label connectLabel;

    @FXML
    private Button connectPhone;

    @FXML
    private ListView dataList;

    private String IPAddress = getIPAddress();

    private boolean isConnected = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        sensors.add(LIGHT);
        sensors.add(ACCELEROMETER);
        sensors.add(PRESSURE);
        sensors.add(PROXIMITY);
        sensors.add(HUMIDITY);
        sensors.add(TEMPERATURE);
        sensors.add(MAGNETOMETER);

        dataList.setVisible(false);
        //dataList.setDisable(true);

        connectPhone.setOnAction(event -> {

            updateLabelText();
            connectPhone.setDisable(true);

            try {
                ServerSocket ss = new ServerSocket();
                ss.bind(new InetSocketAddress(IPAddress, PORT));

                Task task = new Task<Void>() {

                    @Override public Void call() {
                        try {
                            while(true) {
                                Socket cs = ss.accept();
                                PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
                                BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));

                                String request;
                                String clString = "";
                                do {
                                    request = br.readLine();

                                    if (request.toLowerCase().startsWith("content-length")) clString = request;
                                    if (request.isEmpty()) break;
                                } while (true);

                                int contentLength = Integer.parseInt(clString.substring(16));
                                //System.out.println("Num: " + contentLength);

                                final char[] contents = new char[contentLength + 2];
                                br.read(contents);
                                String POSTContent = decodePOSTString(new String(contents));
                                //System.out.println("Content: " + POSTContent);
                                //System.out.println("done");

                                out.print("HTTP/ 1.1 200 OK\n" +
                                        "Content-Type: text/html\n" +
                                        "Content-Length: 20\n" +
                                        "Connection: keep-alive\n\n" +
                                        "RESPONSE OK");
                                out.flush();
                                cs.close();

                                isConnected = true;
                                displayAndSendData(POSTContent.substring(POSTContent.indexOf('{')));
                            }

                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                        return null;
                    }
                };

                new Thread(task).start();

            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });
    }

    private synchronized void displayAndSendData(String jsonString) {

        if(isConnected) {
            clearLabelText();
            dataList.setVisible(true);
        }

        Platform.runLater(() -> {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);

                dataList.getItems().clear();
                dataList.getItems().add("Light: " + jsonObject.get(LIGHT) + " (lux)");
                dataList.getItems().add("Pressure: " + jsonObject.get(PRESSURE) + " (hPa)");
                dataList.getItems().add("Humidity: " + jsonObject.get(HUMIDITY) + " (%)");
                dataList.getItems().add("Proximity: " + jsonObject.get(PROXIMITY) + " (cm)");
                dataList.getItems().add("Magnetometer: " + jsonObject.get(MAGNETOMETER) + " (uT)");
                dataList.getItems().add("Accelerometer: " + jsonObject.get(ACCELEROMETER) + " (m/s^2)");
                dataList.getItems().add("Temperature: " + jsonObject.get(TEMPERATURE) + " (°C)");
                dataList.getItems().add("Location: " + jsonObject.get(LOCATION) + " (°)");
                dataList.getItems().add("Battery level: " + jsonObject.get(BATTERY) + " (%)");

                TelnetServer.powerCapacity(jsonObject.getString(BATTERY));
                TelnetServer.setLocation(jsonObject.get(LOCATION).toString());

                for(String sensor: sensors)
                    TelnetServer.setSensor(sensor + " " + jsonObject.get(sensor).toString().replaceAll("[xyz]=", "").replaceAll(", ", ":"));
            } catch (JSONException je) {
                je.printStackTrace();
            }
        });
    }

    private String decodePOSTString(String input) {
        input = input.replace("%7B", "{");
        input = input.replace("%22", "\"");
        input = input.replace("%3A", ":");
        input = input.replace("%2C", ",");
        input = input.replace("%3D", "=");
        input = input.replace("%7D", "}");
        input = input.replace("+", " ");

        return input;
    }

    private void updateLabelText() {
        connectLabel.setText("Connect your Android phone to " + IPAddress + ", port " + PORT +
                "\nWaiting for connection...");
    }

    private synchronized void clearLabelText() {
        Platform.runLater(() -> connectLabel.setText(""));
    }

    private String getIPAddress() {
        String ip = "";

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet6Address) continue;
                    if (iface.getDisplayName().toLowerCase().contains("vmware")) continue;

                    ip = addr.getHostAddress();
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }

        return ip;
    }
}
