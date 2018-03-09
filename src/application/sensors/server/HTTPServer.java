package application.sensors.server;

import application.TelnetServer;
import application.sensors.SensorsTabController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;

public class HTTPServer {
    private SensorsTabController controller;
    private ServerSocket ss;
    private final int PORT = 80;
    private String IPAddress;

    private boolean isConnected = false;

    public HTTPServer(SensorsTabController controller) throws IOException {
        this.controller = controller;
        ss = new ServerSocket();

        IPAddress = getIPAddress();
        System.out.println(IPAddress);
        ss.bind(new InetSocketAddress(IPAddress, PORT));
    }

    public void listen() throws IOException {
        Task task = new Task<Void>() {

            @Override public Void call() {
                try {
                    while(true) {
                        System.out.println("Here");
                        Socket cs = ss.accept();
                        System.out.println("connected");
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
                        System.out.println(POSTContent);

                        displayAndSendData(POSTContent.substring(POSTContent.indexOf('{')));
                        //displayAndSendData(POSTContent);
                    }

                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private synchronized void displayAndSendData(String jsonString) {

        Platform.runLater(() -> {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);

                System.out.println(jsonObject.get("light"));
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
