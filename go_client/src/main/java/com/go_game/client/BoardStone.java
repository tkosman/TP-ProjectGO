package com.go_game.client;

import javafx.scene.shape.Circle;
import shared.enums.Stone;

public class BoardStone extends Circle {
    private Stone stoneColor = Stone.EMPTY;
    private int x;
    private int y;

    public BoardStone(int radius, int x, int y) {
        super(radius);
        this.x = x;
        this.y = y;
    }


    public Stone getStoneColor() {
        return this.stoneColor;
    }

    public void setStoneColor(Stone color) {
        this.stoneColor = color;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
