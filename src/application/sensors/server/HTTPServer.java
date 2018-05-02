package application.sensors.server;

import application.utilities.TelnetServer;
import application.sensors.SensorsTabController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class HTTPServer {
    private static final Logger Log = Logger.getLogger(HTTPServer.class.getName());

    private SensorsTabController controller;
    private ServerSocket serverSocket;

    private final int PORT = 1338;

    private double latitude;
    private double longitude;
    private double battery;


    private AtomicBoolean isListening = new AtomicBoolean(true);
    private boolean isConnected = false;

    public HTTPServer(SensorsTabController controller) throws IOException {
        this.controller = controller;
        serverSocket = new ServerSocket();

        final String IPAddress = getIPAddress();

        serverSocket.bind(new InetSocketAddress(IPAddress, PORT));
    }

    private class ReceiveJSONTask extends Task<Void> {
        private Socket socket;
        private boolean isFirstRun = true;

        private ReceiveJSONTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        protected Void call() {
            try {
                while(true) {
                    if(isFirstRun)
                        isFirstRun = false;
                    else
                        socket = serverSocket.accept();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String request;
                    String clString = "";

                    String method = null;
                    do {
                        request = bufferedReader.readLine();
                        if(method == null) {
                            if(request.startsWith("POST")) {
                                method = "POST";
                            }
                        }

                        if (request.toLowerCase().startsWith("content-length")) {
                            clString = request;
                        }
                        if (request.isEmpty()) break;
                    } while (true);

                    if(method !=  null) {
                        int contentLength = Integer.parseInt(clString.substring(16));
                        final char[] contents = new char[contentLength + 2];
                        bufferedReader.read(contents);
                        String POSTContent = URLDecoder.decode(new String(contents), "UTF-8");

                        displayAndSendData(POSTContent.substring(POSTContent.indexOf('{')));
                    }

                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    pw.print("HTTP/1.1 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "Connection: keep-alive\n\n" +
                            "OK");

                    pw.close();
                    os.close();
                    socket.close();
                }

            } catch (IOException ioe) {
                Log.error(ioe.getMessage(), ioe);
            }

            return null;
        }
    }

    public boolean listen() throws IOException {
        final Socket socket;

        socket = serverSocket.accept();

        Thread thread = new Thread(new ReceiveJSONTask(socket));
        thread.setDaemon(true);
        thread.start();

        return true;
    }

    public void setIsListening(boolean flag) {
        isListening.set(flag);
    }

    private void displayAndSendData(String jsonString) {
        if(isListening.get()) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);

                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();

                    switch (key) {
                        case SensorsTabController.ORIENTATION:
                            final double pitch = Double.parseDouble((String) jsonObject.getJSONArray(key).get(1));
                            final double roll = Double.parseDouble((String) jsonObject.getJSONArray(key).get(2));
                            Platform.runLater(() -> {
                                controller.pitchSlider.setValue(pitch);
                                controller.rollSlider.setValue(roll*-1);
                            });
                            break;
                        case SensorsTabController.LIGHT:
                            double light = jsonObject.getDouble(key);
                            Platform.runLater(() -> controller.lightSlider.setValue(light));
                            break;
                        case SensorsTabController.HUMIDITY:
                            double humidity = jsonObject.getDouble(key);
                            Platform.runLater(() -> controller.humiditySlider.setValue(humidity));
                            break;
                        case SensorsTabController.TEMPERATURE:
                            double temperature = jsonObject.getDouble(key);
                            Platform.runLater(() -> controller.temperatureSlider.setValue(temperature));
                            break;
                        case SensorsTabController.PRESSURE:
                            double pressure = jsonObject.getDouble(key);
                            Platform.runLater(() -> controller.pressureSlider.setValue(pressure));
                            break;
                        case SensorsTabController.PROXIMITY:
                            double proximity = jsonObject.getDouble(key);
                            Platform.runLater(() -> controller.proximitySlider.setValue(proximity));
                            break;
                        case SensorsTabController.BATTERY:
                            battery = jsonObject.getDouble(key);
                            TelnetServer.powerCapacity(""+battery);
                            Platform.runLater(() -> controller.batteryLabel.setText(""+battery));
                            break;
                        case SensorsTabController.LOCATION:
                            latitude =  (Double) jsonObject.getJSONArray(key).get(0);
                            longitude = (Double) jsonObject.getJSONArray(key).get(1);
                            TelnetServer.setLocation(longitude + " " + latitude);
                            Platform.runLater(() -> controller.locationLabel.setText("Latitude: " + latitude + "\n" + "Longitude: " + longitude));
                            break;
                    }
                }
            } catch (JSONException je) {
                Log.error(je.getMessage(), je);
            }
        }
    }

    public int getPORT() {
        return PORT;
    }

    public String getIPAddress() {
        String ip = "";

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (address instanceof Inet6Address) continue;
                    if (networkInterface.getDisplayName().toLowerCase().contains("vmware")) continue;

                    ip = address.getHostAddress();
                }
            }
        } catch (SocketException se) {
            Log.error(se.getMessage(), se);
        }

        return ip;
    }
}
