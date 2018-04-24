package application.sensors;

import application.TelnetServer;
import application.utilities.ApplicationUtils;
import application.utilities.XMLUtil;
import application.sensors.model.AccelerometerModel;
import application.sensors.model.GyroscopeModel;
import application.sensors.model.MagneticFieldModel;
import application.sensors.server.HTTPServer;
import application.utilities.ThreeDimensionalVector;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SensorsTabController implements Initializable, ApplicationUtils {
    //region fields
    private final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\sensors";

    public static final String LIGHT = "light";
    public static final String HUMIDITY = "humidity";
    public static final String PRESSURE = "pressure";
    public static final String PROXIMITY = "proximity";
    public static final String TEMPERATURE = "temperature";
    public static final String ACCELERATION = "acceleration";
    public static final String GYROSCOPE = "gyroscope";
    public static final String ORIENTATION = "orientation";
    public static final String MAGNETIC_FIELD = "magnetic-field";
    public static final String YAW = "yaw";
    public static final String PITCH = "pitch";
    public static final String ROLL = "roll";
    public static final String LOCATION = "location";
    public static final String BATTERY = "battery";

    private final int RECORDING_PERIOD = 20; //record period in ms
    private AtomicInteger playbackSpeed = new AtomicInteger(RECORDING_PERIOD);

    @FXML
    public Slider lightSlider;
    @FXML
    public Slider temperatureSlider;
    @FXML
    public Slider pressureSlider;
    @FXML
    public Slider proximitySlider;
    @FXML
    public Slider humiditySlider;
    @FXML
    public Slider yawSlider;
    @FXML
    public Slider pitchSlider;
    @FXML
    public Slider rollSlider;
    @FXML
    private Slider playbackSlider;

    @FXML
    private Label lightLabel;
    @FXML
    private Label temperatureLabel;
    @FXML
    private Label pressureLabel;
    @FXML
    private Label proximityLabel;
    @FXML
    private Label humidityLabel;
    @FXML
    private Label yawLabel;
    @FXML
    private Label pitchLabel;
    @FXML
    private Label rollLabel;
    @FXML
    private Label magneticFieldLabel;
    @FXML
    private Label accelerometerLabel;
    @FXML
    private Label gyroscopeLabel;
    @FXML
    private Label orientationLabel;
    @FXML
    private Label playbackTitleLabel;
    @FXML
    private Label playbackLabel;
    @FXML
    private Label loggerLabel;
    @FXML
    public Label batteryLabel;
    @FXML
    public Label locationLabel;

    @FXML
    private Button recordButton;
    @FXML
    private Button stopRecordingButton;
   // @FXML
   // private Button saveButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button playButton;

    @FXML
    private CheckBox loopBox;
    @FXML
    private CheckBox listenBox;

    @FXML
    private AnchorPane phonePane;

    @FXML
    private Box phone;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private RadioButton rotateRadioButton;
    @FXML
    private RadioButton moveRadioButton;

    @FXML
    private ComboBox<String> axisComboBox;

    private boolean isPhoneDragged = false;
    private boolean isRotate = true;
    private boolean isConnected = false;

    private int yawValue;
    private int pitchValue;
    private int rollValue;

    private int pitchBeforeValue;
    private int rollBeforeValue;

    private double accelerometerX;
    private double accelerometerY;
    private double accelerometerZ;

    private double magneticFieldX;
    private double magneticFieldY;
    private double magneticFieldZ;

    private double gyroscopeYaw;
    private double gyroscopePitch;
    private double gyroscopeRoll;

    private final double GRAVITY_CONSTANT = 9.80665;
    private final double MAGNETIC_NORTH = 22874.1;
    private final double MAGNETIC_EAST = 5939.5;
    private final double MAGNETIC_VERTICAL = 43180.5;

    private final DecimalFormat TWO_DECIMAL_FORMAT = new DecimalFormat("#0.00");

    private AccelerometerModel accelerometerModel;
    private GyroscopeModel gyroscopeModel;
    private MagneticFieldModel magneticFieldModel;

    private double mousePosX = 0;
    private double mousePosY = 0;
    private double mouseMoveX = 0;
    private double mouseMoveZ = 0;

    public Rotate rotateX = new Rotate(90, Rotate.X_AXIS);
    public Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    public Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private Map<String, Double> sensorValues = Collections.synchronizedMap(new HashMap<>());
    private playbackThread playbackThread;
    private Timer recordingTimer;

    private XMLUtil xmlUtil;
    private HTTPServer server;

    private volatile boolean isRecording = false;
    private boolean startNewTimerTask = true;
    private boolean isLoaded = false;
    private boolean wasPaused = false;
    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializePhone();
        initializeButtons();
        initHashMap();

        xmlUtil = new XMLUtil(false);

        listenBox.setVisible(false);
        axisComboBox.getSelectionModel().select(0);

        //region initialize
        yawValue = 180;
        yawSlider.setValue(yawValue);
        pitchValue = -180;
        pitchSlider.setValue(pitchValue);
        rollValue = 0;
        rollSlider.setValue(rollValue);

        playbackSlider.setValue(50);

        accelerometerModel = new AccelerometerModel();
        gyroscopeModel = new GyroscopeModel();
        magneticFieldModel = new MagneticFieldModel();

        accelerometerModel.setUpdateDuration(200);
        gyroscopeModel.setUpdateDuration(200);
        magneticFieldModel.setUpdateDuration(200);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                updateSensorValues();
                return null;
            }
        };
        new Thread(task).start();

        handleSliderEvents();
        handleMouseEvents();

        updateSliderValues();
        //endregion
    }

    @Override
    public void initializeButtons() {
        rotateRadioButton.setSelected(true);
        stopRecordingButton.setDisable(true);

        setImage("/resources/record_cropped.png", "Record values", recordButton);
        setImage("/resources/stop.png", "Stop recording", stopRecordingButton);
        setImage("/resources/play_cropped.png", null, playButton);

        if(!isLoaded) {
            playButton.setVisible(false);
            loopBox.setVisible(false);
            playbackLabel.setVisible(false);
            playbackSlider.setVisible(false);
            playbackTitleLabel.setVisible(false);
        }
    }

    //region buttonHandlers
    @FXML
    private void handleStopRecordingButtonPressed(ActionEvent event) {
        isRecording = false;

        recordButton.setDisable(false);
        stopRecordingButton.setDisable(true);

        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DIRECTORY));

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if(file != null) {
            xmlUtil.saveFile(file);
            loggerLabel.setTextFill(Color.GREEN);
            loggerLabel.setText("File \"" + file.getName().replace(".xml", "") + "\" saved");
        } else {
            loggerLabel.setTextFill(Color.RED);
            loggerLabel.setText("File not saved");
        }

        xmlUtil = new XMLUtil(false);

        recordingTimer.cancel();
        startNewTimerTask = true;
    }

    @FXML
    private void handleLoadButtonClicked(ActionEvent event) {
        isRecording = false;

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DIRECTORY));

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        HashMap<Integer, HashMap<String, Double>> loadedValues;
        if(file != null) {
            loggerLabel.setTextFill(Color.GREEN);
            loggerLabel.setText("File \"" + file.getName().replace(".xml", "") + "\" loaded");
            System.out.println(file.getAbsolutePath());

            setImage("/resources/play_cropped.png", null, playButton);

            xmlUtil = new XMLUtil(false);
            loadedValues = xmlUtil.loadXML(file);

            if(playbackThread != null) {
                playbackThread.stopThread();

                wasPaused = false;
                playButton.setDisable(false);
            }

            playbackThread = new playbackThread(loadedValues);

            isLoaded = true;

            playButton.setVisible(true);
            loopBox.setVisible(true);
            playbackLabel.setVisible(true);
            playbackSlider.setVisible(true);
            playbackTitleLabel.setVisible(true);
        } else {
            loggerLabel.setTextFill(Color.RED);
            loggerLabel.setText("File not loaded");
        }
    }

    @FXML
    private void handleRecordButtonClicked(ActionEvent event) {
        isRecording = !isRecording;

        if(startNewTimerTask) {
            recordingTimer = new Timer();
            recordingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isRecording) {
                        System.out.println("adding element");
                        xmlUtil.addElement(sensorValues);
                    }
                }
            }, 0, RECORDING_PERIOD);

            startNewTimerTask = false;
        }

        if(isRecording) {
            setImage("/resources/pause.png", "Pause recording", recordButton);
        } else {
            setImage("/resources/record_cropped.png", "Record values", recordButton);
        }

        stopRecordingButton.setDisable(isRecording);
        playButton.setDisable(isRecording);
        loopBox.setDisable(isRecording);
        loadButton.setDisable(isRecording);
    }

    @FXML
    private void handlePlayButtonClicked(ActionEvent event) {
        if (playbackThread.isPaused()) {
            playbackThread.play();
            System.out.println("RESUMING");
            setImage("/resources/pause.png", null, playButton);
            recordButton.setDisable(true);
            stopRecordingButton.setDisable(true);
        } else {
            if(!wasPaused) {
                playbackThread.run();
                System.out.println("STARTING");
                setImage("/resources/pause.png", null, playButton);
                wasPaused = true;
                recordButton.setDisable(true);
                stopRecordingButton.setDisable(true);
            } else {
                playbackThread.pause();
                System.out.println("PAUSING");
                setImage("/resources/play_cropped.png", null, playButton);
                recordButton.setDisable(false);
                stopRecordingButton.setDisable(false);
            }
        }
    }

    @FXML
    private void handleConnectButtonClicked(ActionEvent event) {
        try {
            server = new HTTPServer(this);
            connectButton.setDisable(true);
            loggerLabel.setTextFill(Color.GREEN);
            loggerLabel.setText("Connect your app to " + server.getIPAddress() + " Port " + server.getPORT());

            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return server.listen();
//                    while (true) {
//                        if (isConnected) {
//                            Platform.runLater(() -> {
//                                loggerLabel.setText("Connected");
//                                listenBox.setVisible(true);
//                                listenBox.setSelected(true);
//                            });
//                            break;
//                        }
//                    }
//                    return null;
                }
            };

            task.setOnSucceeded(event1 -> {
                isConnected = true;
                listenBox.setVisible(true);
                listenBox.setSelected(true);

                loggerLabel.setTextFill(Color.GREEN);
                loggerLabel.setText("Connected");

                System.out.println("Connected bai");

            });

            task.setOnFailed(event1 -> {
                loggerLabel.setTextFill(Color.RED);
                loggerLabel.setText("Could not connect");
                listenBox.setVisible(false);
                listenBox.setSelected(false);
            });

            new Thread(task).start();



        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    //endregion

    private void handleSliderEvents() {
        lightSlider.valueProperty().addListener((observable, oldvalue, newvalue) -> sendSensorValues(LIGHT, newvalue.doubleValue()));
        temperatureSlider.valueProperty().addListener((observable, oldvalue, newvalue) -> sendSensorValues(TEMPERATURE, newvalue.doubleValue()));
        pressureSlider.valueProperty().addListener((observable, oldvalue, newvalue) -> sendSensorValues(PRESSURE, newvalue.doubleValue()));
        proximitySlider.valueProperty().addListener((observable, oldvalue, newvalue) -> sendSensorValues(PROXIMITY, newvalue.doubleValue()));
        humiditySlider.valueProperty().addListener((observable, oldvalue, newvalue) -> sendSensorValues(HUMIDITY, newvalue.doubleValue()));
        yawSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {

            System.out.println("yaw moved");

            yawValue = newvalue.intValue();
            sensorValues.put(YAW, (double) yawValue);

            updateSliderValues();
        });

        pitchSlider.setOnMouseDragged((mouseEvent) -> rotateX.setAngle(180-pitchBeforeValue-270));

        pitchSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            System.out.println("pitch moved");
            pitchBeforeValue = pitchValue = newvalue.intValue();

            if(playbackThread != null) {
                if (playbackThread.isRunning()) {
                    rotateX.setAngle(180 - pitchBeforeValue - 270);
                }
            }

            if(isConnected && listenBox.isSelected()) {
                rotateX.setAngle(180 - -1 * (pitchBeforeValue) - 270);
                sensorValues.put(PITCH, (double) (-1*(pitchBeforeValue)));
            }
            else
                sensorValues.put(PITCH, (double) pitchBeforeValue);

            updateSliderValues();
        });

        rollSlider.setOnMouseDragged((mouseEvent) -> rotateZ.setAngle(90-rollBeforeValue-90));

        rollSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            System.out.println("roll moved");

            if(!isPhoneDragged)
                rollBeforeValue = rollValue = newvalue.intValue() * -1;
            else
                rollBeforeValue = rollValue = newvalue.intValue();

            sensorValues.put(ROLL, (double) rollBeforeValue);

            if(playbackThread != null) {
                if (playbackThread.isRunning()) {
                    rotateZ.setAngle(90 - rollBeforeValue - 90);
                }
            }

            if(isConnected && listenBox.isSelected()) {
                rotateZ.setAngle(90 - rollBeforeValue - 90);
                sensorValues.put(ROLL, (double) (-1*(rollBeforeValue)));
            } else
                sensorValues.put(ROLL, (double) rollBeforeValue);

            updateSliderValues();
        });

        playbackSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->  {
            double value = newvalue.intValue();

            if(value >= 50) {
                value -= 50;
                value /= 5;
                if(value < 1) value = 1;
                int result = RECORDING_PERIOD / (int)value;

                playbackSpeed.set(result);
                playbackLabel.setText("x " + (int) value + "");
            } else {
                value += 50;
                value /= 50;
                value -= 1;
                if(value < 0.1) value = 0.1;
                if(value == 0) value = 1;
                if(value > 0.9) value = 0.9;
                double newValue = Double.parseDouble(String.format("%.1f", value));
                newValue = 1 - newValue;
                int result = (int) (newValue*(RECORDING_PERIOD*10));

                playbackSpeed.set(result);
                playbackLabel.setText("x " + String.format("%.1f", value));
            }
        });
    }

    private void initializePhone() {
        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setSpecularColor(Color.ORANGE);
        redMaterial.setDiffuseColor(Color.RED);

        phone.setMaterial(redMaterial);
        phone.getTransforms().addAll(rotateZ, rotateY, rotateX);
        phone.setManaged(false);

        HBox phonePaneHBox = new HBox();

        phone.setLayoutX(240);
        phone.setLayoutY(100);
        phonePaneHBox.getChildren().add(phone);

        phonePane.getChildren().addAll(phonePaneHBox);
        phonePane.setStyle("-fx-background-color: gray;");
    }

    private void handleMouseEvents() {
        listenBox.setOnMouseClicked(event -> server.setIsListening(listenBox.isSelected()));

        moveRadioButton.setOnMouseClicked(event -> {
            if(moveRadioButton.isSelected()) {
                isRotate = false;
                rotateRadioButton.setSelected(false);
            } else {
                moveRadioButton.setSelected(true);
            }
        });

        rotateRadioButton.setOnMouseClicked(event -> {
            if(rotateRadioButton.isSelected()) {
                isRotate = true;
                moveRadioButton.setSelected(false);
            } else {
                rotateRadioButton.setSelected(true);
            }
        });

        phonePane.setOnMousePressed((mouseEvent) -> {
            mousePosX = mouseEvent.getSceneX();
            mousePosY = mouseEvent.getSceneY();

            mouseMoveX = 10;
            mouseMoveZ = accelerometerModel.getMoveZ();

            isPhoneDragged = true;
        });

        phonePane.setOnMouseReleased((mouseEvent) -> isPhoneDragged = false);

        phonePane.setOnMouseDragged((mouseEvent) -> {
            if(isRotate) {
                double dx = (mousePosX - mouseEvent.getSceneX());
                double dy = (mousePosY - mouseEvent.getSceneY());

                if (mouseEvent.isPrimaryButtonDown()) {
                    /* Pitch */
                    double rotateXAngle = (rotateX.getAngle() - (dy * 10 / phone.getHeight() * 360) * (Math.PI / 180)) % 360;
                    if (rotateXAngle < 0)
                        rotateXAngle += 360;
                    rotateX.setAngle(rotateXAngle);

                    pitchValue = adjustValue(rotateXAngle);
                    pitchSlider.setValue(pitchValue);

                    if(axisComboBox.getSelectionModel().isSelected(0) || axisComboBox.getSelectionModel().isSelected(2) ) {
                        /* Yaw */
                        double rotateYAngle = (rotateY.getAngle() - (dx * 10 / phone.getWidth() * -360) * (Math.PI / 180)) % 360;
                        if (rotateYAngle < 0) {
                            rotateYAngle += 360;
                        }
                        rotateY.setAngle(rotateYAngle);
                        yawValue = (int) rotateYAngle;

                        if (yawValue > 180)
                            yawValue -= 360;
                        yawSlider.setValue(yawValue);
                    }

                    if(axisComboBox.getSelectionModel().isSelected(1) || axisComboBox.getSelectionModel().isSelected(2)) {
                        /* Roll */
                        double rotateZAngle = (rotateZ.getAngle() + (dy * 10 / phone.getWidth() * -360) * (Math.PI / 180)) % 360;
                        if (rotateZAngle < 0) {
                            rotateZAngle += 360;
                        }
                        rotateZ.setAngle(rotateZAngle);
                        rollValue = adjustValue(rotateZAngle);
                    }

                    updateSliderValues();
                    rollSlider.setValue(rollValue);
                }
                mousePosX = mouseEvent.getSceneX();
                mousePosY = mouseEvent.getSceneY();
            } else {
                if(mouseEvent.getX() > 0 && mouseEvent.getX() < 250)
                    phone.setLayoutX(mouseEvent.getX());
                if(mouseEvent.getY() > 0 && mouseEvent.getY() < 250)
                    phone.setLayoutY(mouseEvent.getY());

                //int newMoveX = (int) ((mouseMoveX - (mouseEvent.getX() - mousePosX))*1.2);
                int newMoveX = (int) ((mouseMoveX - (mouseEvent.getX() - mousePosX)));
                int newMoveZ = (int) ((mouseMoveZ - (mouseEvent.getY() - mousePosY)) / 2) - 40;

                System.out.println("\n****X*****");
                System.out.println("mouseMoveX: " + mouseMoveX);
                System.out.println("mouseEvent.getx: " + mouseEvent.getX());
                System.out.println("mousePosX: " + mousePosX);
                System.out.println("newMoveX: " + newMoveX);

                System.out.println("\n****Y*****");
                System.out.println("mouseMoveY: " + mouseMoveZ);
                System.out.println("mouseEvent.gety: " + mouseEvent.getY());
                System.out.println("mousePosY: " + mousePosY);
                System.out.println("newMoveY: " + newMoveZ);

                accelerometerModel.setMoveX(newMoveX);
                accelerometerModel.setMoveZ(newMoveZ);

                updateAccelerometerData();
            }
        });
    }

    private void sendSensorValues(String sensor, double value) {
        sensorValues.put(sensor, value);
        TelnetServer.setSensor(sensor + " " + String.format("%.2f",value));

        switch (sensor) {
            case LIGHT:
                lightLabel.setText((int) value+"");
                break;
            case TEMPERATURE:
                temperatureLabel.setText((int) value+"");
                break;
            case PRESSURE:
                pressureLabel.setText((int) value+"");
                break;
            case PROXIMITY:
                proximityLabel.setText((int) value+"");
                break;
            case HUMIDITY:
                humidityLabel.setText((int) value+"");
                break;
        }
    }

    private void sendSensorValues(String sensor, double X, double Y, double Z) {
        TelnetServer.setSensor(sensor + " " + TWO_DECIMAL_FORMAT.format(X)
                + ":"
                + TWO_DECIMAL_FORMAT.format(Y)
                + ":"
                + TWO_DECIMAL_FORMAT.format(Z));

        switch (sensor) {
            case ACCELERATION:
                Platform.runLater(() -> accelerometerLabel.setText(TWO_DECIMAL_FORMAT.format(X)
                                + ", "
                                + TWO_DECIMAL_FORMAT.format(Y)
                                + ", "
                                + TWO_DECIMAL_FORMAT.format(Z)));
                break;
            case MAGNETIC_FIELD:
                Platform.runLater(() -> magneticFieldLabel.setText(TWO_DECIMAL_FORMAT.format(X)
                        + ", "
                        + TWO_DECIMAL_FORMAT.format(Y)
                        + ", "
                        + TWO_DECIMAL_FORMAT.format(Z)));
                break;
            case GYROSCOPE:
                Platform.runLater(() -> gyroscopeLabel.setText(TWO_DECIMAL_FORMAT.format(X)
                        + ", "
                        + TWO_DECIMAL_FORMAT.format(Y)
                        + ", "
                        + TWO_DECIMAL_FORMAT.format(Z)));
                break;
        }
    }

    private int adjustValue(double angle) {
        int result = 0;

        if (angle > 270 || (angle < 90 && angle > 0)) {
            if (angle > 270) result = (int) (360 - angle - 90);
            else result = (int) (180 - angle - 270);
        } else if (angle <= 270 || angle >= 90) {
            if (angle <= 270 && angle <= 180) result = (int) (270 - angle);
            else result = (int) (360 - angle - 90);
        }

        return result;
    }

    protected void updateSliderValues() {
        // Restrict pitch value to -90 and +90
        if (pitchValue < -90) {
            pitchValue = -180 - pitchValue;
        } else if (pitchValue > 90) {
            pitchValue = 180 - pitchValue;
        }

        // yaw from 0 to 360
        if (yawValue < 0) {
            yawValue = yawValue + 360;
        }
        if (yawValue >= 360) {
            yawValue = yawValue - 360;
        }

        if (rollValue < -90) {
            rollValue = -180 - rollValue;
        } else if (rollValue > 90) {
            rollValue = 180 - rollValue;
        }

        updateMagneticFieldData();
        updateAccelerometerData();

        orientationLabel.setText(pitchValue + ", " + rollValue + ", " + yawValue);
        TelnetServer.setSensor(ORIENTATION + " " + pitchValue + ":" + rollValue + ":" + yawValue);

        if(!isPhoneDragged) {
            rotateY.setAngle(yawValue);
        }

        yawLabel.setText(yawValue + "");
        pitchLabel.setText(pitchValue + "");
        rollLabel.setText(rollValue + "");
    }

    private void updateSensorValues() {
        while(true) {
            gyroscopeModel.refreshAngularSpeed(10, pitchValue, yawValue, rollValue);

            accelerometerModel.updateSensorReadoutValues();
            gyroscopeModel.updateSensorReadoutValues();
            magneticFieldModel.updateSensorReadoutValues();

            if(accelerometerX != accelerometerModel.getReadAccelerometerX() || accelerometerY != accelerometerModel.getReadAccelerometerY() || accelerometerZ != accelerometerModel.getReadAccelerometerZ()) {
                accelerometerX = accelerometerModel.getReadAccelerometerX();
                accelerometerY = accelerometerModel.getReadAccelerometerY();
                accelerometerZ = accelerometerModel.getReadAccelerometerZ();

                sendSensorValues(ACCELERATION, accelerometerX, accelerometerY, accelerometerZ);
            }

            if(magneticFieldX != magneticFieldModel.getReadCompassX() || magneticFieldY != magneticFieldModel.getReadCompassY() || magneticFieldZ != magneticFieldModel.getReadCompassZ()) {
                magneticFieldX = magneticFieldModel.getReadCompassX();
                magneticFieldY = magneticFieldModel.getReadCompassY();
                magneticFieldZ = magneticFieldModel.getReadCompassZ();

                sendSensorValues(MAGNETIC_FIELD, magneticFieldX, magneticFieldY, magneticFieldZ);
            }

            if(gyroscopePitch != gyroscopeModel.getReadGyroscopePitch() || gyroscopeYaw != gyroscopeModel.getReadGyroscopeYaw() || gyroscopeRoll != gyroscopeModel.getReadGyroscopeRoll()) {
                gyroscopePitch = gyroscopeModel.getReadGyroscopePitch();
                gyroscopeYaw = gyroscopeModel.getReadGyroscopeYaw();
                gyroscopeRoll = gyroscopeModel.getReadGyroscopeRoll();

                sendSensorValues(GYROSCOPE, gyroscopePitch, gyroscopeYaw, gyroscopeRoll);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void setConnected(boolean flag) {
        isConnected = true;
    }

    private void updateMagneticFieldData() {
        ThreeDimensionalVector magneticFieldVector = new ThreeDimensionalVector(MAGNETIC_EAST, MAGNETIC_NORTH, -MAGNETIC_VERTICAL);
        magneticFieldVector.scale(0.001); // convert from nT (nano-Tesla) to uT
        // (micro-Tesla)

        magneticFieldVector.reverserollpitchyaw(rollValue, pitchValue, yawValue);
        magneticFieldModel.setCompass(magneticFieldVector);
    }

    private void updateAccelerometerData() {
        // get component vectors (gravity + linear_acceleration)
        ThreeDimensionalVector gravityVec = getGravityVector();
        ThreeDimensionalVector linearVec = getLinearAccVector(accelerometerModel);

        ThreeDimensionalVector resultVec = ThreeDimensionalVector.addVectors(gravityVec, linearVec);

        accelerometerModel.setXYZ(resultVec);

        double limit = GRAVITY_CONSTANT * 10;

        accelerometerModel.limitate(limit);
    }

    private ThreeDimensionalVector getLinearAccVector(AccelerometerModel accModel) {
        double meterPerPixel = 1. / 3000;
        double dt = 0.001 * 1; // from ms to s
        double k = 500;
        double gamma = 50;

        accModel.refreshAcceleration(k, gamma, dt);

        // Now calculate this into mobile phone acceleration:
        // ! Mobile phone's acceleration is just opposite to
        // lab frame acceleration !
        ThreeDimensionalVector vec = new ThreeDimensionalVector(-accModel.getAx() * meterPerPixel, 0,
                -accModel.getAz() * meterPerPixel);
        vec.reverserollpitchyaw(rollValue, pitchValue, yawValue);

        return vec;
    }

    private ThreeDimensionalVector getGravityVector() {
        // apply orientation
        // we reverse roll, pitch, and yawDegree,
        // as this is how the mobile phone sees the coordinate system.
        ThreeDimensionalVector gravityVec = new ThreeDimensionalVector(0, 0, GRAVITY_CONSTANT);
        gravityVec.reverserollpitchyaw(rollValue, pitchValue, yawValue);

        return gravityVec;
    }

    private void restartThread() {
        playbackThread.run();
    }

    private void initHashMap() {
        sensorValues.put(LIGHT, 0.0);
        sensorValues.put(HUMIDITY, 0.0);
        sensorValues.put(PRESSURE, 0.0);
        sensorValues.put(TEMPERATURE, 0.0);
        sensorValues.put(PROXIMITY, 0.0);
        sensorValues.put(YAW, 0.0);
        sensorValues.put(PITCH, 0.0);
        sensorValues.put(ROLL, 0.0);
    }

    private class playbackThread extends Thread {
        private final AtomicBoolean pauseFlag = new AtomicBoolean(false);
        private final AtomicBoolean stopFlag = new AtomicBoolean(false);

        private HashMap<Integer, HashMap<String, Double>> loadedValues;

        private playbackThread(HashMap<Integer, HashMap<String, Double>> loadedValues) {
            this.loadedValues = loadedValues;
        }

        private void stopThread() {
            System.out.println("StopThread called");

            pauseFlag.set(false);
            synchronized (pauseFlag) {
                pauseFlag.notify();
            }
            stopFlag.set(true);
        }

        private void pause() {
            pauseFlag.set(true);
        }

        private void play() {
            pauseFlag.set(false);
            synchronized (pauseFlag) {
                pauseFlag.notify();
            }
        }

        private boolean isPaused() {
            return pauseFlag.get();
        }

        private boolean isRunning() {
            return !(pauseFlag.get() || stopFlag.get());
        }

        @Override
        public void run() {
            pauseFlag.set(false);
            stopFlag.set(false);

            Task task = new Task<Void>() {

                @Override
                public Void call() {

                    for (Integer i : loadedValues.keySet()) {
                        for (String key : loadedValues.get(i).keySet()) {

                            if (pauseFlag.get()) {
                                synchronized (pauseFlag) {
                                    while (pauseFlag.get()) {
                                        try {
                                            System.out.println("WAITING.....");
                                            pauseFlag.wait();
                                        } catch (InterruptedException e) {
                                            System.out.println("waiting.....");
                                            Thread.currentThread().interrupt();
                                            return null;
                                        }
                                    }
                                }
                            }

                            if(stopFlag.get()) {
                                System.out.println("STOPPING");
                                return null;
                            }

                            switch (key) {
                                case LIGHT:
                                    if(lightSlider.getValue() != loadedValues.get(i).get(key))
                                        Platform.runLater(() -> lightSlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case PROXIMITY:
                                    if(proximitySlider.getValue() != loadedValues.get(i).get(key))
                                        Platform.runLater(() -> proximitySlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case TEMPERATURE:
                                    if(temperatureSlider.getValue() != loadedValues.get(i).get(key))
                                        Platform.runLater(() -> temperatureSlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case PRESSURE:
                                    if(pressureSlider.getValue() != loadedValues.get(i).get(key))
                                        Platform.runLater(() -> pressureSlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case HUMIDITY:
                                    if(humiditySlider.getValue() != loadedValues.get(i).get(key))
                                        Platform.runLater(() -> humiditySlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case YAW:
                                    if(yawSlider.getValue() != loadedValues.get(i).get(key))
                                        Platform.runLater(() -> yawSlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case PITCH:
                                    if(pitchSlider.getValue() !=  loadedValues.get(i).get(key))
                                        Platform.runLater(() -> pitchSlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                                case ROLL:
                                    if(rollSlider.getValue() !=  loadedValues.get(i).get(key))
                                        Platform.runLater(() -> rollSlider.setValue(loadedValues.get(i).get(key)));
                                    break;
                            }
                        }

                        try {
                            Thread.sleep(playbackSpeed.get());
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }

                    return null;
                }
            };

            task.setOnSucceeded(event -> {
                if(loopBox.isSelected()) {
                    System.out.println("RESTARTING");
                    if(!stopFlag.get())
                        restartThread();
                } else {
                    System.out.println("FINISHED");
                    stopFlag.set(true);
                    pauseFlag.set(false);
                    wasPaused = false;
                    playButton.setDisable(false);
                    recordButton.setDisable(false);

                    setImage("/resources/play_cropped.png", null, playButton);
                }
            });

            new Thread(task).start();
        }
    }
}
