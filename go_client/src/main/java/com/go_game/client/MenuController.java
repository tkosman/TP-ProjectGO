package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MenuController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private LineChart<?, ?> eloLineChart;

    @FXML
    private VBox leftPanelVBox;

    @FXML
    private Button logoutButton;

    @FXML
    private HBox logoutHBox;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private HBox menuHBox;

    @FXML
    private VBox middlePanelVBox;

    @FXML
    private HBox modeSelectHBox;

    @FXML
    private Label nameDisplayLabel;

    @FXML
    private ListView<?> replayListView;

    @FXML
    private VBox rightPanelVBox;

    @FXML
    private Button select13x13Button;

    @FXML
    private Button select19x19Button;

    @FXML
    private Button select9x9Button;

    @FXML
    void initialize() {
        assert eloLineChart != null : "fx:id=\"eloLineChart\" was not injected: check your FXML file 'menu.fxml'.";
        assert leftPanelVBox != null : "fx:id=\"leftPanelVBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert logoutButton != null : "fx:id=\"logoutButton\" was not injected: check your FXML file 'menu.fxml'.";
        assert logoutHBox != null : "fx:id=\"logoutHBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'menu.fxml'.";
        assert menuHBox != null : "fx:id=\"menuHBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert middlePanelVBox != null : "fx:id=\"middlePanelVBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert modeSelectHBox != null : "fx:id=\"modeSelectHBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert nameDisplayLabel != null : "fx:id=\"nameDisplayLabel\" was not injected: check your FXML file 'menu.fxml'.";
        assert replayListView != null : "fx:id=\"replayListView\" was not injected: check your FXML file 'menu.fxml'.";
        assert rightPanelVBox != null : "fx:id=\"rightPanelVBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert select13x13Button != null : "fx:id=\"select13x13Button\" was not injected: check your FXML file 'menu.fxml'.";
        assert select19x19Button != null : "fx:id=\"select19x19Button\" was not injected: check your FXML file 'menu.fxml'.";
        assert select9x9Button != null : "fx:id=\"select9x9Button\" was not injected: check your FXML file 'menu.fxml'.";

        select13x13Button.setOnAction(event -> startXxXGame(13));
        select19x19Button.setOnAction(event -> startXxXGame(19));
        select9x9Button.setOnAction(event -> startXxXGame(9));

    }


    @FXML
    void logOut() {
        // TODO: send logout information to server

        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    
    private void startXxXGame(int x) {
        // TODO: send x to server

        gameModeAlert(x);

    }

    private void gameModeAlert(int x) {
        Alert alert = new Alert(AlertType.NONE, "", ButtonType.CLOSE);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        alert.getDialogPane().setPrefSize(300, 300);

        VBox gameModeSelectVBox = new VBox(5);
        gameModeSelectVBox.setAlignment(Pos.CENTER);

        Image pvpIcon = new Image(App.class.getResource("pvp.png").toExternalForm());
        Image botIcon = new Image(App.class.getResource("bot.png").toExternalForm());

        double scaledWidth = 50;
        double scaledHeight = 50;

        ImageView pvpImageView = new ImageView(pvpIcon);
        pvpImageView.setFitWidth(scaledWidth);
        pvpImageView.setFitHeight(scaledHeight);

        ImageView botImageView = new ImageView(botIcon);
        botImageView.setFitWidth(scaledWidth);
        botImageView.setFitHeight(scaledHeight);

        Label titleLable = new Label("Select game mode");

        Button pvpButton = new Button("Play PvP", pvpImageView);
        Button botButton = new Button("Play Bot", botImageView);

        pvpButton.setMaxWidth(Double.MAX_VALUE);
        botButton.setMaxWidth(Double.MAX_VALUE);

        pvpButton.setOnAction(event -> {
            alert.setResult(ButtonType.OK);
            alert.close();
            
            try {
                App.setRoot("game");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        botButton.setOnAction(event -> {
            alert.setResult(ButtonType.OK);
            alert.close();

            try {
                App.setRoot("game");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        gameModeSelectVBox.getChildren().addAll(titleLable, pvpButton, botButton);
        VBox.setVgrow(pvpButton, Priority.ALWAYS);
        VBox.setVgrow(botButton, Priority.ALWAYS);

        alert.getDialogPane().setContent(gameModeSelectVBox);

        alert.showAndWait();
    }

    private void matchmakingAlert() {

    } 

}
