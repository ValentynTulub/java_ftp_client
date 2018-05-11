package net.metryumora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import static net.metryumora.controllers.ApplicationStage.MAIN;

public class ConnectWindowController extends BasicController {

    @FXML
    private TextField ftpTF;
    @FXML
    private TextField loginTF;
    @FXML
    private TextField passwordTF;

    @FXML
    public void initialize() {
    }

    @FXML
    public void connect() {
        FTPClient client = getDispatcher().getClient();
        try {
            client.connect(ftpTF.getText());
            client.enterLocalPassiveMode();
            try {
                client.login(loginTF.getText(), passwordTF.getText());
                MainWindowController mainWindowController = (MainWindowController) getDispatcher().getController(MAIN);
                MainWindowController.setCurrentPath("/");
                mainWindowController.loadFromCurrentPath();
                returnToMainWindow();
            } catch (IOException e) {
                showAlert("Failed to log in. Check your login and password.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            showAlert("Server not found!", Alert.AlertType.ERROR);
        }

    }
}
