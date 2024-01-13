package shared.messages;

import shared.enums.MessageType;
import shared.enums.Stone;

public class GameJoinedMsg extends AbstractMessage
{
    private int gameID;
    private Stone stoneColor;
    private boolean isPlayer1Turn;

    public GameJoinedMsg(int gameID, Stone stoneColor, boolean isPlayer1Turn)
    {
        type = MessageType.GAME_JOINED;
        this.gameID = gameID;
        this.stoneColor = stoneColor;
        this.isPlayer1Turn = isPlayer1Turn;
    }

    public int getGameID()
    {
        return gameID;
    }

    public Stone getStoneColor()
    {
        return stoneColor;
    }

    public boolean isPlayer1Turn()
    {
        return isPlayer1Turn;
    }

    @Override
    public String toString()
    {
        return "GameJoinedMsg{" +
                "gameID=" + gameID +
                ", stoneColor=" + stoneColor +
                ", isPlayerTurn=" + isPlayer1Turn +
                '}';
    }
}

