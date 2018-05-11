package net.metryumora.controllers;

public enum ApplicationStage {
    MAIN(0, "mainWindow", "JavaFTPClient"),
    CONNECT(1, "connect", "Connect to server");

    private class PathConstants {
        public static final String PATH_PREFIX = "/view/";
        public static final String FILE_SUFFIX = ".fxml";
    }

    private final int id;
    private final String fxmlFilepath;
    private final String windowHeader;

    ApplicationStage(int id, String fxmlFilepath, String windowHeader) {
        this.id = id;
        this.fxmlFilepath = fxmlFilepath;
        this.windowHeader = windowHeader;
    }

    public int getId() {
        return id;
    }

    public String getFxmlFilepath() {
        return PathConstants.PATH_PREFIX + fxmlFilepath + PathConstants.FILE_SUFFIX;
    }

    public String getWindowTitle() {
        return windowHeader;
    }
}

