package application.sensors.server;

import application.TelnetServer;
import application.sensors.SensorsTabController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class HTTPServer {
    private SensorsTabController controller;
    private ServerSocket ss;
    private final int PORT = 1338;
    private String IPAddress;

    private double initialYawValue;

    private double latitude;
    private double longitude;
    private double battery;

    private AtomicBoolean isListening = new AtomicBoolean(true);
    private boolean isConnected = false;

    public HTTPServer(SensorsTabController controller) throws IOException {
        this.controller = controller;
        ss = new ServerSocket();

        IPAddress = getIPAddress();
        System.out.println(IPAddress);

        try {
            ss.bind(new InetSocketAddress(IPAddress, PORT));
        } catch (BindException be) {
            //NOOP
        }

        initialYawValue = 0;
    }

    public void listen() throws IOException {
        Task<Void> task = new Task<Void>() {

            @Override public Void call() {
                try {
                    while(true) {
                        Socket cs = ss.accept();
                        if(!isConnected) {
                            isConnected = true;
                            controller.setConnected(isConnected);
//                            Platform.runLater(() -> {
//
//                            });
                        }

                        BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));

                        String request;
                        String clString = "";

                        String method = null;
                        do {
                            request = br.readLine();
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
                            br.read(contents);
                            String POSTContent = URLDecoder.decode(new String(contents), "UTF-8");

                            displayAndSendData(POSTContent.substring(POSTContent.indexOf('{')));
                        }

                        OutputStream os = cs.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        pw.print("HTTP/1.1 200 OK\n" +
                                "Content-Type: text/html\n" +
                                "Connection: keep-alive\n\n" +
                                "OK");

                        pw.close();
                        os.close();
                        cs.close();
                    }

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (JSONException jse) {
                    jse.printStackTrace();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    public void setIsListening(boolean flag) {
        isListening.set(flag);
    }

    private synchronized void displayAndSendData(String jsonString) throws JSONException {
        if(isListening.get()) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);

                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();

                    switch (key) {
                        case SensorsTabController.ORIENTATION:
                            double yaw = Double.parseDouble((String) jsonObject.getJSONArray(key).get(0));
                            final double pitch = Double.parseDouble((String) jsonObject.getJSONArray(key).get(1));
                            final double roll = Double.parseDouble((String) jsonObject.getJSONArray(key).get(2));

                            yaw *= -1;

                            if (initialYawValue == 0)
                                initialYawValue = yaw;

                            yaw += 180 - initialYawValue;

                            if (yaw > 180)
                                yaw -= 360;

                            final double newYaw = yaw;

                            Platform.runLater(() -> {
                                controller.yawSlider.setValue(newYaw*-1);
                                controller.pitchSlider.setValue(pitch);
                                controller.rollSlider.setValue(roll*-1);
                            });
                            break;
                        case SensorsTabController.LIGHT:
                            double light = jsonObject.getDouble(key);
                            if(controller.lightSlider.getValue() != light)
                                Platform.runLater(() -> controller.lightSlider.setValue(light));
                            break;
                        case SensorsTabController.HUMIDITY:
                            double humidity = jsonObject.getDouble(key);
                            if(controller.humiditySlider.getValue() != humidity)
                                Platform.runLater(() -> controller.humiditySlider.setValue(humidity));
                            break;
                        case SensorsTabController.TEMPERATURE:
                            double temperature = jsonObject.getDouble(key);
                            if(controller.temperatureSlider.getValue() != temperature)
                                Platform.runLater(() -> controller.temperatureSlider.setValue(temperature));
                            break;
                        case SensorsTabController.PRESSURE:
                            double pressure = jsonObject.getDouble(key);
                            if(controller.pressureSlider.getValue() != pressure)
                                Platform.runLater(() -> controller.pressureSlider.setValue(pressure));
                            break;
                        case SensorsTabController.PROXIMITY:
                            double proximity = jsonObject.getDouble(key);
                            if(controller.proximitySlider.getValue() != proximity)
                                Platform.runLater(() -> controller.proximitySlider.setValue(proximity));
                            break;
                        case SensorsTabController.BATTERY:
                            if(battery != jsonObject.getDouble(key)) {
                                battery = jsonObject.getDouble(key);
                                TelnetServer.powerCapacity(""+battery);
                                Platform.runLater(() -> controller.batteryLabel.setText(""+battery));
                            }
                            break;
                        case SensorsTabController.LOCATION:
                            if(latitude != (Double) jsonObject.getJSONArray(key).get(0) || longitude != (Double) jsonObject.getJSONArray(key).get(1)){
                                latitude =  (Double) jsonObject.getJSONArray(key).get(0);
                                longitude = (Double) jsonObject.getJSONArray(key).get(1);
                                TelnetServer.setLocation(longitude + " " + latitude);
                                Platform.runLater(() -> controller.locationLabel.setText("Latidude: " + latitude + "\n" + "Longitude: " + longitude));
                            }
                            break;
                    }
                }
            } catch (JSONException je) {
                je.printStackTrace();
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
