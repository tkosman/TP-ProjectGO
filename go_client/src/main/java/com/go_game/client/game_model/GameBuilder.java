package com.go_game.client.game_model;

import java.util.ArrayList;

public class GameBuilder {
    private Integer x;
    private Player black;
    private Player white; 
    private ArrayList<String> state;

    public GameBuilder() {
        this.state = new ArrayList<>();
    }

    public GameBuilder setX(int x) {
        this.x = x;
        return this;
    }

    public GameBuilder setBlackPlayer(Player black) {
        this.black = black;
        return this;
    }

    public GameBuilder setWhitePlayer(Player white) {
        this.white = white;
        return this;
    }

    public GameBuilder setState(ArrayList<String> state) {
        this.state = state;
        return this;
    }

    public Game build() {
        if (x == null) {
            throw new IllegalStateException("x must be set before building Game.");
        }

        if (black == null || white == null) {
            throw new IllegalStateException("Both black and white players must be set before building Game.");
        }

        try {
            return new Game(x, black, white, state);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
