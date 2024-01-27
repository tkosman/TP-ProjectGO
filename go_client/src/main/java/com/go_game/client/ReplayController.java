package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.go_game.client.game_model.ColorEnum;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.StageStyle;

public class ReplayController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane boardAnchorPane;

    @FXML
    private HBox boardHBox;

    @FXML
    private VBox controllsVBox;

    @FXML
    private Button exitButton;

    @FXML
    private HBox mainHBox;

    @FXML
    private ListView<?> moveBoardListView;

    @FXML
    private Label oponentNameLabel;

    @FXML
    private Label oponentScoreLabel;

    @FXML
    private Label playerNameLabel;

    @FXML
    private Label playerScoreLabel;

    @FXML
    private Button selectReplayButton;


    @FXML
    void initialize() {
        assert boardAnchorPane != null : "fx:id=\"boardAnchorPane\" was not injected: check your FXML file 'replay.fxml'.";
        assert boardHBox != null : "fx:id=\"boardHBox\" was not injected: check your FXML file 'replay.fxml'.";
        assert controllsVBox != null : "fx:id=\"controllsVBox\" was not injected: check your FXML file 'replay.fxml'.";
        assert exitButton != null : "fx:id=\"exitButton\" was not injected: check your FXML file 'replay.fxml'.";
        assert mainHBox != null : "fx:id=\"mainHBox\" was not injected: check your FXML file 'replay.fxml'.";
        assert moveBoardListView != null : "fx:id=\"moveBoardListView\" was not injected: check your FXML file 'replay.fxml'.";
        assert oponentNameLabel != null : "fx:id=\"oponentNameLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert oponentScoreLabel != null : "fx:id=\"oponentScoreLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert playerNameLabel != null : "fx:id=\"playerNameLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert playerScoreLabel != null : "fx:id=\"playerScoreLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert selectReplayButton != null : "fx:id=\"selectReplayButton\" was not injected: check your FXML file 'replay.fxml'.";


        //! for testing
        List<List<String>> sampleList = Arrays.asList(
            Arrays.asList("1", "2024-01-20 19-23"),
            Arrays.asList("2", "2024-01-21 13-41"),
            Arrays.asList("3", "2024-01-21 21-37")
        );

        // TODO: get replays from server

        replayAlert(sampleList);

        selectReplayButton.setOnAction(event -> replayAlert(sampleList));
    }

    private void initializeBoard(int boardSize) {
        final int gridSize = boardSize;
        final int circleRadius = 15;
        final int spacing = 5;

        boardHBox.setSpacing(spacing);
        boardHBox.setAlignment(Pos.CENTER);

        for (int i = 0; i <= gridSize; i++) {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(spacing);
            boardHBox.getChildren().add(vBox);
        }

        ((VBox) boardHBox.getChildren().get(0)).getChildren().add(new Circle(circleRadius, Color.TRANSPARENT));
        ((Shape) ((VBox) boardHBox.getChildren().get(0)).getChildren().get(0)).setStrokeWidth(2);
        

        for (int i = 1; i <= gridSize; i++) {
            Circle circleX = new Circle(circleRadius);
            circleX.setStroke(Color.TRANSPARENT);
            circleX.setOpacity(0.0);
            circleX.setStrokeWidth(2);
        
            Label labelX = new Label(String.format("%d", i));
            labelX.setStyle("-fx-font-weight: bold;");
            StackPane coordinateX = new StackPane(circleX, labelX);
            coordinateX.setAlignment(Pos.CENTER);
            ((VBox) boardHBox.getChildren().get(i)).getChildren().add(coordinateX);
        
            Circle circleY = new Circle(circleRadius);
            circleY.setStroke(Color.TRANSPARENT);
            circleY.setOpacity(0.0);
            circleY.setStrokeWidth(2);
        
            Label labelY = new Label(String.valueOf((char) ('a' + i - 1)));
            labelY.setStyle("-fx-font-weight: bold;");
            StackPane coordinateY = new StackPane(circleY, labelY);
            coordinateY.setAlignment(Pos.CENTER);
            ((VBox) boardHBox.getChildren().get(0)).getChildren().add(coordinateY);
        }

        for (int i = 1; i <= gridSize; i++) {
            for (int j = 1; j <= gridSize; j++) {
                Circle circle = new Circle(circleRadius);
                circle.getStyleClass().add("game-circle");
                // circle.hoverProperty().addListener(createHoverChangeListener(circle, j));
                circle.setStroke(Color.TRANSPARENT);
                circle.setOpacity(1.0);
                circle.setStrokeWidth(2);
                ((VBox) boardHBox.getChildren().get(i)).getChildren().add(circle);
            }
        }
    }

    private void replayAlert(List<List<String>> replayList) {
        Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.CLOSE);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        alert.getDialogPane().setPrefSize(300, 300);

        ListView<String> listView = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();

        for (List<String> entry : replayList) {
            items.add(String.join("  |  ", entry));
        }

        listView.setItems(items);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // TODO: get replay string from server

                boardHBox.getChildren().clear();
                // TODO: pass board size to initializeBoard() 
                initializeBoard(9);
                alert.close();
            }
        });

        VBox vBox = new VBox(listView);
        alert.getDialogPane().setContent(vBox);

        alert.setOnCloseRequest(event -> {
            if (boardHBox.getChildren().isEmpty()) {
                exit();
            }
        });

        Platform.runLater(() -> {
            alert.showAndWait();
        });
    }

    // TODO: change it to outline last placed stone
    // private ChangeListener<Boolean> createHoverChangeListener(Circle circle, int y) {
    //     return (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
    //         if (newValue) {
    //             circle.setStroke(Color.web("#af00ff")); // #afafaf
    //             circle.setOpacity(0.8);

    //             StackPane coordX = (StackPane) circle.getParent().getChildrenUnmodifiable().get(0);
    //             ((Label) coordX.getChildren().get(1)).setTextFill(Color.web("#af00ff"));

    //             StackPane coordY = (StackPane) ((VBox) boardHBox.getChildren().get(0)).getChildren().get(y);
    //             ((Label) coordY.getChildren().get(1)).setTextFill(Color.web("#af00ff"));
    //         }
    //         else {
    //             circle.setStroke(Color.TRANSPARENT);
    //             circle.setOpacity(1.0);

    //             StackPane coordX = (StackPane) circle.getParent().getChildrenUnmodifiable().get(0);
    //             ((Label) coordX.getChildren().get(1)).setTextFill(Color.web("#ffffff"));

    //             StackPane coordY = (StackPane) ((VBox) boardHBox.getChildren().get(0)).getChildren().get(y);
    //             ((Label) coordY.getChildren().get(1)).setTextFill(Color.web("#ffffff"));
    //         }
    //     };
    // }

    @FXML
    void exit() {
        try {
            App.setRoot("menu", this, new MenuController());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
