package shared.messages;

import shared.db.DBQueuer;
import shared.enums.MessageType;
import shared.enums.Stone;

public class BoardStateMsg extends AbstractMessage 
{
    private Stone[][] boardState;
    private int[] score;

    DBQueuer dbQueuer;
    

    public BoardStateMsg(Stone[][] boardState, int[] score)
    {
        this.type = MessageType.BOARD_STATE;
        this.boardState = boardState;
        this.score = score;
    }

    public Stone[][] getBoardState()
    {
        return boardState;
    }

    public int[] getScore()
    {
        return score;
    }

    @Override
    public String toString()
    {
        return "BoardStateMsg [boardState=" + boardState + "]";
    }
}
