package com.go_game.client.connection;

import shared.enums.BoardSize;
import shared.enums.PlayerColors;
import shared.enums.Stone;

public class Game {
    private int ID;
    private BoardSize BS;
    private PlayerColors playerColor;
    private Stone[][] state;
    private int[] score;


    public Game(BoardSize boardSize, int id, PlayerColors playerColor) {
        this.BS = boardSize;
        this.ID = id;
        this.playerColor = playerColor;
        this.state = new Stone[boardSize.getIntSize() + 1][boardSize.getIntSize() + 1];
        this.score = new int[2];
        this.score[0] = 0; 
        this.score[1] = 0; 
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

    public int getScore(int player) {
        return score[player];
    }

    public void setScore(int[] score) {
        this.score = score;
    }
}
