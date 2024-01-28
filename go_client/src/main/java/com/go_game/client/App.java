package com.go_game.client;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static final Duration FADE_DURATION = Duration.millis(700);

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 900, 600);

        scene.getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        scene.setFill(Paint.valueOf("#2E2E2E"));

        stage.setScene(scene);

        stage.setMinWidth(900);
        stage.setMinHeight(600);

        Image icon = new Image(App.class.getResource("logo.png").toExternalForm());
        stage.getIcons().add(icon);
        stage.setTitle("go");

        stage.show();

        setRoot("login", null, null);
    }

    public static void setRoot(String fxml, Object currentController, Object nextController) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    
        // Set the controller for the new FXML
        fxmlLoader.setController(nextController);
    
        Parent newRoot = fxmlLoader.load();
    
        // Set the background color of the new scene to black
        newRoot.setStyle("fade-background");
    
        // Overlay the new scene on top of the old scene
        newRoot.setOpacity(0.0);
        scene.setRoot(newRoot);
    
        // Create a fade transition for fade-in
        FadeTransition fadeTransition = new FadeTransition(FADE_DURATION, newRoot);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
