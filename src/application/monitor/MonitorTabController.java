package application.monitor;

import application.applications.ApplicationTabController;
import application.device.AndroidApplication;
import application.device.Device;
import application.monitor.model.CPUMonitor;
import application.monitor.model.MemoryMonitor;
import application.monitor.model.NetworkMonitor;
import application.utilities.ApplicationUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorTabController extends ApplicationTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(MonitorTabController.class.getName());

    @FXML
    private Label cpuLabel;

    @FXML
    private Button startButton;
    @FXML
    private Button refreshButton;

    @FXML
    private SplitPane verticalPane;

    @FXML
    private AreaChart<Number, Number> CPUChart;
    @FXML
    private AreaChart<Number, Number> MemoryChart;
    @FXML
    private AreaChart<Number, Number> NetworkChart;

    private XYChart.Series<Number, Number> CPUDataSeries;
    private Timeline CPUAnimation;

    private XYChart.Series<Number, Number> MemoryDataSeries;
    private Timeline MemoryAnimation;

    private XYChart.Series<Number, Number> NetworkDataSeriesSent;
    private XYChart.Series<Number, Number> NetworkDataSeriesReceived;
    private Timeline NetworkAnimation;

    private int sequence = 0;

    private Device device = Device.getInstance();

    private Thread monitorServiceThread;
    private MonitorService monitorService = MonitorService.getInstance();
    private CPUMonitor cpuMonitor = CPUMonitor.getInstance();
    private MemoryMonitor memoryMonitor = MemoryMonitor.getInstance();
    private NetworkMonitor networkMonitor = NetworkMonitor.getInstance();

    private Random rand = new Random();

    private IntegerProperty CPUPercentage = new SimpleIntegerProperty();
    private IntegerProperty totalMemory = new SimpleIntegerProperty();
    private IntegerProperty freeMemory = new SimpleIntegerProperty();
    private LongProperty sentBytes = new SimpleLongProperty();
    private LongProperty receivedBytes = new SimpleLongProperty();

    private long lastSentKiloBits;
    private long lastReceivedKiloBits;

    private double usedMemoryPercentage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verticalPane.setDividerPositions(0.3333f, 0.6666f, 0.9999f);

        initializeButtons();
        initializeCPUChart();
        initializeMemoryChart();
        initializeNetworkChart();

    }

    private void initializeNetworkChart() {
        NetworkAnimation = new Timeline();
        NetworkAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {

            System.out.println("Sent bytes: " + sentBytes.get());
            long sentKiloBits = (sentBytes.get() * 8)/1000;
            double sentDifference = (double)((sentKiloBits - lastSentKiloBits)/1000);
            System.out.println("Sent kilo bits: " + sentKiloBits);
            System.out.printf("sent difference: %.1f", sentDifference);
            lastSentKiloBits = sentKiloBits;

            System.out.println("\nReceived bytes: " + receivedBytes.get());
            long receivedKiloBits = (receivedBytes.get() * 8)/1000;
            double receivedDifference = (double)((receivedKiloBits - lastReceivedKiloBits)/1000);
            System.out.println("Received kilo bits: " + receivedKiloBits);
            System.out.printf("received difference: %.1f", receivedDifference);
            lastReceivedKiloBits = receivedKiloBits;

            System.out.println("\n");

            NetworkDataSeriesSent.getData().add(new XYChart.Data<>(sequence, sentDifference));
            if(NetworkDataSeriesSent.getData().size() > 10) {
                NetworkDataSeriesSent.getData().remove(0);
            }

            NetworkDataSeriesReceived.getData().add(new XYChart.Data<>(sequence, receivedDifference));
            if(NetworkDataSeriesReceived.getData().size() > 10) {
                NetworkDataSeriesReceived.getData().remove(0);
            }
        }));
        NetworkAnimation.setCycleCount(Animation.INDEFINITE);

        NetworkDataSeriesSent = new XYChart.Series<>();
        NetworkDataSeriesReceived = new XYChart.Series<>();

        NetworkChart.setLegendVisible(false);

        NetworkChart.getData().add(NetworkDataSeriesSent);
        NetworkChart.getData().add(NetworkDataSeriesReceived);
        NetworkChart.setCreateSymbols(false);

        NetworkDataSeriesSent.nodeProperty().get().setStyle("-fx-stroke-width: 1px;");

        NetworkChart.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-fill: #85e085;");
        NetworkChart.setStyle("CHART_COLOR_1: #49d049;");
    }

    private void initializeMemoryChart() {
        MemoryAnimation = new Timeline();
        MemoryAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {

            double usedMemory = totalMemory.get() - freeMemory.get();
            double totalMem = totalMemory.get();

            usedMemoryPercentage = ((usedMemory / totalMem)*100);

            MemoryDataSeries.getData().add(new XYChart.Data<>(sequence, usedMemoryPercentage));
            if(MemoryDataSeries.getData().size() > 10) {
                MemoryDataSeries.getData().remove(0);
            }
        }));
        MemoryAnimation.setCycleCount(Animation.INDEFINITE);

        MemoryDataSeries = new XYChart.Series<>();
        MemoryDataSeries.setName("");

        MemoryChart.setLegendVisible(false);

        MemoryChart.getData().add(MemoryDataSeries);
        MemoryChart.setCreateSymbols(false);

        MemoryChart.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-fill: #99d6ff;");
        MemoryChart.setStyle("CHART_COLOR_1: #33adff;");
    }

    private void initializeCPUChart() {
        CPUAnimation = new Timeline();
        CPUAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
            CPUDataSeries.getData().add(new XYChart.Data<>(++sequence, CPUPercentage.get()));
            if(CPUDataSeries.getData().size() > 10) {
                CPUDataSeries.getData().remove(0);
            }
        }));
        CPUAnimation.setCycleCount(Animation.INDEFINITE);

        CPUDataSeries = new XYChart.Series<>();
        CPUDataSeries.setName("");

        CPUChart.setLegendVisible(false);

        CPUChart.getData().add(CPUDataSeries);
        CPUChart.setCreateSymbols(false);

        CPUDataSeries.getNode().setStyle("-fx-bar-fill: #4CAF50");
    }

    @FXML
    protected void handleStartButtonClicked(MouseEvent event) {
        if(startButton.getText().equals("Start"))
            play();
        else
            stop();
    }

    public void play() {
        Log.info("Playing...");

        monitorServiceThread = new Thread(monitorService);
        monitorServiceThread.start();

        bindLabels();

        MemoryAnimation.play();
        CPUAnimation.play();
        NetworkAnimation.play();

        startButton.setText("Stop");
    }

    private void bindLabels() {
        CPUPercentage.bindBidirectional(cpuMonitor.systemCPUPercentageUtilizationProperty());
        freeMemory.bindBidirectional(memoryMonitor.freeMemoryProperty());
        totalMemory.bindBidirectional(memoryMonitor.totalMemoryProperty());

        sentBytes.bindBidirectional(networkMonitor.systemSentBytesProperty());
        receivedBytes.bindBidirectional(networkMonitor.systemReceivedBytesProperty());

        cpuLabel.textProperty().bindBidirectional(cpuMonitor.CPUVendorProperty());
    }

    public void stop() {
        Log.info("Stopping...");

        Log.info("GOING TO INTERRUPT!!!!!!");
        //monitorService.interrupt();
        Log.info("is cancelled? " + monitorService.cancel());
        monitorServiceThread.interrupt();
        Log.info("INTERRUPTED!!!!!!!");

        //monitorServiceThread.interrupt();

        MemoryAnimation.pause();
        CPUAnimation.pause();
        NetworkAnimation.pause();

        startButton.setText("Start");
    }

    @FXML
    private void handleAppsListViewClicked(MouseEvent mouseEvent) {
        String applicationName = appsOnDeviceListView.getSelectionModel().getSelectedItem();

        Task<AndroidApplication> task = new Task<AndroidApplication>() {
            @Override
            protected AndroidApplication call() {
                return new AndroidApplication(applicationName);
            }
        };
        task.setOnSucceeded(event -> {
            device.setSelectedApplication(task.getValue());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleRefreshButtonClicked(ActionEvent event) {
        Log.info("");

        updateDeviceListView();
    }

    @Override
    public void initializeButtons() {
        setImage("/resources/refresh.png", null, refreshButton);
    }
}
