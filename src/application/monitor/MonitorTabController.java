package application.monitor;

import application.ADBUtil;
import application.device.AndroidApplication;
import application.device.Device;
import application.logcat.LogCatTabController;
import application.utilities.ADB;
import application.utilities.ApplicationUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorTabController extends LogCatTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(MonitorTabController.class.getName());
    public Button refreshButton;

    @FXML
    private ListView<String> appsOnDeviceListView;

    @FXML
    private AreaChart<Number, Number> chart;

    private XYChart.Series<Number, Number> dataSeries;
    private Timeline animation;

    private int sequence = 0;

    private Device device = Device.getInstance();

    private Random rand = new Random();

    private AtomicInteger CPUPercentage = new AtomicInteger(0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> {
            dataSeries.getData().add(new XYChart.Data<>(++sequence, rand.nextInt(100)));
            if(dataSeries.getData().size() > 10) {
                dataSeries.getData().remove(0);
            }
        }));
        animation.setCycleCount(Animation.INDEFINITE);

        dataSeries = new XYChart.Series<>();
        dataSeries.setName("");

chart.setLegendVisible(false);

        chart.getData().add(dataSeries);
       // data.add(dataSeries);


        chart.setCreateSymbols(false);
        //chart.setData(data);
    }

    @FXML
    @Override
    protected void handleStartButtonClicked(MouseEvent event) {
        if(startButton.getText().equals("Start"))
            play();
        else
            stop();
    }

    private void updateCPUPercentage() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + device.getName() + " shell \"top -d 1 | grep " + device.getSelectedApplication().getName() + "\"");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(stopFlag)
                        break;

                    if(line.isEmpty())
                        continue;

                    System.out.println("line before: " + line);
                    line = line.trim().replace("   ", " ").replace("  ", " ");
                    System.out.println("line after: " + line);

                    String CPUString = line.split(" ")[2];
                    System.out.println("CPU String before: " + CPUString);

                    CPUString = CPUString.replace("%", "").trim();
                    System.out.println("CPU String after: " + CPUString);

                    CPUPercentage.set(Integer.parseInt(CPUString));
                    System.out.println("CPU Percentage: " + CPUPercentage);
                    System.out.println();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    public void play() {
        Log.info("Playing...");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                updateCPUPercentage();
                return null;
            }
        };
        new Thread(task).start();


        animation.play();

        startButton.setText("Stop");
    }

    public void stop() {
        Log.info("Stopping...");
        animation.pause();
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
    @FXML
    protected void handleSearchFieldAction(KeyEvent event) {
        appsOnDeviceListView.setItems(filter(searchField.getText(), device.getApplicationNames()));
    }

    private void updateDeviceListView() {
        try {
            appsOnDeviceListView.getItems().clear();
            device.getApplicationNames().clear();

            Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
                @Override
                protected ObservableList<String> call() {
                    return FXCollections.observableArrayList(ADB.listApplications());
                }
            };
            task.setOnSucceeded(event1 -> {
                Collections.sort(task.getValue());
                appsOnDeviceListView.setItems(filter(searchField.getText(), task.getValue()));
                device.setApplicationNames(task.getValue());
            });
            task.setOnFailed(event -> Log.error(task.getException().getMessage(), task.getException()));
            new Thread(task).start();
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }
    }

    @Override
    public void initializeButtons() {
        setImage("/resources/refresh.png", null, refreshButton);
    }
}
