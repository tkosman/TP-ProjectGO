package com.go_game.server;

import java.io.Serializable;

public class Message implements Serializable
{
    private static enum typeOfPlayers {PLAYER1, PLAYER2};

    private MessageType type;
    private String content;
    private int xCoordinate;
    private char yCoordinate;
    private typeOfPlayers whoDidMove;

    //TODO:change this enum for actual states used in the game
    public enum MessageType
    {
        NEW_JOINED,     //? new player joined the game
        MOVE,           //? player made a move
        GAME_OVER       //? game is over
    }

    //? Usual message
    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    //? move message
    public Message(MessageType type, typeOfPlayers whoDidMove, int xCoordinate, char yCoordinate) {
        this.type = type;
        this.whoDidMove = whoDidMove;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public char getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(char yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public typeOfPlayers getWhoDidMove() {
        return whoDidMove;
    }

    public void setWhoDidMove(typeOfPlayers whoDidMove) {
        this.whoDidMove = whoDidMove;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                ", whoDidMove=" + whoDidMove +
                '}';
    }
}
