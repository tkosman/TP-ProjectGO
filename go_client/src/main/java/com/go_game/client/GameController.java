package com.go_game.client;

import java.net.URL;
import java.util.ResourceBundle;

import com.go_game.client.game_model.Game;
import com.go_game.client.game_model.GameBuilder;
import com.go_game.client.game_model.Player;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class GameController {
    private Game game;
    private GameBuilder gb;


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane mainAnchorPane;

    
    @FXML
    void initialize() {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'game.fxml'.";
        
        // ask server for game manifest
        // get x from server message
        // WARNING: temporary code
        int x = 13;
        Player pl1 = new Player("Ala", 1500);
        Player pl2 = new Player("Bob", 1375);
        // end of temporary code

        this.gb = new GameBuilder();
        this.gb.setX(x).setBlackPlayer(pl1).setWhitePlayer(pl2);
        this.game = this.gb.build();
    }

    
}
