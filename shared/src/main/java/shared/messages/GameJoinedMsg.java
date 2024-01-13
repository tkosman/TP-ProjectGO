package shared.messages;

import shared.enums.MessageType;
import shared.enums.Stone;

public class GameJoinedMsg extends AbstractMessage
{
    private int gameID;
    private Stone stoneColor;

    public GameJoinedMsg(int gameID, Stone stoneColor)
    {
        this.gameID = gameID;
        this.stoneColor = stoneColor;
        type = MessageType.GAME_JOINED;
    }

    public int getGameID()
    {
        return gameID;
    }

    public Stone getStoneColor()
    {
        return stoneColor;
    }

    @Override
    public String toString()
    {
        return "GameJoinedMsg{" +
                "gameID=" + gameID +
                ", stoneColor=" + stoneColor +
                '}';
    }
}

