package application.connect;

import application.HTTPServer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String IPAddress = getIPAddress();


        //new Thread(new HTTPServer(IPAddress,PORT));
        connectPhone.setOnAction(new EventHandler<ActionEvent>() {
            boolean isConnected = false;
            @Override
            public void handle(ActionEvent event) {
                connectLabel.setText("Connect your Android phone to " + IPAddress + ", port " + PORT +
                        "\nWaiting for connection...");

                try {
                    ServerSocket ss = new ServerSocket();
                    ss.bind(new InetSocketAddress(IPAddress, PORT));

                    while (!isConnected) {
                        System.out.println("waiting");
                        Socket cs = ss.accept();
                        new Thread(new HTTPServer(cs, IPAddress, PORT)).start();
                        isConnected = true;
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });

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
