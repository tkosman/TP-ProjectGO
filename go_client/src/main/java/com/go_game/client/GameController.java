package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.go_game.client.connection.Client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import shared.enums.BoardSize;
import shared.enums.MessageType;
import shared.enums.PlayerColors;
import shared.enums.Stone;
import shared.messages.AbstractMessage;
import shared.messages.BoardStateMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;

public class GameController {
    private static final int CIRCLE_RADIUS = 15;
    private static final int SPACING = 5;
    private int GRID_SIZE;

    private Client client;

    private BoardStone[][] stoneGrid;


    private PlayerColors currentPlayerColor = PlayerColors.BLACK;
    
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
    private Label oponentScoreLabel;

    @FXML
    private Button passButton;

    @FXML
    private Label playerScoreLabel;

    @FXML
    private Button resignButton;


    @FXML
    void initialize() {
        assert boardAnchorPane != null : "fx:id=\"boardAnchorPane\" was not injected: check your FXML file 'game.fxml'.";
        assert boardHBox != null : "fx:id=\"boardHBox\" was not injected: check your FXML file 'game.fxml'.";
        assert controllsVBox != null : "fx:id=\"controllsVBox\" was not injected: check your FXML file 'game.fxml'.";
        assert exitButton != null : "fx:id=\"exitButton\" was not injected: check your FXML file 'game.fxml'.";
        assert mainHBox != null : "fx:id=\"mainHBox\" was not injected: check your FXML file 'game.fxml'.";
        assert moveBoardListView != null : "fx:id=\"moveBoardListView\" was not injected: check your FXML file 'game.fxml'.";
        assert oponentScoreLabel != null : "fx:id=\"oponentScoreLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert passButton != null : "fx:id=\"passButton\" was not injected: check your FXML file 'game.fxml'.";
        assert playerScoreLabel != null : "fx:id=\"playerScoreLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert resignButton != null : "fx:id=\"resignButton\" was not injected: check your FXML file 'game.fxml'.";


        // TODO: ustawić elementy interfaceu
        GRID_SIZE = this.client.getGame().getBS().getIntSize();

        stoneGrid = new BoardStone[GRID_SIZE + 1][GRID_SIZE + 1];

        boardHBox.setSpacing(SPACING);
        boardHBox.setAlignment(Pos.CENTER);

        for (int i = 0; i <= GRID_SIZE; i++) {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(SPACING);
            boardHBox.getChildren().add(vBox);
        }

        ((VBox) boardHBox.getChildren().get(0)).getChildren().add(new Circle(CIRCLE_RADIUS, Color.TRANSPARENT));
        ((Shape) ((VBox) boardHBox.getChildren().get(0)).getChildren().get(0)).setStrokeWidth(2);
        

        for (int i = 1; i <= GRID_SIZE; i++) {
            Circle circleX = new Circle(CIRCLE_RADIUS);
            circleX.setStroke(Color.TRANSPARENT);
            circleX.setOpacity(0.0);
            circleX.setStrokeWidth(2);
        
            Label labelX = new Label(String.format("%d", i));
            labelX.setStyle("-fx-font-weight: bold;");
            StackPane coordinateX = new StackPane(circleX, labelX);
            coordinateX.setAlignment(Pos.CENTER);
            ((VBox) boardHBox.getChildren().get(i)).getChildren().add(coordinateX);
        
            Circle circleY = new Circle(CIRCLE_RADIUS);
            circleY.setStroke(Color.TRANSPARENT);
            circleY.setOpacity(0.0);
            circleY.setStrokeWidth(2);
        
            Label labelY = new Label(String.valueOf((char) ('a' + i - 1)));
            labelY.setStyle("-fx-font-weight: bold;");
            StackPane coordinateY = new StackPane(circleY, labelY);
            coordinateY.setAlignment(Pos.CENTER);
            ((VBox) boardHBox.getChildren().get(0)).getChildren().add(coordinateY);
        }

        for (int i = 1; i <= GRID_SIZE; i++) {
            for (int j = 1; j <= GRID_SIZE; j++) {
                BoardStone circle = new BoardStone(CIRCLE_RADIUS, i, j);

                circle.getStyleClass().add("game-circle");
                circle.setOnMouseClicked(createCircleClickHandler(circle));
                circle.hoverProperty().addListener(createHoverChangeListener(circle, j));
                circle.setStroke(Color.TRANSPARENT);
                circle.setOpacity(1.0);
                circle.setStrokeWidth(2);
                ((VBox) boardHBox.getChildren().get(i)).getChildren().add(circle);
            }
        }

        //! 1 out white
        if (this.client.getGame().getPlayerColor() == PlayerColors.WHITE) {
            try {
				this.client.sendMessage(new OkMsg());

                // BoardStateMsg firstBS = (BoardStateMsg) this.client.receiveMessage();
                // client.getGame().setState(firstBS.getBoardState());
                // setBoard(firstBS.getBoardState());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        Thread fred = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (currentPlayerColor == getClient().getGame().getPlayerColor()) {
                        //TODO: wait for player to click
                        //TODO: receive board state
                    }
                    else
                    {
                        //TODO: send ok msg imediatelly
                        //TODO: receive board state
                    }
                }
            }
            
        });
        fred.start();
    }

    

    public Client getClient() {
        return client;
    }
    
    protected void setBoard(Stone[][] state) {
        Platform.runLater(() -> {
            for (int i = 1; i <= GRID_SIZE; i++) {
                for (int j = 1; j <= GRID_SIZE; j++) {
                    Color color;

                    if (this.client.getGame().getState()[i - 1][j - 1] == Stone.BLACK) color = Color.BLACK;
                    else if (this.client.getGame().getState()[i - 1][j - 1] == Stone.BLACK) color = Color.WHITE;
                    else color = Color.web("#3E3E3E");

                    ((Circle) ((VBox) boardHBox.getChildren().get(i)).getChildren().get(j)).setFill(color);
                }
            }
        });
    }


    public GameController(Client client) {
        this.client = client;
    }

    // changing stone color
    private EventHandler<MouseEvent> createCircleClickHandler(BoardStone circle) {
        return event -> {
            if (circle.getStoneColor().equals(Stone.EMPTY)) {
                try {
                    if (currentPlayerColor == this.client.getGame().getPlayerColor()) {
                        System.out.println("send move message");
                        this.client.sendMessage(new MoveMsg(circle.getX(), circle.getY()));

                        System.out.println("recive response with board state");
                        AbstractMessage responseForMove = (AbstractMessage) this.client.receiveMessage();
                        if (responseForMove.getType() == MessageType.MOVE_NOT_VALID) {
                            System.out.println(((MoveNotValidMsg) responseForMove).getDescription());
                        } 
                        else if (responseForMove.getType() == MessageType.BOARD_STATE) {
                            Platform.runLater(() -> {
                                circle.setFill(this.client.getGame().getPlayerColor() == PlayerColors.BLACK ? Color.BLACK : Color.WHITE);
                            });
                            
                            currentPlayerColor = currentPlayerColor.getOpposite();

                            client.getGame().setState(((BoardStateMsg) responseForMove).getBoardState());
                            setBoard(client.getGame().getState());
                            
                            System.out.println("send ok message");
                            client.sendMessage(new OkMsg());

                        }
                        // TODO: game over
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            event.consume();
        };
    }

    // hover effect
    private ChangeListener<Boolean> createHoverChangeListener(Circle circle, int y) {
        return (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                circle.setStroke(Color.web("#af00ff")); // #afafaf
                circle.setOpacity(0.8);

                StackPane coordX = (StackPane) circle.getParent().getChildrenUnmodifiable().get(0);
                ((Label) coordX.getChildren().get(1)).setTextFill(Color.web("#af00ff"));

                StackPane coordY = (StackPane) ((VBox) boardHBox.getChildren().get(0)).getChildren().get(y);
                ((Label) coordY.getChildren().get(1)).setTextFill(Color.web("#af00ff"));
            }
            else {
                circle.setStroke(Color.TRANSPARENT);
                circle.setOpacity(1.0);

                StackPane coordX = (StackPane) circle.getParent().getChildrenUnmodifiable().get(0);
                ((Label) coordX.getChildren().get(1)).setTextFill(Color.web("#ffffff"));

                StackPane coordY = (StackPane) ((VBox) boardHBox.getChildren().get(0)).getChildren().get(y);
                ((Label) coordY.getChildren().get(1)).setTextFill(Color.web("#ffffff"));
            }
        };
    }

    @FXML
    void exit() {
        // TODO: loose game

        try {
            App.setRoot("menu", this, new MenuController());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void resign() {
        // TODO: ofer resignation to oponent

        resignAlert();
    }

    private void resignAlert() {
        Alert alert = new Alert(AlertType.NONE, "", ButtonType.CLOSE);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        alert.getDialogPane().setPrefSize(300, 300);

        Label titleLabel = new Label("Waiting for oponent to accept your surredner");

        alert.getDialogPane().setContent(titleLabel);

        alert.setOnCloseRequest(event -> {
            // TODO: send cancelation of resignation to server
        });

        alert.showAndWait();
    }

    @FXML
    void pass() {
        if (currentPlayerColor.equals(this.client.getGame().getPlayerColor())) {
            try {
                this.client.sendMessage(new MoveMsg(true));
                switchColors();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void switchColors() {
        currentPlayerColor = currentPlayerColor.getOpposite();
    }
}
