package application.connect;

import application.HTTPServer;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ConnectTabController implements Initializable {

    private final int PORT = 80;
    @FXML
    private Label connectLabel;

    @FXML
    private Button connectPhone;

    private String IPAddress = getIPAddress();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        connectPhone.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                updateLabelText();

                try {
                    ServerSocket ss = new ServerSocket();
                    ss.bind(new InetSocketAddress(IPAddress, PORT));

                    Task task = new Task<Void>() {
                        @Override public Void call() {
                            try (Socket cs = ss.accept();
                                 PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
                                 BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                                 ){

                                String request = "";
                                do {
                                    request = br.readLine();
                                    System.out.println(request);
                                    //out.println(":)");
                                } while(true);

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
            }
        });
    }

    private void updateLabelText() {
        connectLabel.setText("Connect your Android phone to " + IPAddress + ", port " + PORT +
                "\nWaiting for connection...");
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
