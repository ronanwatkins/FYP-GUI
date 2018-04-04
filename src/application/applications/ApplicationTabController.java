package application.applications;

import application.ADBUtil;
import application.logcat.LogCatTabController;
import application.utilities.ApplicationUtils;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static application.utilities.ADB.*;

public class ApplicationTabController implements Initializable, ApplicationUtils {
    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\applications";
    private final String EXTENSION = ".apk";

    @FXML
    private TextField searchField;

    @FXML
    private TableView<AndroidApplication> applicationTableView;

    @FXML
    private TableColumn<AndroidApplication, String> APKNameColumn;
    @FXML
    private TableColumn<AndroidApplication, String> APKPathColumn;
    @FXML
    private TableColumn<AndroidApplication, String> versionCodeColumn;
    @FXML
    private TableColumn<AndroidApplication, Integer> userIdColumn;
    @FXML
    private TableColumn<AndroidApplication, String> dataDirColumn;

    @FXML
    private TableView<Intent> intentsTableView;

//    @FXML
//    private TableColumn<Intent, ObservableList<String>> actionsColumn;

    @FXML
    private TableColumn<ObservableList<StringProperty>, String> actionsColumn;
    @FXML
    private TableColumn categoriesColumn;
    @FXML
    private TableColumn mimeTypesColumn;

//    @FXML
//    private TableColumn<Intent, ObservableList<StringProperty>> actionsColumn;
//
//    @FXML
//    private TableColumn<ObservableList<StringProperty>, String> categoriesColumn;
//    @FXML
//    private TableColumn<ObservableList<StringProperty>, String> mimeTypesColumn;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private Button showLogCatButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button installButton;
    @FXML
    private Button openButton;
    @FXML
    private Button uninstallButton;
    @FXML
    private Button copyButton;
    @FXML
    private Button closeButton;

    @FXML
    private AnchorPane pane;

    @FXML
    private ListView<String> appsOnPCListView;
    @FXML
    private ListView<String> appsOnDeviceListView;
    @FXML
    private ListView<String> componentsListView;

    private ObservableList<String> appsOnPCList;
    private ObservableList<String> appsOnDeviceList;

    private AndroidApplication androidApplication;

    private File directory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        directory = new File(DIRECTORY);

        initializeApplicationTableView();
        initializeIntentTableView();
        initializeButtons();
        updatePCListView();
    }

    private void initializeApplicationTableView() {
       // applicationTableView.setRowResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        APKNameColumn.setCellValueFactory(cellData -> cellData.getValue().APKNameProperty());
        APKPathColumn.setCellValueFactory(cellData -> cellData.getValue().APKPathProperty());
        APKPathColumn.setCellFactory(tc -> textWrappingCell());
        versionCodeColumn.setCellValueFactory(cellData -> cellData.getValue().versionCodeProperty());
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        dataDirColumn.setCellValueFactory(cellData -> cellData.getValue().dataDirProperty());
        dataDirColumn.setCellFactory(tc -> textWrappingCell());
    }

    private TableCell<AndroidApplication, String> textWrappingCell() {
        TableCell<AndroidApplication, String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(APKPathColumn.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        return cell;
    }

    private void initializeIntentTableView() {
       // actionsColumn.setCellValueFactory(
        //        (TableColumn.CellDataFeatures<Intent, String>  p) ->
          //              new SimpleStringProperty(":)"));
//        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));
//        actionsColumn.setCellValueFactory(p -> {
//
//            System.out.println("yaboyya:  " + p.getValue());
//
//            final ObservableList row = p.getValue().actionProperty();
//
//            List<Observable> dependencies = new ArrayList<>();
//            for (Object value : row) {
//                if (value instanceof Observable) {
//                    dependencies.add((Observable)value);
//                }
//            }
//            dependencies.add(row);
//
//            return null;
//            return Bindings.createStringBinding(() -> {
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < row.size(); i++) {
//                    //Check for Property objects and append the value
//                    if (row.get(i) instanceof Property) {
//                        sb.append(((Property)row.get(i)).getValue());
//                    }
//                    else {
//                        sb.append(row.get(i));
//                    }
//
//                    if (i+1 < row.size()) {
//                        sb.append(", ");
//                    }
//                }
//                return sb.toString();
//            }, dependencies.toArray(new Observable[dependencies.size()]));
//        });
//        categoriesColumn.setCellValueFactory(p -> {
//            final ObservableList row = p.getValue();
//
//            List<Observable> dependencies = new ArrayList<>();
//            for (Object value : row) {
//                if (value instanceof Observable) {
//                    dependencies.add((Observable)value);
//                }
//            }
//            dependencies.add(row);
//
//            return Bindings.createStringBinding(() -> {
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < row.size(); i++) {
//                    //Check for Property objects and append the value
//                    if (row.get(i) instanceof Property) {
//                        sb.append(((Property)row.get(i)).getValue());
//                    }
//                    else {
//                        sb.append(row.get(i));
//                    }
//
//                    if (i+1 < row.size()) {
//                        sb.append(", ");
//                    }
//                }
//                return sb.toString();
//            }, dependencies.toArray(new Observable[dependencies.size()]));
//        });
//        mimeTypesColumn.setCellValueFactory(p -> {
//            final ObservableList row = p.getValue();
//
//            List<Observable> dependencies = new ArrayList<>();
//            for (Object value : row) {
//                if (value instanceof Observable) {
//                    dependencies.add((Observable)value);
//                }
//            }
//            dependencies.add(row);
//
//            return Bindings.createStringBinding(() -> {
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < row.size(); i++) {
//                    //Check for Property objects and append the value
//                    if (row.get(i) instanceof Property) {
//                        sb.append(((Property)row.get(i)).getValue());
//                    }
//                    else {
//                        sb.append(row.get(i));
//                    }
//
//                    if (i+1 < row.size()) {
//                        sb.append(", ");
//                    }
//                }
//                return sb.toString();
//            }, dependencies.toArray(new Observable[dependencies.size()]));
//        });
    }

    private void updateComponentsListView(AndroidApplication androidApplication) {
        componentsListView.getItems().clear();

        ObservableList<Intent> intents = androidApplication.intents();

        for (Intent intent : intents) {
            String componentName = intent.getComponent();
            if(componentName.contains("/"))
                componentName = "..." + componentName.substring(componentName.indexOf("/"));
            componentsListView.getItems().add(componentName);
        }
    }

    private void updatePCListView() {
        appsOnPCList = FXCollections.observableArrayList();
        try {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                appsOnPCList.add(file.getName());
            }
        } catch (NullPointerException ignored) {}

        appsOnPCListView.setItems(appsOnPCList);
    }

    private void updateDeviceListView() {
        appsOnDeviceList = FXCollections.observableArrayList();
        try {
            appsOnDeviceListView.getItems().clear();
            appsOnDeviceList.clear();

            Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
                @Override
                protected ObservableList<String> call() { return FXCollections.observableArrayList(ADBUtil.listApplications());
                }
            };
            task.setOnSucceeded(event1 -> {
                appsOnDeviceList = task.getValue();
                Collections.sort(appsOnDeviceList);
                appsOnDeviceListView.setItems(filter(searchField.getText(), appsOnDeviceList));
            });

            new Thread(task).start();
        } catch (NullPointerException ignored) {}
    }

    @FXML
    private void handleComponentsListViewClicked(MouseEvent mouseEvent) {
        ObservableList<Intent> intents = androidApplication.intents();

        Intent intent = null;
        String component = componentsListView.getSelectionModel().getSelectedItem().replace("...",  appsOnDeviceListView.getSelectionModel().getSelectedItem());

        for(Intent in : intents) {
            System.out.println(component + " " + in.getComponent() + " " + in.getComponent().equals(component));
            if(in.getComponent().equals(component))
                intent = in;
        }

        System.out.println(intent);

        intentsTableView.getItems().clear();

        ObservableList<Intent> temp = FXCollections.observableArrayList(intent);
        //intentsTableView.setItems(temp);

        actionsColumn.getColumns().add(getColumn(1));


       // intentsTableView.getItems().add(intent);
    }

    private TableColumn<ObservableList<StringProperty>, String> getColumn(int columnIndex) {
        TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
        column.setCellValueFactory(cellDataFeatures -> {
            ObservableList<StringProperty> values = cellDataFeatures.getValue();
            // Pad to current value if necessary:
            for (int index = values.size(); index <= columnIndex; index++) {
                values.add(index, new SimpleStringProperty("cunt fart"));
            }
            return cellDataFeatures.getValue().get(columnIndex);
        });
        column.setCellFactory(TextFieldTableCell.forTableColumn());

        return column;
    }


    @FXML
    private void handleAppsListViewClicked(MouseEvent mouseEvent) {
        System.out.println("handleAppsListViewClicked");
        enableButtons();
        applicationTableView.setPlaceholder(new Label("Loading Application details..."));
        String applicationName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        applicationTableView.getItems().clear();

        Task<AndroidApplication> task = new Task<AndroidApplication>() {
            @Override
            protected AndroidApplication call() {
                return new AndroidApplication(applicationName);
            }
        };
        task.setOnSucceeded(event -> {
            androidApplication = task.getValue();
            applicationTableView.getItems().add(androidApplication);

            updateComponentsListView(androidApplication);
            //updateIntentsTable(androidApplication);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleInstallButtonClicked(ActionEvent event) {
        String appName = appsOnPCListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(installApp(directory.getAbsolutePath() + "\\" + appName));

        updateDeviceListView();
    }

    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {
        String fileName = appsOnPCListView.getSelectionModel().getSelectedItem();
        int fileIndex = appsOnPCListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName);
        if(fileToDelete.delete()) {
            appsOnPCList.remove(fileIndex);
            updatePCListView();
        }
    }

    @FXML
    private void handleOpenButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(openApp(appName));
    }

    @FXML
    private void handleUninstallButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(uninstallApp(appName));
        updateDeviceListView();
    }

    @FXML
    private void handleCopyButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return getAPKFile(appName, directory.getAbsolutePath());
            }
        };
        task.setOnSucceeded(event1 -> {
            resultTextArea.setText(task.getValue());
            updatePCListView();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCloseButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(closeApp(appName));
    }

    @FXML
    private void handleLogCatButtonClicked(ActionEvent event) {
        LogCatTabController logCatTabController = new LogCatTabController();
        try {
            logCatTabController.newWindow(this, null);
        } catch (IOException ioe) {
            resultTextArea.setText(ioe.getMessage());
        }
    }

    @FXML
    private void handleRefreshButtonClicked(ActionEvent event) {
        applicationTableView.getItems().clear();
        updateDeviceListView();
        updatePCListView();
    }

    @FXML
    private void handleAppsOnPCListViewClicked(MouseEvent mouseEvent) {
        try {
            if (appsOnPCListView.getSelectionModel().getSelectedItem().endsWith(EXTENSION)) {
                deleteButton.setDisable(false);
                installButton.setDisable(false);
            }
        } catch (NullPointerException ignored) {}
    }

    @FXML
    private void handleSearchFieldAction(KeyEvent event) {
        appsOnDeviceListView.setItems(filter(searchField.getText(), appsOnDeviceList));
    }

    public String getApplicationName() {
        return appsOnDeviceListView.getSelectionModel().getSelectedItem();
    }

    private void enableButtons() {
        openButton.setDisable(false);
        closeButton.setDisable(false);
        uninstallButton.setDisable(false);
        copyButton.setDisable(false);
    }

    @Override
    public void initializeButtons() {
        deleteButton.setDisable(true);
        installButton.setDisable(true);
        openButton.setDisable(true);
        closeButton.setDisable(true);
        uninstallButton.setDisable(true);
        copyButton.setDisable(true);

        setImage("/resources/delete.png", "Delete file", deleteButton);
        setImage("/resources/pop_out.png", null, showLogCatButton);
        setImage("/resources/refresh.png", null, refreshButton);
    }
}
