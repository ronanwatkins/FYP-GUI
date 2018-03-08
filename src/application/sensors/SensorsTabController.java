package application.sensors;

import application.TelnetServer;
import application.XMLUtil;
import application.sensors.model.AccelerometerModel;
import application.sensors.model.GyroscopeModel;
import application.sensors.model.MagneticFieldModel;
import application.utilities.ThreeDimensionalVector;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jdk.nashorn.internal.objects.annotations.Function;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SensorsTabController implements Initializable {

    //region fields
    private final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\sensors";

    private final String LIGHT = "light";
    private final String HUMIDITY = "humidity";
    private final String PRESSURE = "pressure";
    private final String PROXIMITY = "proximity";
    private final String TEMPERATURE = "temperature";
    private final String YAW = "yaw";
    private final String PITCH = "pitch";
    private final String ROLL = "roll";
    private final String LOCATION = "location";
    private final String BATTERY = "battery";

    private final int PERIOD = 10; //record/playback period in ms

    @FXML
    private Slider lightSlider;
    @FXML
    private Slider temperatureSlider;
    @FXML
    private Slider pressureSlider;
    @FXML
    private Slider proximitySlider;
    @FXML
    private Slider humiditySlider;
    @FXML
    private Slider yawSlider;
    @FXML
    private Slider pitchSlider;
    @FXML
    private Slider rollSlider;

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
    private Button recordButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;

    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private CheckBox loopBox;

    @FXML
    private AnchorPane phonePane;
    @FXML
    private AnchorPane buttonsPane;

    @FXML
    private Box phone;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private RadioButton rotateRadioButton;
    @FXML
    private RadioButton moveRadioButton;

    private boolean isRotate = true;

    private int yawValue;
    private int pitchValue;
    private int rollValue;

    private int pitchBeforeValue;

    private final double GRAVITY_CONSTANT = 9.80665;
    private final double MAGNETIC_NORTH = 22874.1;
    private final double MAGNETIC_EAST = 5939.5;
    private final double MAGNETIC_VERTICAL = 43180.5;

    private static final DecimalFormat TWO_DECIMAL_FORMAT = new DecimalFormat("#0.00");

    private AccelerometerModel accelerometerModel;
    private GyroscopeModel gyroscopeModel;
    private MagneticFieldModel magneticFieldModel;

    private double mousePosX = 0;
    private double mousePosY = 0;
    private double mouseMoveX = 0;
    private double mouseMoveZ = 0;

    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private Map<String, Double> sensorValues = new HashMap<>();
    private MyThread thread;

    private XMLUtil xmlUtil;

    private boolean isRecording = false;
    private boolean isLoaded = false;
    private boolean wasPaused = false;
    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setSpecularColor(Color.ORANGE);
        redMaterial.setDiffuseColor(Color.RED);

        phone.setMaterial(redMaterial);
        phone.getTransforms().addAll(rotateZ, rotateY, rotateX);
        phone.setManaged(false);

        HBox phonePaneHBox = new HBox();
        phone.setLayoutX(125);
        phone.setLayoutY(125);
        phonePaneHBox.getChildren().add(phone);

        phonePane.getChildren().addAll(phonePaneHBox);
        phonePane.setStyle("-fx-background-color: gray;");

        rotateRadioButton.setSelected(true);

        //region intialise
        pitchSlider.setValue(-90);

        if(!isLoaded) {
            playButton.setVisible(false);
            pauseButton.setVisible(false);
            loopBox.setVisible(false);
        }

        xmlUtil = new XMLUtil();
        saveButton.setDisable(true);
        initHashMap();

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
        //endregion
    }

    //region buttonHandlers
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
            System.out.println(file.getAbsolutePath());
            loadedValues = xmlUtil.loadXML(file);

            if(thread != null) {
                thread.stopThread();

                wasPaused = false;
                playButton.setDisable(false);
            }

            thread = new MyThread(loadedValues);

            isLoaded = true;

            playButton.setVisible(true);
            pauseButton.setVisible(true);
            loopBox.setVisible(true);

            pauseButton.setDisable(true);
        }
    }

    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        isRecording = false;

        recordButton.setDisable(false);
        saveButton.setDisable(true);

        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DIRECTORY));

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if(file != null)
            xmlUtil.saveFile(file);
    }

    @FXML
    private void handleRecordButtonClicked(ActionEvent event) {
        isRecording = true;

        recordButton.setDisable(true);
        saveButton.setDisable(false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(isRecording) {
                    xmlUtil.addElement(sensorValues);
                }
            }
        }, 0, PERIOD);
    }

    @FXML
    private void handlePlayButtonClicked(ActionEvent event) {
        playButton.setDisable(true);
        recordButton.setDisable(true);
        pauseButton.setDisable(false);

        if(wasPaused) {
            thread.play();
            System.out.println("RESUMING");
        } else {
            thread.run();
            System.out.println("STARTING");
        }
    }

    @FXML
    private void handlePauseButtonClicked(ActionEvent event) {
        wasPaused = true;

        pauseButton.setDisable(true);
        recordButton.setDisable(false);
        playButton.setDisable(false);

        System.out.println("PAUSING");
        thread.pause();
    }
    //endregion

    private void handleSliderEvents() {
        lightSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            double lightValue = newvalue.doubleValue();
            sensorValues.put(LIGHT, lightValue);
            lightLabel.setText(String.format("%.2f",lightValue)+"");
            TelnetServer.setSensor("light " + String.format("%.2f",lightValue));
        });

        temperatureSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            double temperatureValue = newvalue.doubleValue();
            sensorValues.put(TEMPERATURE,  temperatureValue);
            temperatureLabel.setText(String.format("%.2f",temperatureValue)+"");
            TelnetServer.setSensor("temperature " + String.format("%.2f",temperatureValue));
        });

        pressureSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            double pressureValue = newvalue.doubleValue();
            sensorValues.put(PRESSURE, pressureValue);
            pressureLabel.setText(String.format("%.2f",pressureValue)+"");
            TelnetServer.setSensor("pressure " + String.format("%.2f",pressureValue));
        });

        proximitySlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            double proximityValue = newvalue.doubleValue();
            sensorValues.put(PROXIMITY, proximityValue);
            proximityLabel.setText(String.format("%.2f",proximityValue)+"");
            TelnetServer.setSensor("proximity " + String.format("%.2f",proximityValue));
        });

        humiditySlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            double humidityValue = newvalue.doubleValue();
            sensorValues.put(HUMIDITY, humidityValue);
            humidityLabel.setText(String.format("%.2f",humidityValue)+"");
            TelnetServer.setSensor("humidity " + String.format("%.2f",humidityValue));
        });

        yawSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            yawValue = newvalue.intValue();
            sensorValues.put(YAW, (double) yawValue);

            updateSliderValues();
        });

        pitchSlider.setOnMouseDragged((mouseEvent) -> rotateX.setAngle(180-pitchBeforeValue-270));

        pitchSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            pitchBeforeValue = pitchValue = newvalue.intValue();
            sensorValues.put(PITCH, (double) pitchBeforeValue);
            if(thread != null) {
                if (thread.isRunning()) {
                    rotateX.setAngle(180 - pitchBeforeValue - 270);
                }
            }

            updateSliderValues();
        });

        rollSlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            rollValue = newvalue.intValue();
            sensorValues.put(ROLL, (double) rollValue);

            updateSliderValues();
        });
    }

    private void handleMouseEvents() {
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

        phonePane.setOnMousePressed((MouseEvent mouseEvent) -> {
            mousePosX = mouseEvent.getSceneX();
            mousePosY = mouseEvent.getSceneY();

//            mouseMoveX = accelerometerModel.getMoveX();
            mouseMoveX = 10;
            mouseMoveZ = accelerometerModel.getMoveZ();
        });

        phonePane.setOnMouseDragged((MouseEvent mouseEvent) -> {
            if(isRotate) {

                double dx = (mousePosX - mouseEvent.getSceneX());
                double dy = (mousePosY - mouseEvent.getSceneY());

                if (mouseEvent.isPrimaryButtonDown()) {
                    double rotateXAngle = (rotateX.getAngle() - (dy * 10 / phone.getHeight() * 360) * (Math.PI / 180)) % 360;
                    if (rotateXAngle < 0)
                        rotateXAngle += 360;
                    rotateX.setAngle(rotateXAngle);
                    System.out.println("rotateXAngle: " + rotateXAngle);

                    if (rotateXAngle > 270 || (rotateXAngle < 90 && rotateXAngle > 0)) {
                        if (rotateXAngle > 270)
                            pitchValue = (int) (360 - rotateXAngle - 90);
                        else
                            pitchValue = (int) (180 - rotateXAngle - 270);
                    } else if (rotateXAngle <= 270 || rotateXAngle >= 90) {
                        if (rotateXAngle <= 270 && rotateXAngle <= 180)
                            pitchValue = (int) (270 - rotateXAngle);
                        else
                            pitchValue = (int) (360 - rotateXAngle - 90);
                    }

                    pitchSlider.setValue(pitchValue);

                    updateSliderValues();

                    double rotateYAngle = (rotateY.getAngle() - (dx * 10 / phone.getWidth() * -360) * (Math.PI / 180)) % 360;
                    if (rotateYAngle < 0) {
                        rotateYAngle += 360;
                    }

                    rotateY.setAngle(rotateYAngle);
                    rollSlider.setValue(rotateYAngle);

                    updateSliderValues();
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

    private void updateSliderValues() {
        //System.out.println("in updateSliderValues");
        // Restrict pitch value to -90 to +90
        if (pitchValue < -90) {
            pitchValue = -180 - pitchValue;
            //yawValue += 180;
            //rollValue += 180;
        } else if (pitchValue > 90) {
            pitchValue = 180 - pitchValue;
            //yawValue += 180;
            //rollValue += 180;
        }

        // yaw from 0 to 360
        if (yawValue < 0) {
            yawValue = yawValue + 360;
            //yawValue = yawValue + 180;
        }
        if (yawValue >= 360) {
            yawValue = yawValue - 360;
            //yawValue = yawValue + 180;
        }

        if (rollValue > 360) {
            rollValue -= 360;
        }
        if(rollValue < 0) {
            rollValue += 360;
        }

        // roll from -180 to + 180
//        if (rollValue >= 180) {
//            rollValue -= 360;
//        }

        updateMagneticFieldData();
        updateAccelerometerData();

        rotateZ.setAngle(yawValue);
        rotateY.setAngle(rollValue);

        yawLabel.setText(yawValue + "");
        pitchLabel.setText(pitchValue + "");
        rollLabel.setText(rollValue + "");

//        System.out.println("yawValue: " + yawValue);
//        System.out.println("pitchValue: " + pitchValue);
//        System.out.println("rollValue: " + pitchValue);
//        System.out.println("finished updateSliderValues");
    }

    private void updateSensorValues() {
        while(true) {
            gyroscopeModel.refreshAngularSpeed(10, pitchValue, yawValue, rollValue);

            accelerometerModel.updateSensorReadoutValues();
            gyroscopeModel.updateSensorReadoutValues();
            magneticFieldModel.updateSensorReadoutValues();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gyroscopeLabel.setText(TWO_DECIMAL_FORMAT.format(gyroscopeModel.getReadGyroscopePitch())
                            + ", "
                            + TWO_DECIMAL_FORMAT.format(gyroscopeModel.getReadGyroscopeYaw())
                            + ", "
                            + TWO_DECIMAL_FORMAT.format(gyroscopeModel.getReadGyroscopeRoll()));

                    magneticFieldLabel.setText(TWO_DECIMAL_FORMAT.format(magneticFieldModel.getReadCompassX())
                            + ", "
                            + TWO_DECIMAL_FORMAT.format(magneticFieldModel.getReadCompassY())
                            + ", "
                            + TWO_DECIMAL_FORMAT.format(magneticFieldModel.getReadCompassZ()));

                    accelerometerLabel.setText(TWO_DECIMAL_FORMAT.format(accelerometerModel.getReadAccelerometerX())
                            + ", "
                            + TWO_DECIMAL_FORMAT.format(accelerometerModel.getReadAccelerometerY())
                            + ", "
                            + TWO_DECIMAL_FORMAT.format(accelerometerModel.getReadAccelerometerZ()));
                }
            });
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
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
        thread.run();
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

    private class MyThread extends Thread {

        private final AtomicBoolean pauseFlag = new AtomicBoolean(false);
        private final AtomicBoolean stopFlag = new AtomicBoolean(false);

        private HashMap<Integer, HashMap<String, Double>> loadedValues;

        private MyThread (HashMap<Integer, HashMap<String, Double>> loadedValues) {
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

        private boolean isRunning() {
            return !(pauseFlag.get() && stopFlag.get());
        }

        @Override
        public void run() {
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

                                Platform.runLater(() -> {

                                    switch (key) {
                                        case LIGHT:
                                            if(lightSlider.getValue() != loadedValues.get(i).get(key)) {
                                                lightSlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case PROXIMITY:
                                            if(proximitySlider.getValue() != loadedValues.get(i).get(key)) {
                                                proximitySlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case TEMPERATURE:
                                            if(temperatureSlider.getValue() != loadedValues.get(i).get(key)) {
                                                temperatureSlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case PRESSURE:
                                            if(pressureSlider.getValue() != loadedValues.get(i).get(key)) {
                                                pressureSlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case HUMIDITY:
                                            if(humiditySlider.getValue() != loadedValues.get(i).get(key)) {
                                                humiditySlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case YAW:
                                            if(yawSlider.getValue() != loadedValues.get(i).get(key)) {
                                                yawSlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case PITCH:
                                            if(pitchSlider.getValue() !=  loadedValues.get(i).get(key)) {
                                                pitchSlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                        case ROLL:
                                            if(rollSlider.getValue() !=  loadedValues.get(i).get(key)) {
                                                rollSlider.setValue(loadedValues.get(i).get(key));
                                            }
                                            break;
                                    }
                                });
                            }

                            try {
                                Thread.sleep(PERIOD);
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
                        wasPaused = false;
                        pauseButton.setDisable(true);
                        playButton.setDisable(false);
                        recordButton.setDisable(false);
                    }
                });

                new Thread(task).start();
        }
    }
}
