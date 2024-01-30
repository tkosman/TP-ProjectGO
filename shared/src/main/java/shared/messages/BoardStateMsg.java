package shared.messages;

import shared.db.DBManager;
import shared.db.DBQueuer;
import shared.enums.MessageType;
import shared.enums.Stone;

public class BoardStateMsg extends AbstractMessage 
{
    private static final long serialVersionUID = 1L;

    private Stone[][] boardState;
    private int[] score;
    private int gameID;
    private int moveNumber;

    private static DBManager dbManager = new DBManager();
    private static DBQueuer dbQueuer = new DBQueuer(dbManager);

    public static void setDbQueuer(DBQueuer dbQueuer) {
        BoardStateMsg.dbQueuer = dbQueuer;
    }
    

    public BoardStateMsg(Stone[][] boardState, int[] score, int gameID, int moveNumber)
    {
        this.type = MessageType.BOARD_STATE;
        this.boardState = boardState;
        this.score = score;
        this.gameID = gameID;
        this.moveNumber = moveNumber;

        if (dbQueuer != null) {
            dbQueuer.saveBoardStateMsg(this);
        }
    }

    public Stone[][] getBoardState()
    {
        return boardState;
    }

    public int[] getScore()
    {
        return score;
    }

    public int getGameID()
    {
        return gameID;
    }

    public int getMoveNumber()
    {
        return moveNumber;
    }

    @Override
    public String toString()
    {
        return "BoardStateMsg { " + "boardState = " + boardState + ", score = " + score + ", gameID = " + gameID + ", moveNumber = " + moveNumber + " }";
    }
}
