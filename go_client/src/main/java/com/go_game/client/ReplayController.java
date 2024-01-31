package com.go_game.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import com.go_game.client.connection.Client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.StageStyle;
import shared.enums.GameMode;
import shared.enums.Stone;
import shared.messages.BoardStateMsg;
import shared.messages.ClientInfoMsg;
import shared.messages.IndexSetMsg;
import shared.messages.ReplayFetchMsg;

public class ReplayController {
    private static final int PORT = 4444;
    private static final String HOST = "localhost";

    private Client client;
    private List<BoardStateMsg> moveList;
    private int boardSize = -1;
    private int movePointer;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private HBox boardHBox;

    @FXML
    private VBox controllsVBox;

    @FXML
    private Button exitButton;

    @FXML
    private HBox mainHBox;

    @FXML
    private Button moveBackButton;

    @FXML
    private ListView<?> moveBoardListView;

    @FXML
    private Button moveForwardButton;

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
        assert boardHBox != null : "fx:id=\"boardHBox\" was not injected: check your FXML file 'replay.fxml'.";
        assert controllsVBox != null : "fx:id=\"controllsVBox\" was not injected: check your FXML file 'replay.fxml'.";
        assert exitButton != null : "fx:id=\"exitButton\" was not injected: check your FXML file 'replay.fxml'.";
        assert mainHBox != null : "fx:id=\"mainHBox\" was not injected: check your FXML file 'replay.fxml'.";
        assert moveBackButton != null : "fx:id=\"moveBackButton\" was not injected: check your FXML file 'replay.fxml'.";
        assert moveBoardListView != null : "fx:id=\"moveBoardListView\" was not injected: check your FXML file 'replay.fxml'.";
        assert moveForwardButton != null : "fx:id=\"moveForwardButton\" was not injected: check your FXML file 'replay.fxml'.";
        assert oponentNameLabel != null : "fx:id=\"oponentNameLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert oponentScoreLabel != null : "fx:id=\"oponentScoreLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert playerNameLabel != null : "fx:id=\"playerNameLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert playerScoreLabel != null : "fx:id=\"playerScoreLabel\" was not injected: check your FXML file 'replay.fxml'.";
        assert selectReplayButton != null : "fx:id=\"selectReplayButton\" was not injected: check your FXML file 'replay.fxml'.";


        selectReplay();
    }


    private void initializeBoard(int boardSize) {
        final int gridSize = boardSize;
        final int circleRadius = 15;
        final int spacing = 5;

        boardHBox.getChildren().clear();

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

    private void replayAlert(Map<Integer, Date> replayList) {
        ButtonType cancelButton = new ButtonType("cancel", ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.NONE, "", cancelButton);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        alert.getDialogPane().setPrefSize(300, 300);

        Button cancelBtn = (Button) alert.getDialogPane().lookupButton(cancelButton);
        cancelBtn.addEventFilter(ActionEvent.ACTION, event -> {
            if (boardSize == -1) {
                exit();
            }
        });
    
        ListView<String> listView = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();
    
        // Use Java Streams to iterate over the entry set and create a formatted string
        replayList.entrySet().stream().map(entry -> entry.getKey() + "  |  " + entry.getValue()).forEach(items::add);
    
        listView.setItems(items);
    
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // TODO: get replay string from server
                try {
                    System.out.println(newValue.split("\\s*\\|\\s*")[0]);

                    client.sendMessage(new ReplayFetchMsg(Integer.parseInt(newValue.split("\\s*\\|\\s*")[0])));

                    ReplayFetchMsg moveListMessage = (ReplayFetchMsg) client.receiveMessage();

                    moveList = moveListMessage.getReplaysList();
                    boardSize = moveList.get(0).getBoardState().length;

                } catch (NumberFormatException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    exit();
                }
    
                boardHBox.getChildren().clear();
                // TODO: pass board size to initializeBoard() 
                initializeBoard(boardSize);
                alert.close();
            }
        });
    
        VBox vBox = new VBox(listView);
        alert.getDialogPane().setContent(vBox);
    
        Platform.runLater(() -> alert.showAndWait());
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

    protected void setBoard(Stone[][] state) {
        Platform.runLater(() -> {
            for (int i = 1; i <= boardSize; i++) {
                for (int j = 1; j <= boardSize; j++) {
                    Color color;

                    if (state[i - 1][j - 1] == Stone.BLACK) color = Color.BLACK;
                    else if (state[i - 1][j - 1] == Stone.WHITE) color = Color.WHITE;
                    else color = Color.web("#3E3E3E");

                    ((Circle) ((VBox) boardHBox.getChildren().get(i)).getChildren().get(j)).setFill(color);
                }
            }
        });
    }

    @FXML
    void selectReplay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Client(new Socket(HOST, PORT));
                    client.receiveMessage();
    
                    client.sendMessage(new ClientInfoMsg(GameMode.REPLAY));
    
                    ReplayFetchMsg replayList = (ReplayFetchMsg) client.receiveMessage();
    
                    Platform.runLater(() -> {
                        replayAlert(replayList.getGameIDsAndDates());
                        
                        initializeBoard(boardSize);
                    });
    
                    setBoard(moveList.get(0).getBoardState());

                    movePointer = 0;

                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                    exit();
                }
            }
        }).start();
    }

    @FXML
    void moveBack() {
        if (movePointer > 0) {
            movePointer--;
        }

        setBoard(moveList.get(movePointer).getBoardState());
    }

    @FXML
    void moveForward() {
        if (movePointer < moveList.size()) {
            movePointer++;
        }

        setBoard(moveList.get(movePointer).getBoardState());
    }

    @FXML
    void exit() {
        try {
            try {
                client.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            App.setRoot("menu", this, new MenuController());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
