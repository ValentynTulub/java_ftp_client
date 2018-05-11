package net.metryumora.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import net.metryumora.ApplicationLauncher;

import static net.metryumora.controllers.ApplicationStage.MAIN;

public class BasicController {

    @FXML
    private void initialize() {
    }

    Dispatcher getDispatcher() {
        return ApplicationLauncher.getDispatcher();
    }

    BasicController getController(ApplicationStage stageController) {
        return getDispatcher().getController(stageController);
    }

    Stage getStage(ApplicationStage stage) {
        return getDispatcher().getStage(stage);
    }

    Stage getStage(int stageId) {
        return getDispatcher().getStage(stageId);
    }

    Stage getCurrentStage() {
        return getDispatcher().getStages().get(getDispatcher().getControllers().indexOf(this));
    }

    @FXML
    public void returnToMainWindow() {
        switchTo(MAIN);
    }

    @FXML
    public void switchTo(ApplicationStage stage) {
        getStage(getDispatcher().getControllers().indexOf(this)).hide();
        getStage(stage).show();
    }

    public static void showAlert(String message, Alert.AlertType alertType) {
        String title = "";
        switch (alertType) {
            case ERROR: {
                title = "Error!";
                break;
            }
            case INFORMATION: {
                title = "Message";
                break;
            }
            case WARNING: {
                title = "Warning!";
                break;
            }
            case CONFIRMATION: {
                title = "Confirm action";
                break;
            }
        }
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
