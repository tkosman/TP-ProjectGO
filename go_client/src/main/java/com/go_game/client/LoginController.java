package com.go_game.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button loginButton;

    @FXML
    private TextField loginTextField;

    @FXML
    private ImageView logoImageView;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private ButtonBar menuButtonBar;

    @FXML
    private VBox menuVBox;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button registerButton;

    @FXML
    private Label statusLabel;


    @FXML
    void initialize() {
        assert loginButton != null : "fx:id=\"loginButton\" was not injected: check your FXML file 'login.fxml'.";
        assert loginTextField != null : "fx:id=\"loginTextField\" was not injected: check your FXML file 'login.fxml'.";
        assert logoImageView != null : "fx:id=\"logoImageView\" was not injected: check your FXML file 'login.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'login.fxml'.";
        assert menuButtonBar != null : "fx:id=\"menuButtonBar\" was not injected: check your FXML file 'login.fxml'.";
        assert menuVBox != null : "fx:id=\"menuVBox\" was not injected: check your FXML file 'login.fxml'.";
        assert passwordTextField != null : "fx:id=\"passwordTextField\" was not injected: check your FXML file 'login.fxml'.";
        assert registerButton != null : "fx:id=\"registerButton\" was not injected: check your FXML file 'login.fxml'.";
        assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'login.fxml'.";
    }


    @FXML
    void logIn(ActionEvent event) throws IOException {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        LoginStatusEnum loginStatus = LoginManager.logIn(login, password);

        if (loginStatus == LoginStatusEnum.SUCCESSFUL) {
            try {
                App.setRoot("game");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            passwordTextField.clear();

            if (loginStatus == LoginStatusEnum.INCORRECT_LOGIN_OR_PASSWORD){
                displayError("Incorrect login or password.");
            } 
            else if (loginStatus == LoginStatusEnum.CONNECTION_ERROR){
                displayError("Could not connect to the server.");
            }
        }
    }

    @FXML
    void register(ActionEvent event) throws IOException {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            displayError("Login or password empty.");
        }

        RegisterStatusEnum registerStatus = LoginManager.register(login, password);

        if (registerStatus == RegisterStatusEnum.SUCCESSFUL) {
            try {
                App.setRoot("game");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            passwordTextField.clear();
            
            if (registerStatus == RegisterStatusEnum.LOGIN_ALREADY_EXISTS){
                displayError("This login already exists.");
            }
            else if (registerStatus == RegisterStatusEnum.LOGIN_TO_LONG){
                displayError("Your password can not be more than 32 characters long.");
            }
            else if (registerStatus == RegisterStatusEnum.PASSWORD_TOO_SHORT){
                displayError("Your password has to be at least 8 characters long.");
            }
            else if (registerStatus == RegisterStatusEnum.PASSWORD_TOO_LONG){
                displayError("Your password can not be more than 32 characters long.");
            }
            else if (registerStatus == RegisterStatusEnum.PASSWORD_NOT_SATISFYING_REQUIREMENTS){
                displayError("Your password has to contain at least 1 small letter, 1 big lettr and 1 number.");
            }
            else if (registerStatus == RegisterStatusEnum.CONNECTION_ERROR){
                displayError("Could not connect to the server.");
            }
        }
    }

    private void displayError(String message) {
        statusLabel.setTextFill(Paint.valueOf("red"));
        statusLabel.setText(message);
    }
}
