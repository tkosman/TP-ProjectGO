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

    public Integer getX() {
        if (this.BS == BoardSize.NINE_X_NINE) return 9;
        else if (this.BS == BoardSize.THIRTEEN_X_THIRTEEN) return 13;
        else if (this.BS == BoardSize.NINETEEN_X_NINETEEN) return 19;
        else return null;
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
