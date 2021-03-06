package application.monitor;

import application.applications.ApplicationTabController;
import application.automation.AutomationTabController;
import application.device.AndroidApplication;
import application.device.Device;
import application.logcat.LogCatTabController;
import application.monitor.model.CPUMonitor;
import application.monitor.model.MemoryMonitor;
import application.monitor.model.NetworkMonitor;
import application.utilities.ApplicationUtils;
import application.utilities.Showable;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MonitorTabController extends ApplicationTabController implements Showable<Initializable>, Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(MonitorTabController.class.getName());

    @FXML
    private NumberAxis CPUXAxis;
    @FXML
    private NumberAxis NetworkXAxis;
    @FXML
    private NumberAxis MemoryXAxis;
    private ArrayList<NumberAxis> axises;

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

    private AreaChart.Series<Number, Number> CPUDataSeriesSystem;
    private AreaChart.Series<Number, Number> CPUDataSeriesApplication;
    private Timeline CPUAnimation;

    private XYChart.Series<Number, Number> MemoryDataSeriesSystem;
    private XYChart.Series<Number, Number> MemoryDataSeriesApplication;
    private Timeline MemoryAnimation;

    private XYChart.Series<Number, Number> NetworkDataSeriesSentSystem;
    private XYChart.Series<Number, Number> NetworkDataSeriesReceivedSystem;
    private XYChart.Series<Number, Number> NetworkDataSeriesSentApplication;
    private XYChart.Series<Number, Number> NetworkDataSeriesReceivedApplication;
    private Timeline NetworkAnimation;

    private Timeline sequenceManagementAnimation;

    private final int Y_AXIS_LENGTH = 60;
    private double sequence = 0;

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

    private AutomationTabController automationTabController;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verticalPane.setDividerPositions(0.3333f, 0.6666f, 0.9999f);

        axises = new ArrayList<>();
        axises.add(CPUXAxis);
        axises.add(NetworkXAxis);
        axises.add(MemoryXAxis);

        for(NumberAxis axis : axises) {
            axis.setLowerBound(0);
            axis.setUpperBound(Y_AXIS_LENGTH);
            axis.setTickUnit(Y_AXIS_LENGTH/10);
        }

        initializeButtons();
        initializeCPUChart();
        initializeMemoryChart();
        initializeNetworkChart();
        runSequenceManagementAnimation();

        monitorTabController = this;
    }

    @Override
    public Initializable newWindow(Initializable controller, Object object) throws IOException {
        automationTabController = (AutomationTabController) controller;

        FXMLLoader fxmlLoader = new FXMLLoader(LogCatTabController.class.getClass().getResource("/application/monitor/MonitorTab.fxml"));

        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/application/main/global.css");

        MonitorTabController monitorTabController = fxmlLoader.getController();

        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setTitle("Monitor");
        stage.setScene(scene);
        stage.setMaximized(true);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setMaxWidth(primaryScreenBounds.getWidth());
        stage.setMaxHeight(primaryScreenBounds.getHeight());
        stage.setOnCloseRequest(event -> automationTabController.setMonitorTabController(null));
        stage.show();

        monitorTabController.updateDeviceListView();

        return monitorTabController;
    }

    public static MonitorTabController getController() {
        return monitorTabController;
    }

    /**
     * Runs the sequence management animation
     * This animation increments the sequence variable
     * Removes data from the start of each series every second after a minute has passed
     * to allow the chart to move from right to left
     */
    private void runSequenceManagementAnimation() {
        sequenceManagementAnimation = new Timeline();
        sequenceManagementAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
            sequence++;

            for(NumberAxis axis : axises) {
                axis.setLowerBound(sequence-Y_AXIS_LENGTH);
                axis.setUpperBound(sequence-1);
            }
        }));
        sequenceManagementAnimation.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Sets up the Network Chart
     * Data from the {@link CPUMonitor} class is added to the 2 series' in this chart every minute
     */
    private void initializeCPUChart() {
        CPUAnimation = new Timeline();
        CPUAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(250), event -> {
            CPUDataSeriesSystem.getData().add(new AreaChart.Data(sequence, cpuMonitor.systemCPUPercentageUtilizationProperty().get()));
            CPUDataSeriesApplication.getData().add(new AreaChart.Data(sequence, cpuMonitor.applicationCPUPercentageUtilizationProperty().get()));
        }));
        CPUAnimation.setCycleCount(Animation.INDEFINITE);

        CPUDataSeriesSystem = new AreaChart.Series<>();
        CPUChart.getData().add(CPUDataSeriesSystem);
        styleSeries(CPUDataSeriesSystem, Color.RED, false);

        CPUDataSeriesApplication = new AreaChart.Series<>();
        CPUChart.getData().add(CPUDataSeriesApplication);
        styleSeries(CPUDataSeriesApplication, Color.GREEN, false);
    }

    /**
     * Sets up the Network Chart
     * Data from the {@link NetworkMonitor} class is added to the 4 series' in this chart every minute
     */
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

    /**
     * Sets up the Memory Chart
     * Data from the {@link MemoryMonitor} class is added to the 2 series' in this chart every minute
     */
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

    private long monitorStartTime;

    private long getMonitorExecutionTime() {
        return System.currentTimeMillis() - monitorStartTime;
    }

    /**
     * Starts the {@link MonitorService} to gather data from the device
     * Binds the labels to the data in each monitor
     * starts all chart animations
     */
    public void play() {
        Log.info("Playing...");

        if(monitorServiceThread != null)
            monitorServiceThread.interrupt();

        monitorStartTime = System.currentTimeMillis();
        monitorServiceThread = new Thread(monitorService);
        monitorServiceThread.start();

        bindLabels();

        sequenceManagementAnimation.play();
        MemoryAnimation.play();
        CPUAnimation.play();
        NetworkAnimation.play();
    }

    public void pause() {
        Log.info("pausing monitor service");

        if(monitorServiceThread != null) {
            monitorService.pause();
            sequenceManagementAnimation.pause();
            MemoryAnimation.pause();
            CPUAnimation.pause();
            NetworkAnimation.pause();
        }
    }

    public void resume() {
        Log.info("resuming monitor service");

        if(monitorServiceThread != null) {
            monitorService.resume();
            sequenceManagementAnimation.play();
            MemoryAnimation.play();
            CPUAnimation.play();
            NetworkAnimation.play();
        }
    }

    /**
     * Binds the labels to the corresponding data in the monitors
     */
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

    /**
     * Styles the line and body of the series according to the Color parameter passed
     * @param series series to apply style to
     * @param color color to style series with
     * @param dashed boolean to select if line will be dashed or not
     */
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

    /**
     * Handles the application list view being clicked
     * Interrupts the Monitor Service if it is running.
     * Sets the selected application in the device to the value in the list
     * Starts a new instance of the Monitor Service
     * @param mouseEvent
     */
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

    /**
     * Updates the application ListView
     * @param event
     */
    @Override
    @FXML
    protected void handleRefreshButtonClicked(ActionEvent event) {
        updateDeviceListView();
    }

    /**
     * Initialize the buttons
     * Can do any of the following:
     * Set tooltip text
     * Set image
     * Set disabled / enabled
     * Set visible / invisible
     */
    @Override
    public void initializeButtons() {
        setImage("/resources/refresh.png", null, refreshButton);
        setImage("/resources/pop_out.png", null, showLogCatButton);
    }
}
