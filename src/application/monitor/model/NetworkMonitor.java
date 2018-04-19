package application.monitor.model;

public class NetworkMonitor {
    private static NetworkMonitor instance = new NetworkMonitor();

    public static NetworkMonitor getInstance() {
        return instance;
    }

    private NetworkMonitor() {
    }
}
