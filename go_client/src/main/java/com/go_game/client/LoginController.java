package com.go_game.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LoginController implements Runnable {
    private Thread serverStatus;
    private boolean threadFlag;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ImageView logoImageView;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox menuVBox;

    @FXML
    private Button playButton;

    @FXML
    private ImageView serverStatusImageView;

    @FXML
    private Label serverStatusLabel;

    @FXML
    void initialize() {
        assert logoImageView != null : "fx:id=\"logoImageView\" was not injected: check your FXML file 'login.fxml'.";
        assert mainBorderPane != null : "fx:id=\"mainBorderPane\" was not injected: check your FXML file 'login.fxml'.";
        assert menuVBox != null : "fx:id=\"menuVBox\" was not injected: check your FXML file 'login.fxml'.";
        assert playButton != null : "fx:id=\"playButton\" was not injected: check your FXML file 'login.fxml'.";
        assert serverStatusImageView != null : "fx:id=\"serverStatusImageView\" was not injected: check your FXML file 'login.fxml'.";
        assert serverStatusLabel != null : "fx:id=\"serverStatusLabel\" was not injected: check your FXML file 'login.fxml'.";

        this.threadFlag = true;
        this.serverStatus = new Thread(this);
        this.serverStatus.start();
    }


    @FXML
    void play(ActionEvent event) throws IOException {
        Platform.runLater(() -> {
            try {
                this.threadFlag = false;
                App.setRoot("menu");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void run() {
        AtomicBoolean serverUp = new AtomicBoolean(false);
        while (this.threadFlag) {
            //! Replace with the actual server address
            String serverAddress = "localhost";

            try {
                InetAddress inetAddress = InetAddress.getByName(serverAddress);
    
                int timeout = 5000; // 5 seconds
                serverUp.set(inetAddress.isReachable(timeout));
    
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + e.getMessage());
            } catch (java.io.IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }

            Platform.runLater(() -> {
                if (serverUp.get()) {
                    serverStatusImageView.setImage(new Image (getClass().getResourceAsStream("ok.png")));
                }
                else {
                    serverStatusImageView.setImage(new Image (getClass().getResourceAsStream("bad.png")));
                }
            });

            try {
                Thread.sleep(2000); // 2 seconds
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
