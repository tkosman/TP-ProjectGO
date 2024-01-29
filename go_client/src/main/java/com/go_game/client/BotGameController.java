package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.go_game.client.connection.Client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import shared.enums.MessageType;
import shared.enums.PlayerColors;
import shared.enums.Stone;
import shared.messages.AbstractMessage;
import shared.messages.BoardStateMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;

public class BotGameController {
    private static final int CIRCLE_RADIUS = 15;
    private static final int SPACING = 5;
    private int GRID_SIZE;

    private Client client;

    private volatile BoardStone[][] stoneGrid;

    private volatile boolean stoneClicked = false;
    // private Point moveCord;
    private volatile int moveCordX;
    private volatile int moveCordY;


    private PlayerColors currentPlayerColor = PlayerColors.BLACK;
    
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
    private ListView<?> moveBoardListView;

    @FXML
    private Label oponentColorLabel;

    @FXML
    private Label oponentNameLabel;

    @FXML
    private Label oponentScoreLabel;

    @FXML
    private Button passButton;

    @FXML
    private Label playerColorLabel;

    @FXML
    private Label playerNameLabel;

    @FXML
    private Label playerScoreLabel;

    @FXML
    private Button resignButton;

    @FXML
    private Label statusLabel;


    @FXML
    void initialize() {
        assert boardHBox != null : "fx:id=\"boardHBox\" was not injected: check your FXML file 'game.fxml'.";
        assert controllsVBox != null : "fx:id=\"controllsVBox\" was not injected: check your FXML file 'game.fxml'.";
        assert exitButton != null : "fx:id=\"exitButton\" was not injected: check your FXML file 'game.fxml'.";
        assert mainHBox != null : "fx:id=\"mainHBox\" was not injected: check your FXML file 'game.fxml'.";
        assert moveBoardListView != null : "fx:id=\"moveBoardListView\" was not injected: check your FXML file 'game.fxml'.";
        assert oponentColorLabel != null : "fx:id=\"oponentColorLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert oponentNameLabel != null : "fx:id=\"oponentNameLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert oponentScoreLabel != null : "fx:id=\"oponentScoreLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert passButton != null : "fx:id=\"passButton\" was not injected: check your FXML file 'game.fxml'.";
        assert playerColorLabel != null : "fx:id=\"playerColorLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert playerNameLabel != null : "fx:id=\"playerNameLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert playerScoreLabel != null : "fx:id=\"playerScoreLabel\" was not injected: check your FXML file 'game.fxml'.";
        assert resignButton != null : "fx:id=\"resignButton\" was not injected: check your FXML file 'game.fxml'.";
        assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'game.fxml'.";


        playerColorLabel.setText(client.getGame().getPlayerColor().toString());
        playerScoreLabel.setText("0");
        oponentColorLabel.setText(client.getGame().getPlayerColor().getOpposite().toString());
        oponentScoreLabel.setText("0");

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

        Thread fred = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true)
                    {
                        // System.out.println("\n\nTURN: " + whoseTurn);
                        //! EACH WILL BE SENT TO SERVER
                        if (isMyTurn())
                        {
                            //? This player turn
                            //! 1 OUT +++++++++ -> It's my turn and I'm sending move to server
                            // System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING MOVE TO SERVER AS PLAYER " + playerNo + " TURN: " + whoseTurn);
                            sendMoveToServer();

                            //! 2 IN ########## -> It's my turn and I'm waiting for server to send me info back
                            receiveInfoFromServer();
                        }
                        else
                        {
                            // //! 1 OUT +++++++++ -> It's NOT my turn so I'm just sending OK to server
                            // System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING OK TO SERVER AS PLAYER " + playerNo + " TURN: " + whoseTurn);
                            client.sendMessage(new OkMsg());

                            //! 2 IN ########## -> It's NOT my turn so I'm waiting for server to send me info back
                            receiveInfoFromServer();
                        }

                    }
                }
                catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
            }
        });
        fred.start();
    }

    private void receiveInfoFromServer() throws IOException, ClassNotFoundException 
    {
        //TODO: !DOBREK! here you should receive the info from the server and handle it

        AbstractMessage message = (AbstractMessage) client.receiveMessage();
        System.out.println("MESSAGE " + message);

        if (message.getType() == MessageType.BOARD_STATE)
        {
            //? Player did a valid move OR it was not his trun
            //? server sent back the updated board
            // System.out.println(new Timestamp(System.currentTimeMillis()) + " RECEIVE BOARD FROM SERVER " + playerNo  + " TURN: " + whoseTurn);
            BoardStateMsg boardStateMsg = (BoardStateMsg) message;
            client.getGame().setState(boardStateMsg.getBoardState());
            setBoard(client.getGame().getState());
            switchTurns();

            for (int i = 0; i <= 1; i++) {
                client.getGame().setScore(boardStateMsg.getScore());
                setScore();
            }

            if (isMyTurn()){
                setStatus("your turn", Color.BLUEVIOLET);
            }
            else {
                setStatus("oponent's turn", Color.BLUEVIOLET);
            }
        }
        else if (message.getType() == MessageType.MOVE_NOT_VALID)
        {
            //? Player did an invalid move
            //? we need to resent the move
            MoveNotValidMsg moveNotValidMsg = (MoveNotValidMsg) message;
            PlayerColors playerWhoDidNotValidMove = moveNotValidMsg.playerWhoDidNotValidMove();
            String description = moveNotValidMsg.getDescription();
            System.out.println(description);
            if (currentPlayerColor == playerWhoDidNotValidMove){
                setStatus("invalid move", Color.RED);
            }
        }
        else if (message.getType() == MessageType.PLAYER_PASSED)
        {
            //? Player passed
            //? we need to print who passed
            PlayerPassedMsg playerPassedMsg = (PlayerPassedMsg) message;
            PlayerColors playerColor = playerPassedMsg.getPlayerColor();
            switchTurns();
            setStatus("oponent's turn", Color.BLUEVIOLET);
        }

        else if (message.getType() == MessageType.GAME_OVER)
        {
            //? Game over
            //? we need to print the winner and the reason
            GameOverMsg gameOverMsg = (GameOverMsg) message;
            PlayerColors winner = gameOverMsg.getWinner();
            String description = gameOverMsg.getdescription();
            if (winner == client.getGame().getPlayerColor()) {
                setStatus("WINNER", Color.GREEN);
            }
            else {
                setStatus("LOOSER", Color.RED);
            }
        }
        else
        {
            //? Something went wrong, simply won't happen
            System.err.println("Sorry sth went wrong :(");
            setStatus("something went wrong", Color.RED);
        }
    }

    private void setStatus(String text, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            statusLabel.setTextFill(color);
        });
    }

    private void setScore() {
        //! 0 - Black, 1 - White
        Platform.runLater(() -> {
            if (client.getGame().getPlayerColor() == PlayerColors.BLACK) {
                playerScoreLabel.setText(String.valueOf(client.getGame().getScore(0)));
                oponentScoreLabel.setText(String.valueOf(client.getGame().getScore(1)));
            }
            else {  
                playerScoreLabel.setText(String.valueOf(client.getGame().getScore(1)));
                oponentScoreLabel.setText(String.valueOf(client.getGame().getScore(0)));
            }
        });
    }

    private void switchTurns() {
        currentPlayerColor = currentPlayerColor.getOpposite();
    }

    protected void sendMoveToServer() {
        while (true) {
            if (stoneClicked) {
                System.out.println("dupa");

                stoneClicked = false;
        
                try {
                    client.sendMessage(new MoveMsg(moveCordX, moveCordY));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private boolean isMyTurn() {
        if (currentPlayerColor == this.client.getGame().getPlayerColor()){
            return true;
        } 
        return false;
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
                    else if (this.client.getGame().getState()[i - 1][j - 1] == Stone.WHITE) color = Color.WHITE;
                    else color = Color.web("#3E3E3E");

                    ((Circle) ((VBox) boardHBox.getChildren().get(i)).getChildren().get(j)).setFill(color);
                }
            }
        });
    }


    public BotGameController(Client client) {
        this.client = client;
    }

    // changing stone color
    private EventHandler<MouseEvent> createCircleClickHandler(BoardStone circle) {
        return event -> {
            if (circle.getStoneColor().equals(Stone.EMPTY)) {

                moveCordX = circle.getX() - 1;
                moveCordY = circle.getY() - 1;
                System.out.println("Clicked: " + moveCordX + ", " + moveCordY);

                this.stoneClicked = true;
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
            client.closeConnection();

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
