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
import javafx.beans.property.*;
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

public class MonitorTabController extends ApplicationTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(MonitorTabController.class.getName());

    @FXML
    private Label CPUUtilizationPercentageLabel;
    @FXML
    private Label CPUProcessesLabel;
    @FXML
    private Label CPUUptimeLabel;
    @FXML
    private Label CPUMaximumSpeedLabel;
    @FXML
    private Label CPUSpeedLabel;
    @FXML
    private Label CPUThreadsLabel;
    @FXML
    private Label CPUVendorLabel;
    @FXML
    private Label CPUCoresLabel;
    @FXML
    private Label memoryUsageSystemTotal;
    @FXML
    private Label memoryUsageSystemPercentage;
    @FXML
    private Label memoryUsageApplicationTotal;
    @FXML
    private Label memoryUsageApplicationPercentage;
    @FXML
    private Label memoryAvailableLabel;
    @FXML
    private Label memoryTotalLabel;
    @FXML
    private Label NetworkSentKBps;
    @FXML
    private Label NetworkReceivedKBps;

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

    private IntegerProperty CPUPercentage = new SimpleIntegerProperty();
    private IntegerProperty totalMemory = new SimpleIntegerProperty();
    private IntegerProperty freeMemory = new SimpleIntegerProperty();
    private DoubleProperty receivedKBps = new SimpleDoubleProperty();
    private DoubleProperty sentKBps = new SimpleDoubleProperty();

    private DoubleProperty usedMemoryPercentageSystem = new SimpleDoubleProperty();
    private DoubleProperty usedMemoryGBSystem = new SimpleDoubleProperty();
    private DoubleProperty usedMemoryPercentageApplication = new SimpleDoubleProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verticalPane.setDividerPositions(0.3333f, 0.6666f, 0.9999f);

        initializeButtons();
        initializeCPUChart();
        initializeMemoryChart();
        initializeNetworkChart();

    }

    private Random random = new Random();

    private void initializeNetworkChart() {
        NetworkAnimation = new Timeline();
        NetworkAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
            NetworkDataSeriesSent.getData().add(new XYChart.Data<>(sequence, receivedKBps.get()));
//            NetworkDataSeriesSent.getData().add(new XYChart.Data<>(sequence, random.nextInt(100)));
            if(NetworkDataSeriesSent.getData().size() > 10) {
                NetworkDataSeriesSent.getData().remove(0);
            }

            NetworkDataSeriesReceived.getData().add(new XYChart.Data<>(sequence, receivedKBps.get()));
//            NetworkDataSeriesReceived.getData().add(new XYChart.Data<>(sequence, random.nextInt(100)));
            if(NetworkDataSeriesReceived.getData().size() > 10) {
                NetworkDataSeriesReceived.getData().remove(0);
            }
        }));
        NetworkAnimation.setCycleCount(Animation.INDEFINITE);

        NetworkDataSeriesSent = new XYChart.Series<>();
        NetworkDataSeriesReceived = new XYChart.Series<>();

        NetworkChart.setLegendVisible(false);

        //NetworkChart.getData().add(NetworkDataSeriesSent);
        NetworkChart.getData().add(NetworkDataSeriesReceived);
        NetworkChart.setCreateSymbols(false);


        //NetworkChart.setStyle("-fx-stroke-width: 10; -fx-stroke: #00FF00; -fx-stroke-dash-array: 2 12 12 2;");
        //NetworkDataSeriesSent.nodeProperty().get().setStyle("-fx-stroke-dash-array: 2d;");
        //NetworkDataSeriesSent.nodeProperty().get().setStyle("-fx-stroke: #80ff80;");

        //NetworkDataSeriesReceived.nodeProperty().get().setStyle("-fx-stroke: #00cc00;");

//        NetworkChart.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-stroke: #00cc00;");
//        NetworkChart.lookup(".default-color1.chart-series-area-fill").setStyle("-fx-stroke: red;");
      //  NetworkChart.setStyle("-fx-stroke-dash-array: 0.1 5.0;");
//        NetworkChart.setStyle("CHART_COLOR_2: black;");
//        NetworkChart.setStyle("CHART_COLOR_3: brown;");
//        NetworkChart.setStyle("CHART_COLOR_0: red;");

        NetworkChart.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-fill: #80ff80;");
        NetworkChart.setStyle("CHART_COLOR_1: #1aff1a;");
    }

    private void initializeMemoryChart() {
        MemoryAnimation = new Timeline();
        MemoryAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {

            double usedMemory = totalMemory.get() - freeMemory.get();
            usedMemoryGBSystem.setValue(usedMemory/1000000);

            double totalMem = totalMemory.get();

            usedMemoryPercentageSystem.setValue((usedMemory / totalMem)*100);
            usedMemoryPercentageApplication.setValue((memoryMonitor.applicationMemoryUsageProperty().getValue() / totalMem)*100);

            MemoryDataSeries.getData().add(new XYChart.Data<>(sequence, usedMemoryPercentageSystem.get()));
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
    protected void handleStartButtonClicked(ActionEvent event) {
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
        CPUPercentage.bind(cpuMonitor.systemCPUPercentageUtilizationProperty());
        freeMemory.bind(memoryMonitor.freeMemoryProperty());
        totalMemory.bind(memoryMonitor.totalMemoryProperty());

        sentKBps.bind(networkMonitor.systemSentKBpsProperty());
        receivedKBps.bind(networkMonitor.systemReceivedKBpsProperty());

        CPUVendorLabel.textProperty().bind(cpuMonitor.CPUVendorProperty());
        CPUMaximumSpeedLabel.textProperty().bind(cpuMonitor.maximumFrequencyProperty().asString("%.1f").concat(" GHz"));
        CPUUtilizationPercentageLabel.textProperty().bind(cpuMonitor.systemCPUPercentageUtilizationProperty().asString().concat("%"));
        CPUProcessesLabel.textProperty().bind(cpuMonitor.runningProcessesProperty().asString());
        //CPUUptimeLabel.textProperty().bind(cpuMonitor.systemUptimeProperty());
        CPUSpeedLabel.textProperty().bind(cpuMonitor.currentFrequencyProperty().asString("%.1f").concat(" GHz"));
        CPUThreadsLabel.textProperty().bind(cpuMonitor.runningThreadsProperty().asString());
        CPUVendorLabel.textProperty().bind(cpuMonitor.CPUVendorProperty());
        CPUCoresLabel.textProperty().bind(cpuMonitor.numberOfCoresProperty().asString());

        memoryUsageSystemTotal.textProperty().bind(usedMemoryGBSystem.asString("%.1f").concat(" GB"));
        memoryUsageSystemPercentage.textProperty().bind(usedMemoryPercentageSystem.asString("%.1f").concat("% "));
        memoryUsageApplicationTotal.textProperty().bind(memoryMonitor.applicationMemoryUsageProperty().divide(1000000.0).asString("%.1f").concat(" GB"));
        memoryUsageApplicationPercentage.textProperty().bind(usedMemoryPercentageApplication.asString("%.1f").concat("% "));
        memoryAvailableLabel.textProperty().bind(memoryMonitor.freeMemoryProperty().divide(1000000.0).asString("%.1f").concat(" GB"));
        memoryTotalLabel.textProperty().bind(memoryMonitor.totalMemoryProperty().divide(1000000.0).asString("%.1f").concat(" GB"));

        NetworkSentKBps.textProperty().bind(sentKBps.asString("%.1f").concat(" KBps"));
        NetworkReceivedKBps.textProperty().bind(receivedKBps.asString("%.1f").concat(" KBps"));
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
        Log.info("is cancelled? " + monitorService.cancel());
        monitorServiceThread.interrupt();

        String applicationName = appsOnDeviceListView.getSelectionModel().getSelectedItem();

        Task<AndroidApplication> task = new Task<AndroidApplication>() {
            @Override
            protected AndroidApplication call() {
                return new AndroidApplication(applicationName);
            }
        };
        task.setOnSucceeded(event -> {
            device.setSelectedApplication(task.getValue());

            monitorServiceThread = new Thread(monitorService);
            monitorServiceThread.start();

        });

        new Thread(task).start();
    }

    @Override
    @FXML
    protected void handleRefreshButtonClicked(ActionEvent event) {
        Log.info("");

        updateDeviceListView();
    }

    @Override
    public void initializeButtons() {
        setImage("/resources/refresh.png", null, refreshButton);
    }
}
