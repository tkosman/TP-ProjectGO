package com.go_game.server.messages;

import com.go_game.server.enums.MessageType;

public class ClientInfo extends AbstractMessage
{
    private int boardSize;

    public ClientInfo(int boardSize)
    {
        type = MessageType.CLIENT_INFO;
        this.boardSize = boardSize;
    }

    public int getBoardSize()
    {
        return boardSize;
    }

    @Override
    public String toString()
    {
        return "ClientInfo{\n" +
                "\ttype = " + type +
                "\n\tboardSize = " + boardSize +
                "\n}";
    }
}
