package net.metryumora;

import javafx.application.Application;
import javafx.stage.Stage;
import net.metryumora.controllers.Dispatcher;

public class ApplicationLauncher extends Application {

    private static Dispatcher dispatcher;

    @Override
    public void start(Stage primaryStage) {
        dispatcher = Dispatcher.getInstance(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }
}


