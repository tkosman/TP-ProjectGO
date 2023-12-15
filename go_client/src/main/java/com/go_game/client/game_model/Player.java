package com.go_game.client.game_model;

public class Player {
    private String name;
    private int elo;
    

    public Player(String name, int elo){
        this.name = name;
        this.elo = elo;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }
}
