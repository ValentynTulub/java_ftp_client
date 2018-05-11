package net.metryumora.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static net.metryumora.controllers.ApplicationStage.CONNECT;
import static net.metryumora.controllers.ApplicationStage.MAIN;

public class MainWindowController extends BasicController {
    private static final String ROOT = "/";
    private static String currentPath = ROOT;

    private final FileChooser fileChooserSave = new FileChooser();
    private final FileChooser fileChooserOpen = new FileChooser();

    @FXML
    private TableView<FTPFile> filesTableView;
    @FXML
    private ObservableList<FTPFile> ftpFiles;
    @FXML
    private TextField tfAddressBar;

    @FXML
    private void initialize() {
        updateAddressBar();
        setTableViewRowFactory();
        initializeColumns();
    }

    private void setTableViewRowFactory() {
        filesTableView.setRowFactory(tableView -> {
            final TableRow<FTPFile> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem item = new MenuItem("Open/Download");
            item.setOnAction(event -> {
                FTPFile ftpFile = row.getItem();
                openFolderOrDownloadFile(ftpFile);
            });
            menu.getItems().add(item);

            MenuItem deleteFileItem = new MenuItem("Delete");
            deleteFileItem.setOnAction(event -> {
                FTPFile ftpFile = row.getItem();
                deleteFile(ftpFile);
            });
            menu.getItems().add(deleteFileItem);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu));

            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        FTPFile ftpFile = row.getItem();
                        openFolderOrDownloadFile(ftpFile);
                    }
                }
            });

            return row;
        });
    }

    private void deleteFile(FTPFile ftpFile) {
        try {
            if (!getFTPClient().deleteFile(currentPath + ftpFile.getName())) {
                showAlert("Unable to delete file! You may be missing necessary rights.", Alert.AlertType.ERROR);
            } else {
                loadFromCurrentPath();
            }
        } catch (IOException e) {
            showAlert("Unable to delete file! You may be missing necessary rights.", Alert.AlertType.ERROR);
        }
    }

    private void openFolderOrDownloadFile(FTPFile ftpFile) {
        if (ftpFile.isFile()) {
            fileChooserSave.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooserSave.setInitialFileName(ftpFile.getName());
            downloadFile(ftpFile);
        } else if (ftpFile.isDirectory()) {
            if (!ftpFile.getName().equals("↑")) {
                currentPath += ftpFile.getName() + "/";
                updateAddressBar();
                loadFromCurrentPath();
            } else {
                goUp();
            }
        }
    }

    private void downloadFile(FTPFile ftpFile) {
        try {
            File fileToDownload = fileChooserSave.showSaveDialog(getDispatcher().getStage(MAIN));
            if (fileToDownload != null) {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileToDownload));
                getFTPClient().retrieveFile(currentPath + ftpFile.getName(), outputStream);
                outputStream.close();
            }
        } catch (IOException e) {
            showAlert("Unable to download file!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void uploadFile() {
        if (!getFTPClient().isConnected()) {
            showAlert("You are not connected to a server.", Alert.AlertType.ERROR);
            return;
        }
        try {
            File fileToUpload = fileChooserOpen.showOpenDialog(getStage(MAIN));
            if (fileToUpload != null) {
                FileInputStream fileInputStream = new FileInputStream(fileToUpload);
                getFTPClient().appendFile(currentPath + fileToUpload.getName(), fileInputStream);
                fileInputStream.close();
                loadFromCurrentPath();
            }
        } catch (IOException e) {
            showAlert("Unable to download file! You may be missing necessary rights.", Alert.AlertType.ERROR);
        }
    }

    private FTPClient getFTPClient() {
        return getDispatcher().getClient();
    }

    private void initializeColumns() {
        TableColumn<FTPFile, String> fileNamesColumn = new TableColumn<>("Name");
        fileNamesColumn.setPrefWidth(400);
        fileNamesColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<FTPFile, String> fileSizesColumn = new TableColumn<>("Size, Kb");
        fileSizesColumn.setPrefWidth(50);
        fileSizesColumn.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            if (param.getValue().isFile()) {
                property.setValue(String.format("%.1f", param.getValue().getSize() / 1000.0));
            } else {
                property.setValue("folder");
            }
            return property;
        });
        TableColumn<FTPFile, String> fileDateTimeColumn = new TableColumn<>("Date");
        fileDateTimeColumn.setPrefWidth(100);
        fileDateTimeColumn.setCellValueFactory(param -> {
            SimpleStringProperty property = new SimpleStringProperty();
            if (param.getValue().getTimestamp() != null) {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                property.setValue(dateFormat.format(param.getValue().getTimestamp().getTime()));
            } else {
                property.setValue("-");
            }
            return property;
        });
        filesTableView.getColumns().addAll(fileNamesColumn, fileSizesColumn, fileDateTimeColumn);
    }

    @FXML
    public void loadFromCurrentPath() {
        loadFiles(getFTPClient(), currentPath);
    }

    @FXML
    public void loadFromAddressBar() {
        currentPath = tfAddressBar.getText();
        loadFromCurrentPath();
    }

    private void loadFiles(FTPClient client, String path) {
        if (!getFTPClient().isConnected()) {
            showAlert("You are not connected to a server.", Alert.AlertType.ERROR);
            return;
        }
        try {
            ftpFiles = FXCollections.observableArrayList(client.listFiles(path));
        } catch (IOException e) {
            showAlert("Unable to load files!", Alert.AlertType.ERROR);
            currentPath = ROOT;
        }
        sortFiles(ftpFiles);

        FTPFile previousFolderLink = new FTPFile();
        previousFolderLink.setType(FTPFile.DIRECTORY_TYPE);
        previousFolderLink.setName("↑");
        ftpFiles.add(0, previousFolderLink);

        filesTableView.setItems(ftpFiles);
    }

    private static void sortFiles(ObservableList<FTPFile> ftpFiles) {
        ftpFiles.sort((o1, o2) -> {
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            } else if (o1.isFile() && o2.isDirectory()) {
                return 1;
            } else {
                return Collator.getInstance().compare(o1.getName(), o2.getName());
            }
        });
    }

    @FXML
    private void showConnectionWindow() {
        getDispatcher().getStage(CONNECT).show();
    }

    @FXML
    private void shutdown() {
        try {
            getFTPClient().disconnect();
        } catch (IOException e) {
            showAlert("Unable to disconnect from server!", Alert.AlertType.ERROR);
        }
        getDispatcher().getStages().forEach(Stage::close);
    }

    @FXML
    private void createFolder() {
        if (!getFTPClient().isConnected()) {
            showAlert("You are not connected to a server.", Alert.AlertType.ERROR);
            return;
        }
        TextInputDialog dialog = new TextInputDialog("new");
        dialog.setTitle("Choose name");
        dialog.setHeaderText("Please, choose new directory name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                if (!getFTPClient().makeDirectory(currentPath + name)) {
                    showAlert("Unable to crete directory. You may be missing necessary rights.",
                            Alert.AlertType.ERROR);
                } else {
                    loadFromCurrentPath();
                }
            } catch (IOException e) {
                showAlert("Unable to crete directory. You may be missing necessary rights.",
                        Alert.AlertType.ERROR);
            }
        });
    }

    private void goUp() {
        if (currentPath.length() > 1) {
            currentPath = currentPath.substring(0, currentPath.length() - 2);
        }
        int indexOfSlash = currentPath.lastIndexOf("/");
        if (indexOfSlash != 0) {
            currentPath = currentPath.substring(0, indexOfSlash + 1);
        } else {
            currentPath = ROOT;
        }
        loadFromCurrentPath();
        updateAddressBar();
    }

    private void updateAddressBar() {
        tfAddressBar.setText(currentPath);
    }

    @FXML
    private void showAbout() {
        showAlert("This application is developed by Valentyn Tulub.", Alert.AlertType.INFORMATION);
    }

    public static void setCurrentPath(String currentPath) {
        MainWindowController.currentPath = currentPath;
    }

    public String getCurrentPath() {
        return currentPath;
    }
}
