package com.go_game.server.messages;

import com.go_game.server.enums.GameMode;
import com.go_game.server.enums.MessageType;

public class ClientInfoMsg extends AbstractMessage
{
    private int boardSize;
    private GameMode gameMode;

    public ClientInfoMsg(int boardSize, GameMode gameMode)
    {
        type = MessageType.CLIENT_INFO;
        this.boardSize = boardSize;
        this.gameMode = gameMode;
    }

    public ClientInfoMsg(GameMode gameMode)
    {
        type = MessageType.CLIENT_INFO;
        this.gameMode = gameMode;
    }
    
    public GameMode getGameMode()
    {
        return gameMode;
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
                "\n\tgameMode = " + gameMode +
                "\n}";
    }
}
