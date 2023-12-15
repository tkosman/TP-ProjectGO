package com.go_game.client;

public class LoginManager {
    
    public static LoginStatusEnum logIn(String login, String password) {
        //TODO: send login data to server and await response

        return LoginStatusEnum.INCORRECT_LOGIN_OR_PASSWORD;
    }

    public static RegisterStatusEnum register(String login, String password) {
        //TODO: send login data to server and await response

        return RegisterStatusEnum.LOGIN_ALREADY_EXISTS;
    }
}
