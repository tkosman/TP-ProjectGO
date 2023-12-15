package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label nameLabel;

    @FXML
    private Button secondaryButton;


    @FXML
    void initialize() {
        assert nameLabel != null : "fx:id=\"name\" was not injected: check your FXML file 'game.fxml'.";
        assert secondaryButton != null : "fx:id=\"secondaryButton\" was not injected: check your FXML file 'game.fxml'.";

    }

    
    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("login");
    }
}
