package com.go_game.client.game_model;

import java.util.ArrayList;

public class Game {
    private Integer x = null;
    private Player black;
    private Player white;
    private ArrayList<String> state;


    public Game() {
        this.state = new ArrayList<String>();
    }

    public Game(int x, Player black, Player white) {
        this.x = x;
        this.black = black;
        this.white = white;
        this.state = new ArrayList<String>();
    }

    public Game(int x, Player black, Player white, ArrayList<String> state) {
        this.x = x;
        this.black = black;
        this.white = white;
        this.state = new ArrayList<String>();
        this.state = state;
    }


    public void setX(int x) throws IllegalArgumentException {
        if (this.x != null) {
            throw new IllegalArgumentException("x alerady set");
        }
        
        if (x < 0) {
            throw new IllegalArgumentException("x must be positive");
        }
        
        this.x = x;
    }

    public Integer getX() {
        return this.x;
    }

    public void setPlayer(Player player, ColorEnum color) throws IllegalArgumentException{
        if (color == ColorEnum.BLACK) {
            if (this.black != null) {
                throw new IllegalArgumentException("The black player is already set.");
            }
            this.black = player;
        } 
        else {
            if (this.white != null) {
                throw new IllegalArgumentException("The white player is already set.");
            }
            this.white = player;
        }   
    }

    public String getLastMove() {
        if (state.isEmpty()) {
            return null;
        }
        return new String(state.get(state.size() - 1));
    }
    
    public int getLastMoveID() {
        int id = this.state.size();
        if (id == 0) {
            // returns -1 when state empty
            return -1;
        }
        return id;
    }    

    public void playMove(String move) {
        this.state.add(move);
    }

    public String getMove(int moveID) {
        return this.state.get(moveID - 1);
    }

    public void setState(ArrayList<String> state) throws IllegalArgumentException {
        if (state != null){
            throw new IllegalArgumentException("The game state already exists.");
        }
        this.state = state;
    }
}
