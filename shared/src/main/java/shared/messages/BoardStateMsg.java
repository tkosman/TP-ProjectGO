package shared.messages;

import shared.enums.MessageType;
import shared.enums.Stone;

//TODO: add score
public class BoardStateMsg extends AbstractMessage 
{
    private Stone[][] boardState;
    private int[] score;

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
