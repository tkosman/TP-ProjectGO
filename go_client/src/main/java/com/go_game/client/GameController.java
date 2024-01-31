package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import com.go_game.client.connection.Client;
import com.go_game.client.connection.Game;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import shared.enums.AgreementState;
import shared.enums.GameMode;
import shared.enums.MessageType;
import shared.enums.PlayerColors;
import shared.enums.Stone;
import shared.enums.UnusualMove;
import shared.messages.AbstractMessage;
import shared.messages.BoardStateMsg;
import shared.messages.ClientInfoMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.IndexSetMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;
import shared.messages.PlayerResignedMsg;
import shared.messages.ResultsNegotiationMsg;

public class GameController {
    private static final int CIRCLE_RADIUS = 15;
    private static final int SPACING = 5;
    private int GRID_SIZE;

    private GameMode gameMode;
    private Client client;

    // private volatile BoardStone[][] stoneGrid;

    private volatile boolean stoneClicked = false;
    private volatile boolean passed = false;
    private volatile boolean isNegotiating = false;
    private volatile boolean resigned = false;

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

        if (isMyTurn()) {
            setStatus("your turn", Color.BLUEVIOLET);
        }
        else {
            setStatus("oponent's turn", Color.BLUEVIOLET);
        } 

        GRID_SIZE = this.client.getGame().getBS().getIntSize();

        // stoneGrid = new BoardStone[GRID_SIZE + 1][GRID_SIZE + 1];

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
                            System.out.println( " SENDING MOVE TO SERVER AS PLAYER " + " TURN: " + currentPlayerColor);
                            sendMoveToServer();

                            //! 2 IN ########## -> It's my turn and I'm waiting for server to send me info back
                            System.out.println("RECEIVED MOVE FROM SERVER");
                            receiveInfoFromServer();
                        }
                        else if (!isNegotiating)
                        {
                            // //! 1 OUT +++++++++ -> It's NOT my turn so I'm just sending OK to server
                            // System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING OK TO SERVER AS PLAYER " + playerNo + " TURN: " + whoseTurn);
                            client.sendMessage(new OkMsg());

                            //! 2 IN ########## -> It's NOT my turn so I'm waiting for server to send me info back
                            receiveInfoFromServer();
                        }
                        // else if (!resigned) {

                        // }
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
            System.out.println(" RECEIVE BOARD FROM SERVER "  + " TURN: " + currentPlayerColor);
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
            //TODO: status
        }
        else if (message.getType() == MessageType.PLAYER_RESIGNED)
        {
            //? Game over
            //? we need to print the winner and the reason
            resigned = true;

            PlayerResignedMsg playerResigned = (PlayerResignedMsg) message;
            PlayerColors resignee = playerResigned.playerWhoResigned();
            if (resignee == client.getGame().getPlayerColor()) {
                setStatus("LOOSER", Color.RED);
            }
            else {
                setStatus("WINNER", Color.GREEN);
            }
            //TODO: close socket
        }
        else if (message.getType() == MessageType.STH_WENT_WRONG)
        {
            //TODO: handle oponnent disconnecting
            //? Something went wrong, simply won't happen
            System.out.println("Sorry sth went wrong :(");
            setStatus("something went wrong", Color.RED);
        }
        else if (message.getType() == MessageType.RESULTS_NEGOTIATION && !resigned)
        {
            //TODO: message contains (description, territoryScore, capturedStones) int[2]\
            ResultsNegotiationMsg resultsNegotiation = (ResultsNegotiationMsg) message;

            isNegotiating = true;
            
            Platform.runLater(() -> {
                showResultAlert(resultsNegotiation);
            });
            
        } 
        else
        {
            System.err.println("message not found");
        }

    }

    private void showResultAlert(ResultsNegotiationMsg message) {
        // Create an undecorated alert
        Alert alert = new Alert(AlertType.NONE);
        alert.initStyle(StageStyle.UNDECORATED);
        
        // Apply a dark theme stylesheet
        alert.getDialogPane().getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        alert.getDialogPane().setPrefSize(300, 300);

        // Display negotiation results information
        Label titleLabel = new Label(message.getDescription());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        int oponentsScore = message.getTerritoryScore()[client.getGame().getPlayerColor().getOpposite().toInt()];
        int playerScore = message.getTerritoryScore()[client.getGame().getPlayerColor().toInt()];

        Label opponentScoreLabel = new Label("Opponent's score: " + oponentsScore);
        Label playerScoreLabel = new Label("Your score: " + playerScore);

        // Text field for user input with a filter for positive integers
        TextField yourPropositionTextField = new TextField();
        yourPropositionTextField.setPromptText("Enter your proposition");
        TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerStringConverter(), null, input -> {
            if (input.getControlNewText().matches("\\d*")) {
                // Allow only digits
                return input;
            } else {
                // Reject the input
                return null;
            }
        });
        yourPropositionTextField.setTextFormatter(textFormatter);

        // Button for proposing
        Button proposeButton = new Button("Propose");

        // Disable button when textField is empty
        proposeButton.disableProperty().bind(Bindings.isEmpty(yourPropositionTextField.textProperty()));

        // HBox for interactable elements (text field and button)
        HBox interactablesBox = new HBox(10, yourPropositionTextField, proposeButton);

        // VBox for organizing dialog content vertically
        VBox dialogContent = new VBox(10, titleLabel, opponentScoreLabel, playerScoreLabel, interactablesBox);
        dialogContent.setPadding(new Insets(15));

        // Set the content of the alert
        alert.getDialogPane().setContent(dialogContent);

        proposeButton.setOnAction(event -> {

            Platform.runLater(() -> {
                yourPropositionTextField.setDisable(true);
                alert.getDialogPane().setContent(waitingAnimation());
            });

            CountDownLatch latch1 = new CountDownLatch(1);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    //TODO: popup with the data and place for sugested score (int playerProposition)
                    try {
                        int playerProposition = Integer.parseInt(yourPropositionTextField.getText());
                        getClient().sendMessage(new ResultsNegotiationMsg(playerProposition));

                        latch1.countDown();

                        
                        //TODO: recive new message (int playerProposition) oponent's sugestion

                        ResultsNegotiationMsg response = (ResultsNegotiationMsg) getClient().receiveMessage();
                        //TODO: popup my suggestion, oponent's sugestion nad calculated score and ask if accepted

                        Label oponentsCalculatedLabel = new Label("Opponent's calculated score : " + oponentsScore);
                        Label oponentsPropositionLabel = new Label("Opponent's proposing : " + response.getPlayerProposition());
                        Label playersCalculatedLabel = new Label("Your calculated score: " + playerScore);
                        Label playersPropositionLabel = new Label("Your proposition: " + playerProposition);

                        Button agreeButton = new Button("agree");
                        Button disagreeButton = new Button("disagree");

                        
                        agreeButton.setOnAction(ev -> {
                            Platform.runLater(() -> {
                                alert.getDialogPane().setContent(waitingAnimation());
                            });

                            // Create a CountDownLatch with an initial count of 1
                            CountDownLatch latch2 = new CountDownLatch(1);

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        //TODO: send (AgreamentState)
                                        getClient().sendMessage(new ResultsNegotiationMsg(AgreementState.AGREE));
        

                                        //TODO: recive new message: if (gameoverMsg):
                                        //TODO:     - handle game over (decide who wins) (score is a float here)
                                        AbstractMessage oponentsAgreement = (AbstractMessage) getClient().receiveMessage();

                                        latch2.countDown();


                                        if (oponentsAgreement.getType() == MessageType.GAME_OVER) {
                                            //! game finished (both agreed)

                                            GameOverMsg scores = (GameOverMsg) oponentsAgreement;

                                            float oponentsFinalScore = scores.getScore()[getClient().getGame().getPlayerColor().getOpposite().toInt()];
                                            float playerFinalScore = scores.getScore()[getClient().getGame().getPlayerColor().toInt()];

                                            Platform.runLater(() -> {
                                                ImageView resImageView = new ImageView();

                                                if (playerFinalScore > oponentsFinalScore) {
                                                    resImageView.setImage(new Image(App.class.getResourceAsStream("winner.png")));
                                                }
                                                else {
                                                    resImageView.setImage(new Image(App.class.getResourceAsStream("looser.png")));
                                                }

                                                Label yourFinalScoreLabel = new Label(Float.toString(playerFinalScore));
                                                
                                                Button ok = new Button("ok");
                                                ok.setOnAction(event -> {
                                                    Platform.runLater(() -> {
                                                        closeAlert();
                                                    });

                                                    try {
                                                        client.closeConnection();
                                                        App.setRoot("menu", this, new MenuController());
                                                    } catch (IOException ex) {
                                                        ex.printStackTrace();
                                                    }
                                                });

                                                VBox endScreen = new VBox(resImageView, yourFinalScoreLabel, ok);
                                                
                                                alert.getDialogPane().setContent(endScreen);
                                            });
                                        } 
                                        else {
                                            ResultsNegotiationMsg resumeGameMsg = (ResultsNegotiationMsg) oponentsAgreement;

                                            currentPlayerColor = resumeGameMsg.getWhoseTurn();
                                            isNegotiating = false;


                                            Platform.runLater(() -> {
                                                closeAlert();
                                            });
                                        }
        
        
                                    } catch (IOException | ClassNotFoundException ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            }).start();

                            // Create a PauseTransition without a fixed duration
                            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));

                            // Set up an event handler to check if the latch2 is counted down
                            pauseTransition.setOnFinished(e -> {
                                if (latch2.getCount() == 0) { // If latch2 is counted down, proceed
                                    //! wait
                                    // alert.getDialogPane().getChildren().clear();
                                    
                                } else { // If latch2 is not counted down, continue waiting
                                    pauseTransition.play();
                                }
                            });

                            // Start the initial pause transition
                            pauseTransition.play();
                        });

                        //TODO: send (AgreamentState)
                        disagreeButton.setOnAction(ev -> {
                            Platform.runLater(() -> {
                                alert.getDialogPane().setContent(waitingAnimation());
                            });

                            // Create a CountDownLatch with an initial count of 1
                            CountDownLatch latch2 = new CountDownLatch(1);

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        //TODO: send (AgreamentState)
                                        getClient().sendMessage(new ResultsNegotiationMsg(AgreementState.DISAGREE));

                                        latch2.countDown();
        
                                        //TODO: else (resoultNegotiationMsg) (AgreamentState, WhosTurn):
                                        //TODO:     - resume game 
                                        ResultsNegotiationMsg oponentsAgreement = (ResultsNegotiationMsg) getClient().receiveMessage();

                                        currentPlayerColor = oponentsAgreement.getWhoseTurn();
                                        isNegotiating = false;

                                        System.out.println("DUPA");

                                        Platform.runLater(() -> {
                                            closeAlert();
                                        });
                                    } catch (IOException | ClassNotFoundException ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            }).start();

                            // Create a PauseTransition without a fixed duration
                            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));

                            // Set up an event handler to check if the latch2 is counted down
                            pauseTransition.setOnFinished(e -> {
                                if (latch2.getCount() == 0) { // If latch2 is counted down, proceed
                                    // Platform.runLater(() -> {
                                    //     // alert.getDialogPane().getChildren().clear();
                                    //     closeAlert();
                                    // });
                                } else { // If latch2 is not counted down, continue waiting
                                    pauseTransition.play();
                                }
                            });

                            // Start the initial pause transition
                            pauseTransition.play();

                            Platform.runLater(() -> {
                                closeAlert();
                            });
                        });

                        HBox buttons = new HBox(10, agreeButton, disagreeButton);

                        VBox agreementDialog = new VBox(oponentsCalculatedLabel, oponentsPropositionLabel, playersCalculatedLabel, playersPropositionLabel, buttons);

                        Platform.runLater(() -> {
                            alert.getDialogPane().setContent(agreementDialog);
                        });
                        
                    } catch (NumberFormatException | IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));

            // Set up an event handler to check if the latch2 is counted down
            pauseTransition.setOnFinished(e -> {
                if (latch1.getCount() == 0) { // If latch2 is counted down, proceed
                    //! wait
                } else { // If latch2 is not counted down, continue waiting
                    pauseTransition.play();
                }
            });
        });

        // Show the alert and wait for user interaction
        alert.showAndWait();
    }

    private void closeAlert() {
        // Find the open alert and close it
        javafx.scene.control.DialogPane dialogPane = getOpenDialogPane();
        if (dialogPane != null) {
            dialogPane.getScene().getWindow().hide();
        }
    }

    private javafx.scene.control.DialogPane getOpenDialogPane() {
        // Iterate through the open windows to find the alert
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (window instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) window;
                javafx.scene.Scene scene = stage.getScene();
                if (scene != null) {
                    javafx.scene.Parent root = scene.getRoot();
                    if (root instanceof javafx.scene.control.DialogPane) {
                        return (javafx.scene.control.DialogPane) root;
                    }
                }
            }
        }
        return null;
    }

    private Node waitingAnimation() {
        Arc coloredArc = new Arc();
        coloredArc.setCenterX(50);
        coloredArc.setCenterY(50);
        coloredArc.setRadiusX(50);
        coloredArc.setRadiusY(50);
        coloredArc.setStartAngle(45);
        coloredArc.setLength(270);
        coloredArc.setStroke(Color.web("#212121"));
        coloredArc.setStrokeWidth(10);
        coloredArc.setFill(Color.TRANSPARENT);

        Arc notchArc = new Arc();
        notchArc.setCenterX(50);
        notchArc.setCenterY(50);
        notchArc.setRadiusX(50);
        notchArc.setRadiusY(50);
        notchArc.setStartAngle(315);
        notchArc.setLength(90);
        notchArc.setStroke(Color.web("#af00ff"));
        notchArc.setStrokeWidth(10);
        notchArc.setFill(Color.TRANSPARENT);

        Group circle = new Group();
        circle.getChildren().addAll(coloredArc, notchArc);


        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2),  circle);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.play();

        return circle;
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
        //TODO: check implementation for bot
        if (gameMode == GameMode.MULTI_PLAYER) {
            currentPlayerColor = currentPlayerColor.getOpposite();
        }
    }

    protected void sendMoveToServer() {
        while (true) {
            if (stoneClicked) {
                System.out.println("moved");

                stoneClicked = false;
        
                try {
                    client.sendMessage(new MoveMsg(moveCordX, moveCordY));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            else if (passed) {
                System.out.println("passed");

                passed = false;
        
                setStatus("oponent's turn", Color.BLUEVIOLET);

                try {
                    client.sendMessage(new MoveMsg(true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            else if (resigned) {
                System.out.println("resigned");
        
                try {
                    client.sendMessage(new MoveMsg(UnusualMove.RESIGN));
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


    public GameController(Client client, GameMode gameMode) {
        this.gameMode = gameMode;
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
        if (isMyTurn()) {
            resigned = true;
        }
        // resoultAlert();
    }

    //! something is wrong here
    @FXML
    void pass() {
        if (isMyTurn()) {
            passed = true;
        }
    }
}
