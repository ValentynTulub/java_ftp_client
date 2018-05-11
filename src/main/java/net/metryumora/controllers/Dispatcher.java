package net.metryumora.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dispatcher {
    private static Dispatcher dispatcher;

    private List<Stage> stages;
    private List<BasicController> controllers;

    private FTPClient client;

    private Dispatcher() {
    }

    private Dispatcher(Stage primaryStage) {
        stages = new ArrayList<>();
        controllers = new ArrayList<>();
        client = new FTPClient();

        List<ApplicationStage> stages = Arrays.asList(ApplicationStage.class.getEnumConstants());
        stages.forEach(applicationStage -> {
            if (applicationStage.equals(stages.get(0))) {
                initializeScene(primaryStage, applicationStage).show();
            } else {
                initializeScene(null, applicationStage);
            }
        });
    }

    public static Dispatcher getInstance(Stage primaryStage) {
        if (dispatcher == null) {
            dispatcher = new Dispatcher(primaryStage);
        }
        return dispatcher;
    }

    private Stage initializeScene(Stage primaryStage, ApplicationStage applicationStage) {
        String title = applicationStage.getWindowTitle();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(applicationStage.getFxmlFilepath()));
        Stage stage;
        if (primaryStage == null) {
            stage = new Stage();
        } else {
            stage = primaryStage;
        }

        try {
            Parent root = loader.load();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.out.println("Failed to load fxml: " + applicationStage.getFxmlFilepath());
            e.printStackTrace();
        }

        stage.setTitle(title);
        stages.add(stage);

        BasicController controller = loader.getController();
        controllers.add(controller);

        return stage;
    }

    public BasicController getController(ApplicationStage stageController) {
        return controllers.get(stageController.getId());
    }

    public BasicController getController(int controllerId) {
        return controllers.get(controllerId);
    }

    public Stage getStage(ApplicationStage stage) {
        return stages.get(stage.getId());
    }

    public Stage getStage(int stageId) {
        return stages.get(stageId);
    }

    public List<BasicController> getControllers() {
        return controllers;
    }

    public FTPClient getClient() {
        return client;
    }

    public List<Stage> getStages() {
        return stages;
    }
}
