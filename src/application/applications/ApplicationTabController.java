package application.applications;

import application.ADBUtil;
import application.logcat.LogCatTabController;
import application.utilities.Utilities;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

import static application.utilities.ADB.*;

public class ApplicationTabController implements Initializable {

    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\applications";

    private final String EXTENSION = ".apk";

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
    private TreeTableView<Intent> intentsTableView;

//    @FXML
//    private TreeTableColumn<String, Intent> componentColumn;

//    @FXML
//    private TreeItem<String> componentItem;
//    @FXML
//    private TreeItem<String> actionItem;
//    @FXML
//    private TreeItem<String> categoryItem;
//    @FXML
//    private TreeItem<String> mimeTypeItem;

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

    private ObservableList<String> appsOnPCList;
    private ObservableList<String> appsOnDeviceList;

//    private AndroidApplication androidApplication;

    private File directory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        directory = new File(DIRECTORY);

        initializeTableView();
        initializeTableTreeView();
        initializeButtons();
        updatePCListView();
    }

    private void initializeTableView() {
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

//    private ObservableValue<String> cunt() {
//        return new ReadOnlyObjectWrapper(p.getValue().getValue().getComponent());
//    }

    private void initializeTableTreeView() {
        //Creating tree items
//        ArrayList<StringProperty> actions = new ArrayList<>();
//        ArrayList<StringProperty> categories = new ArrayList<>();
//        ArrayList<StringProperty> mimeTypes = new ArrayList<>();
//
//        Intent childNode1Value = new Intent(new SimpleStringProperty("Child Node 1"), actions, categories, mimeTypes);
//        Intent childNode2Value = new Intent(new SimpleStringProperty("Child Node 2"), actions, categories, mimeTypes);
//        Intent childNode3Value = new Intent(new SimpleStringProperty("Child Node 3"), actions, categories, mimeTypes);
//
//        final TreeItem<Intent> childNode1 = new TreeItem<>(childNode1Value);
//        final TreeItem<Intent> childNode2 = new TreeItem<>(childNode2Value);
//        final TreeItem<Intent> childNode3 = new TreeItem<>(childNode3Value);
//
//        //Creating the root element
//        Intent intent = new Intent(new SimpleStringProperty("Root node"), actions, categories, mimeTypes);
//        final TreeItem<Intent> root = new TreeItem<>(intent);
//        root.setExpanded(true);
//
//        //Adding tree items to the root
//        root.getChildren().setAll(childNode1, childNode2 ,childNode3);
//
//        TreeTableColumn<Intent,String> column = new TreeTableColumn<>("Intents");
//        column.setPrefWidth(150);
//
//        //Defining cell content
//        column.setCellValueFactory(
//                (TreeTableColumn.CellDataFeatures<Intent, String> param) ->
//                        new ReadOnlyStringWrapper(param.getValue().getValue().getComponent())
//        );
//
////        TreeTableView<Intent> treeTableView = new TreeTableView<Intent>(root);
////        treeTableView.getColumns().setAll(column);
////        pane.getChildren().add(treeTableView);
//
//
//
//        intentsTableView.setRoot(root);
//        intentsTableView.getColumns().setAll(column);
////       // intentsTableView.getColumns().add(column);
////        intentsTableView.setPrefWidth(152);
////        intentsTableView.setShowRoot(true);
//
//        ObservableList <String> components = FXCollections.observableArrayList();
//        components.add("one");
//        components.add("two");
//        components.add("three");
//        for(Intent intent : application.intents())
  //          components.add(intent.componentProperty().get());

        //componentColumn.setCellValueFactory(param -> {
      //      param.getValue().getValue().componentProperty();
     //       return null;
    //    });

        //componentColumn.getColumns().clear();

//        componentItem.getChildren().add(actionItem);
//        componentItem.getChildren().add(categoryItem);
//        componentItem.getChildren().add(mimeTypeItem);
//        intentsTableView.setRoot(componentItem);
   }

   private void updateIntentsTable(AndroidApplication androidApplication) {
       //Creating tree items
       ArrayList<StringProperty> actions = new ArrayList<>();
       ArrayList<StringProperty> categories = new ArrayList<>();
       ArrayList<StringProperty> mimeTypes = new ArrayList<>();

       TreeTableColumn<Intent,String> column = new TreeTableColumn<>("Intents");
       column.setPrefWidth(150);

       //Defining cell content
       column.setCellValueFactory(
               (TreeTableColumn.CellDataFeatures<Intent, String> param) ->
                       new ReadOnlyStringWrapper(param.getValue().getValue().getComponent())
       );

       //Creating the root element
       Intent intent = new Intent(new SimpleStringProperty("Components"), actions, categories, mimeTypes);
       final TreeItem<Intent> root = new TreeItem<>(intent);
       root.setExpanded(true);

       ObservableList<Intent> intents = androidApplication.intents();

       for(Intent in : intents) {
           root.getChildren().add(new TreeItem<>(in));
       }

       intentsTableView.setRoot(root);
       intentsTableView.getColumns().setAll(column);

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
                appsOnDeviceListView.setItems(appsOnDeviceList);

                

            });

            new Thread(task).start();
        } catch (NullPointerException ignored) {}
    }

    @FXML
    private void handleAppsListViewClicked(MouseEvent mouseEvent) {
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
            AndroidApplication androidApplication = task.getValue();
            applicationTableView.getItems().add(androidApplication);

            updateIntentsTable(androidApplication);
            //componentItem.getChildren().
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

    private void enableButtons() {
        openButton.setDisable(false);
        closeButton.setDisable(false);
        uninstallButton.setDisable(false);
        copyButton.setDisable(false);
    }

    private void initializeButtons() {
        deleteButton.setDisable(true);
        installButton.setDisable(true);
        openButton.setDisable(true);
        closeButton.setDisable(true);
        uninstallButton.setDisable(true);
        copyButton.setDisable(true);
        Utilities.setImage("/resources/delete.png", "Delete file", deleteButton);
        Utilities.setImage("/resources/pop_out.png", null, showLogCatButton);
        Utilities.setImage("/resources/refresh.png", null, refreshButton);
    }
}
