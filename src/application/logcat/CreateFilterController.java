package application.logcat;

import application.utilities.ApplicationUtils;
import application.utilities.Showable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static application.logcat.LogCatTabController.FILTER_DIRECTORY;

public class CreateFilterController implements Initializable, Showable<LogCatTabController>, ApplicationUtils {

    @FXML
    private Label errorLabel;

    @FXML
    private Button saveButton;

    @FXML
    private TextField filterNameField;
    @FXML
    private TextField applicationNameField;
    @FXML
    private TextField PIDField;
    @FXML
    private TextField logMessageField;
    @FXML
    private TextField logTagField;

    @FXML
    private ComboBox<String> logLevelComboBox;

    private boolean setPIDFieldDisable = false;
    private boolean isEditMode = false;

    private static LogCatTabController controller;
    private Filter filter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();

        if(!resources.toString().isEmpty()) {
            isEditMode = true;

            filter = Filter.getFilter(resources.toString());
            filterNameField.setText(filter.getFilterName());
            applicationNameField.setText(filter.getApplicationName());
            PIDField.setText(filter.getPID());
            logMessageField.setText(filter.getLogMessage());
            logTagField.setText(filter.getLogTag());
            System.out.println("initialize>> " + filter.getLogLevelOrdinal());
            logLevelComboBox.getSelectionModel().select(filter.getLogLevelOrdinal());

            saveButton.setDisable(false);
        }
    }

    @Override
    public void newWindow(LogCatTabController logCatTabController, File file) throws IOException {
        controller = logCatTabController;
        FXMLLoader fxmlLoader = new FXMLLoader(logCatTabController.getClass().getResource("/application/logcat/CreateFilter.fxml"));
        Bundle bundle = new Bundle(logCatTabController.getFileToEditName());
        fxmlLoader.setResources(bundle);

        Parent root = fxmlLoader.load();
        root.getStylesheets().add("/application/global.css");

        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Create Filter");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleLogLevelComboBoxPressed(ActionEvent event) {
    }

    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        int selectedLevel = logLevelComboBox.getSelectionModel().getSelectedIndex();
        selectedLevel = selectedLevel == -1 ? 6 : selectedLevel;

        if(isEditMode && !filterNameField.getText().equals(filter.getFilterName())) {
            File fileToDelete = new File(FILTER_DIRECTORY + "\\" + filter.getFilterName() + ".xml");
            if(fileToDelete.delete()) {
                System.out.println("File deleted");
            }
        }

        System.out.println("handleSaveButtonClicked>> selectedLevel: " + selectedLevel);

        Filter filter = new Filter(filterNameField.getText(),
                applicationNameField.getText(),
                PIDField.getText(),
                logMessageField.getText(),
                logTagField.getText(),
                selectedLevel);

        filter.save();

        controller.updateFiltersComboBox();
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelButtonClicked(ActionEvent event) {
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleFilterNameFieldAction(KeyEvent keyEvent) {
        if(!setPIDFieldDisable)
            saveButton.setDisable(filterNameField.getText().isEmpty());
    }

    @FXML
    private void handlePIDFieldAction(KeyEvent keyEvent) {
        if(!PIDField.getText().isEmpty()) {
            try {
                if(PIDField.getText().startsWith("-"))
                    throw new NumberFormatException();

                Integer.parseInt(PIDField.getText());
                errorLabel.setText("");
                if (!filterNameField.getText().isEmpty())
                    saveButton.setDisable(false);

                setPIDFieldDisable = false;
            } catch (NumberFormatException nfe) {
                errorLabel.setText("PID should be a positive number");
                setPIDFieldDisable = true;
                saveButton.setDisable(true);
            }
        } else {
            errorLabel.setText("");

            if (!filterNameField.getText().isEmpty())
                saveButton.setDisable(false);
        }
    }

    @Override
    public void initializeButtons() {
        saveButton.setDisable(true);
    }
}
