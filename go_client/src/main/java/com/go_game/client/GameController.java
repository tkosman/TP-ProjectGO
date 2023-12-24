package com.go_game.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class GameController {

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
    private AnchorPane mainAnchorPane;

    @FXML
    private HBox mainHBox;

    @FXML
    private void initialize() {
        int gridSize = 11;
        int circleRadius = 15;
        int spacing = 5;

        boardHBox.setSpacing(spacing);
        boardHBox.setAlignment(javafx.geometry.Pos.CENTER);

        for (int i = 0; i < gridSize; i++) {
            VBox vBox = new VBox();
            vBox.setAlignment(javafx.geometry.Pos.CENTER);
            vBox.setSpacing(spacing);
            boardHBox.getChildren().add(vBox);
        }

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Circle circle = new Circle(circleRadius);
                circle.getStyleClass().add("game-circle");
                circle.setOnMouseClicked(createCircleClickHandler(circle));
                circle.hoverProperty().addListener(createHoverChangeListener(circle));
                circle.setStroke(Color.TRANSPARENT);
                circle.setOpacity(1.0);
                circle.setStrokeWidth(2);
                ((VBox) boardHBox.getChildren().get(i)).getChildren().add(circle);
            }
        }

        Platform.runLater(() -> {
            AnchorPane.setTopAnchor(boardHBox, (circleRadius + spacing) * (gridSize + 1.0));
            AnchorPane.setLeftAnchor(boardHBox, (circleRadius + spacing) * (gridSize - 5.0));
        });
    }

    private EventHandler<MouseEvent> createCircleClickHandler(Circle circle) {
        return event -> {
            circle.setFill(circle.getFill() == Color.BLACK ? Color.WHITE : Color.BLACK);
            event.consume();
        };
    }

    private ChangeListener<Boolean> createHoverChangeListener(Circle circle) {
        return (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                circle.setStroke(Color.web("#af00ff")); // #afafaf
                circle.setOpacity(0.8);
            } else {
                circle.setStroke(Color.TRANSPARENT);
                circle.setOpacity(1.0);
            }
        };
    }
}
