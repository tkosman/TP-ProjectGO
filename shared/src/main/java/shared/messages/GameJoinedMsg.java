package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class GameJoinedMsg extends AbstractMessage
{
    private int gameID;
    private PlayerColors playerColor;
    private PlayerColors whoseTurn;
    // private boolean isPlayer1Turn;

    public GameJoinedMsg(int gameID, PlayerColors playerColor, PlayerColors whoseTurn)
    {
        type = MessageType.GAME_JOINED;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.whoseTurn = whoseTurn;
    }

    public int getGameID()
    {
        return gameID;
    }

    public PlayerColors getPlayerColors()
    {
        return playerColor;
    }

    public PlayerColors getWhoseTurn()
    {
        return whoseTurn;
    }

    @Override
    public String toString()
    {
        return "GameJoinedMsg{" +
                "gameID=" + gameID +
                ", playerColor=" + playerColor +
                ", playerTurn=" +  whoseTurn +
                '}';
    }
}

