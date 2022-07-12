package fr.ufc.metaobs;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("view/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),960, 720);
        primaryStage.setTitle("MetaobsTool");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

}
