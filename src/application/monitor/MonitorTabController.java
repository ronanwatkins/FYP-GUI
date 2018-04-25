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
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.awt.*;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MonitorTabController extends ApplicationTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(MonitorTabController.class.getName());

    @FXML
    private NumberAxis xAxis;

    @FXML
    private Label CPUSystemUtilizationPercentageLabel;
    @FXML
    private Label CPUApplicationUtilizationPercentageLabel;
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
    private Label NetworkSystemSentKBps;
    @FXML
    private Label NetworkSystemReceivedKBps;
    @FXML
    private Label NetworkApplicationSentKBps;
    @FXML
    private Label NetworkApplicationReceivedKBps;

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

    private XYChart.Series<Number, Number> CPUDataSeriesSystem;
    private XYChart.Series<Number, Number> CPUDataSeriesApplication;
    private Timeline CPUAnimation;

    private XYChart.Series<Number, Number> MemoryDataSeriesSystem;
    private XYChart.Series<Number, Number> MemoryDataSeriesApplication;
    private Timeline MemoryAnimation;

    private XYChart.Series<Number, Number> NetworkDataSeriesSentSystem;
    private XYChart.Series<Number, Number> NetworkDataSeriesReceivedSystem;
    private XYChart.Series<Number, Number> NetworkDataSeriesSentApplication;
    private XYChart.Series<Number, Number> NetworkDataSeriesReceivedApplication;
    private Timeline NetworkAnimation;

    private Timeline sequenceMangementAnimation;

    private final int Y_AXIS_LENGTH = 60;
    private double sequence = 0;
    private boolean showChartForSystem = true;

    private Device device = Device.getInstance();

    private Thread monitorServiceThread;
    private MonitorService monitorService = MonitorService.getInstance();
    private CPUMonitor cpuMonitor = CPUMonitor.getInstance();
    private MemoryMonitor memoryMonitor = MemoryMonitor.getInstance();
    private NetworkMonitor networkMonitor = NetworkMonitor.getInstance();

    private IntegerProperty totalMemory = new SimpleIntegerProperty();
    private IntegerProperty freeMemory = new SimpleIntegerProperty();

    private DoubleProperty receivedKBpsSystem = new SimpleDoubleProperty();
    private DoubleProperty sentKBpsSystem = new SimpleDoubleProperty();
    private DoubleProperty receivedKBpsApplication = new SimpleDoubleProperty();
    private DoubleProperty sentKBpsApplication = new SimpleDoubleProperty();

    private DoubleProperty usedMemoryPercentageSystem = new SimpleDoubleProperty();
    private DoubleProperty usedMemoryGBSystem = new SimpleDoubleProperty();
    private DoubleProperty usedMemoryPercentageApplication = new SimpleDoubleProperty();

    private static MonitorTabController monitorTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verticalPane.setDividerPositions(0.3333f, 0.6666f, 0.9999f);

        initializeButtons();
        initializeCPUChart();
        initializeMemoryChart();
        initializeNetworkChart();
        runSequenceManagementAnimation();

        monitorTabController = this;
    }

    public static MonitorTabController getController() {
        return monitorTabController;
    }

    private void runSequenceManagementAnimation() {
        sequenceMangementAnimation = new Timeline();
        sequenceMangementAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
            sequence++;

            if (sequence > Y_AXIS_LENGTH) {
                CPUDataSeriesSystem.getData().remove(0);
                CPUDataSeriesApplication.getData().remove(0);

                NetworkDataSeriesReceivedApplication.getData().remove(0);
                NetworkDataSeriesReceivedSystem.getData().remove(0);
                NetworkDataSeriesSentApplication.getData().remove(0);
                NetworkDataSeriesSentSystem.getData().remove(0);

                MemoryDataSeriesSystem.getData().remove(0);
                MemoryDataSeriesApplication.getData().remove(0);
            }

            if (sequence > Y_AXIS_LENGTH - 1) {
                xAxis.setLowerBound(xAxis.getLowerBound() + 1);
                xAxis.setUpperBound(xAxis.getUpperBound() + 1);
            }

        }));
        sequenceMangementAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void initializeNetworkChart() {
        NetworkAnimation = new Timeline();
        NetworkAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
            NetworkDataSeriesSentSystem.getData().add(new XYChart.Data<>(sequence, sentKBpsSystem.get()));
            NetworkDataSeriesReceivedSystem.getData().add(new XYChart.Data<>(sequence, receivedKBpsSystem.get()));
            NetworkDataSeriesSentApplication.getData().add(new XYChart.Data<>(sequence, sentKBpsApplication.get()));
            NetworkDataSeriesReceivedApplication.getData().add(new XYChart.Data<>(sequence, receivedKBpsApplication.get()));
        }));
        NetworkAnimation.setCycleCount(Animation.INDEFINITE);

        NetworkDataSeriesReceivedSystem = new XYChart.Series<>();
        NetworkChart.getData().add(NetworkDataSeriesReceivedSystem);
        styleSeries(NetworkDataSeriesReceivedSystem, Color.BLUE, false);

        NetworkDataSeriesSentSystem = new XYChart.Series<>();
        NetworkChart.getData().add(NetworkDataSeriesSentSystem);
        Color lightBlue = Color.BLUE.brighter().brighter().brighter().brighter().brighter();
        styleSeries(NetworkDataSeriesSentSystem, lightBlue, true);

        NetworkDataSeriesReceivedApplication = new XYChart.Series<>();
        NetworkChart.getData().add(NetworkDataSeriesReceivedApplication);
        styleSeries(NetworkDataSeriesReceivedApplication, Color.RED, false);

        NetworkDataSeriesSentApplication = new XYChart.Series<>();
        NetworkChart.getData().add(NetworkDataSeriesSentApplication);
        Color lightRed = Color.RED.brighter().brighter().brighter().brighter().brighter();
        styleSeries(NetworkDataSeriesSentApplication, lightRed, true);
    }

    private void initializeMemoryChart() {
        MemoryAnimation = new Timeline();
        MemoryAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {

            double usedMemory = totalMemory.get() - freeMemory.get();
            usedMemoryGBSystem.setValue(usedMemory/1000000);

            double totalMem = totalMemory.get();

            usedMemoryPercentageSystem.setValue((usedMemory / totalMem)*100);
            usedMemoryPercentageApplication.setValue((memoryMonitor.applicationMemoryUsageProperty().getValue() / totalMem)*100);

            MemoryDataSeriesSystem.getData().add(new XYChart.Data<>(sequence, usedMemoryPercentageSystem.get()));
            MemoryDataSeriesApplication.getData().add(new XYChart.Data<>(sequence, usedMemoryPercentageApplication.get()));
        }));
        MemoryAnimation.setCycleCount(Animation.INDEFINITE);

        MemoryDataSeriesSystem = new XYChart.Series<>();
        MemoryChart.getData().add(MemoryDataSeriesSystem);
        styleSeries(MemoryDataSeriesSystem, Color.BLUE, false);

        MemoryDataSeriesApplication = new XYChart.Series<>();
        MemoryChart.getData().add(MemoryDataSeriesApplication);
        styleSeries(MemoryDataSeriesApplication, Color.MAGENTA, false);
    }

    private void initializeCPUChart() {
        CPUAnimation = new Timeline();
        CPUAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
            CPUDataSeriesSystem.getData().add(new XYChart.Data<>(sequence, cpuMonitor.systemCPUPercentageUtilizationProperty().get()));
            CPUDataSeriesApplication.getData().add(new XYChart.Data<>(sequence, cpuMonitor.applicationCPUPercentageUtilizationProperty().get()));
        }));
        CPUAnimation.setCycleCount(Animation.INDEFINITE);

        CPUDataSeriesSystem = new XYChart.Series<>();
        CPUChart.getData().add(CPUDataSeriesSystem);
        styleSeries(CPUDataSeriesSystem, Color.RED, false);

        CPUDataSeriesApplication = new XYChart.Series<>();
        CPUChart.getData().add(CPUDataSeriesApplication);
        styleSeries(CPUDataSeriesApplication, Color.ORANGE.darker().darker().darker(), false);
    }

    public void play() {
        Log.info("Playing...");

        monitorServiceThread = new Thread(monitorService);
        monitorServiceThread.start();

        bindLabels();

        sequenceMangementAnimation.play();
        MemoryAnimation.play();
        CPUAnimation.play();
        NetworkAnimation.play();
    }

    private void bindLabels() {
        sentKBpsSystem.bind(networkMonitor.systemSentKBpsProperty());
        receivedKBpsSystem.bind(networkMonitor.systemReceivedKBpsProperty());
        sentKBpsApplication.bind(networkMonitor.applicationSentKBpsProperty());
        receivedKBpsApplication.bind(networkMonitor.applicationReceivedKBpsProperty());

        freeMemory.bind(memoryMonitor.freeMemoryProperty());
        totalMemory.bind(memoryMonitor.totalMemoryProperty());

        CPUVendorLabel.textProperty().bind(cpuMonitor.CPUVendorProperty());
        CPUMaximumSpeedLabel.textProperty().bind(cpuMonitor.maximumFrequencyProperty().asString("%.1f").concat(" GHz"));
        CPUSystemUtilizationPercentageLabel.textProperty().bind(cpuMonitor.systemCPUPercentageUtilizationProperty().asString().concat("%"));
        CPUApplicationUtilizationPercentageLabel.textProperty().bind(cpuMonitor.applicationCPUPercentageUtilizationProperty().asString().concat("%"));
        CPUProcessesLabel.textProperty().bind(cpuMonitor.runningProcessesProperty().asString());
        CPUUptimeLabel.textProperty().bind(cpuMonitor.upTimeProperty());
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

        NetworkSystemSentKBps.textProperty().bind(sentKBpsSystem.asString("%.1f").concat(" KBps"));
        NetworkSystemReceivedKBps.textProperty().bind(receivedKBpsSystem.asString("%.1f").concat(" KBps"));

        NetworkApplicationSentKBps.textProperty().bind(networkMonitor.applicationSentKBpsProperty().asString("%.1f").concat(" KBps"));
        NetworkApplicationReceivedKBps.textProperty().bind(networkMonitor.applicationReceivedKBpsProperty().asString("%.1f").concat(" KBps"));
    }

    private void styleSeries(XYChart.Series<Number, Number> series, Color color, boolean dashed) {
        String dash = "";
        if(dashed)
            dash = " -fx-stroke-dash-array: 2 4;";

        Node fill = series.getNode().lookup(".chart-series-area-fill");
        Node line = series.getNode().lookup(".chart-series-area-line");

        String rgb = String.format("%d, %d, %d", (color.getRed() * 255), (color.getGreen() * 255), (color.getBlue() * 255));

        fill.setStyle("-fx-fill: rgba(" + rgb + ", 0.15);");
        line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);" + dash);
    }

    @FXML
    private void handleAppsListViewClicked(MouseEvent mouseEvent) {
        Log.info("is cancelled? " + monitorService.cancel());

        if(monitorServiceThread != null)
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
        updateDeviceListView();
    }

    @Override
    public void initializeButtons() {
        setImage("/resources/refresh.png", null, refreshButton);
        setImage("/resources/pop_out.png", null, showLogCatButton);
    }
}
