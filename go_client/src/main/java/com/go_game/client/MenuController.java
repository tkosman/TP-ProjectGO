package com.go_game.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import com.go_game.client.connection.Client;
import com.go_game.client.connection.Game;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import shared.enums.BoardSize;
import shared.enums.GameMode;
import shared.messages.ClientInfoMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.IndexSetMsg;

public class MenuController {
    private static final int PORT = 4444;
    private static final String HOST = "localhost";

    private Client client;

    public Client getClient() {
        return client;
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button exitButton;

    @FXML
    private VBox leftPanelVBox;

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
    private Button replayButton;

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
        assert exitButton != null : "fx:id=\"exitButton\" was not injected: check your FXML file 'menu.fxml'.";
        assert leftPanelVBox != null : "fx:id=\"leftPanelVBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert logoutHBox != null : "fx:id=\"logoutHBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'menu.fxml'.";
        assert menuHBox != null : "fx:id=\"menuHBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert middlePanelVBox != null : "fx:id=\"middlePanelVBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert modeSelectHBox != null : "fx:id=\"modeSelectHBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert replayButton != null : "fx:id=\"replayButton\" was not injected: check your FXML file 'menu.fxml'.";
        assert rightPanelVBox != null : "fx:id=\"rightPanelVBox\" was not injected: check your FXML file 'menu.fxml'.";
        assert select13x13Button != null : "fx:id=\"select13x13Button\" was not injected: check your FXML file 'menu.fxml'.";
        assert select19x19Button != null : "fx:id=\"select19x19Button\" was not injected: check your FXML file 'menu.fxml'.";
        assert select9x9Button != null : "fx:id=\"select9x9Button\" was not injected: check your FXML file 'menu.fxml'.";

        select13x13Button.setOnAction(event -> startXxXGame(BoardSize.THIRTEEN_X_THIRTEEN));
        select19x19Button.setOnAction(event -> startXxXGame(BoardSize.NINETEEN_X_NINETEEN));
        select9x9Button.setOnAction(event -> startXxXGame(BoardSize.NINE_X_NINE));
    }


    @FXML
    void logOut() {
        Platform.exit();
    }

    @FXML
    void showReplays() {
        try {
            App.setRoot("replay", this, new ReplayController());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void startXxXGame(BoardSize bs) {
        try {
            //! out 1
            this.client = new Client(new Socket(HOST, PORT));

            // IndexSetMsg msg = (IndexSetMsg) this.client.receiveMessage();
            
            
            Platform.runLater(() -> gameModeAlert(bs));
        } catch (IOException | ClassNotFoundException  e) {
            e.printStackTrace();
        }
    }


    private void gameModeAlert(BoardSize bs) {
        ButtonType cancelButton = new ButtonType("cancel", ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(AlertType.NONE, "", cancelButton);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("darkTheme.css").toExternalForm());
        alert.getDialogPane().setPrefSize(300, 300);

        alert.setResult(ButtonType.OK);

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

        alert.setOnCloseRequest(event -> {
            alert.setResult(ButtonType.CANCEL);
        });

        pvpButton.setOnAction(event -> {
            gameModeSelectVBox.getChildren().clear();
            Platform.runLater(() -> {
                gameModeSelectVBox.getChildren().add(matchmakingAlert());
            });

            // Create a CountDownLatch with an initial count of 1
            CountDownLatch latch = new CountDownLatch(1);
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //! 2 in
                        IndexSetMsg playerIndex = (IndexSetMsg) getClient().receiveMessage();
                        System.out.println("You are player " + playerIndex.getIndex() + "\n");

                        //! 3 out
                        getClient().sendMessage(new ClientInfoMsg(bs, GameMode.MULTI_PLAYER));


                        //! 4 in
                        GameJoinedMsg gameJoinedMsg = (GameJoinedMsg) getClient().receiveMessage();
                        System.out.println("Game ID: " + gameJoinedMsg.getGameID());

                        getClient().setGame(new Game(bs, gameJoinedMsg.getGameID(), gameJoinedMsg.getPlayerColors()));

                        latch.countDown();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Create a PauseTransition without a fixed duration
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));

            // Set up an event handler to check if the latch is counted down
            pauseTransition.setOnFinished(e -> {
                if (latch.getCount() == 0) { // If latch is counted down, proceed
                    alert.setResult(ButtonType.OK);

                    if (!alert.getResult().equals(ButtonType.CANCEL)) {
                        try {
                            App.setRoot("game", this, new GameController(this.client));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    alert.close();
                } else { // If latch is not counted down, continue waiting
                    pauseTransition.play();
                }
            });

            // Start the initial pause transition
            pauseTransition.play();
        });

        botButton.setOnAction(event -> {
            alert.setResult(ButtonType.OK);

            if (!alert.getResult().equals(ButtonType.CANCEL)) {
                try {
                    this.client.sendMessage(new ClientInfoMsg(GameMode.BOT));
                    App.setRoot("game", this, new GameController(this.client));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            alert.close();
        });

        gameModeSelectVBox.getChildren().addAll(titleLable, pvpButton, botButton);
        VBox.setVgrow(pvpButton, Priority.ALWAYS);
        VBox.setVgrow(botButton, Priority.ALWAYS);

        alert.getDialogPane().setContent(gameModeSelectVBox);


        // TODO: to make it exit the queue when canceling
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == cancelButton) {
            try {
                this.client.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Node matchmakingAlert() {
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
}
