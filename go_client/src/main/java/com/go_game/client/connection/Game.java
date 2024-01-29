package com.go_game.client.connection;

import java.util.ArrayList;

import shared.enums.BoardSize;
import shared.enums.PlayerColors;
import shared.enums.Stone;

public class Game {
    private int ID;
    private BoardSize BS;
    private PlayerColors playerColor;
    private Stone[][] state;


    public Game(BoardSize boardSize, int id, PlayerColors playerColor) {
        this.BS = boardSize;
        this.ID = id;
        this.playerColor = playerColor;
        this.state = new Stone[boardSize.getIntSize() + 1][boardSize.getIntSize() + 1];
    }

    public int getID() {
        return this.ID;
    }

    public BoardSize getBS() {
        return this.BS;
    }

    public PlayerColors getPlayerColor() {
        return this.playerColor;
    }
    
    public Stone[][] getState() {
        return state;
    }

    public void setState(Stone[][] state) {
        this.state = state;
    }
}
