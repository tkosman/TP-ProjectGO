package shared.messages;

import shared.db.DBQueuer;
import shared.enums.MessageType;
import shared.enums.Stone;

public class BoardStateMsg extends AbstractMessage 
{
    private Stone[][] boardState;
    private int[] score;

    private static DBQueuer dbQueuer;

    public static void setDbQueuer(DBQueuer dbQueuer) {
        BoardStateMsg.dbQueuer = dbQueuer;
    }
    

    public BoardStateMsg(Stone[][] boardState, int[] score)
    {
        this.type = MessageType.BOARD_STATE;
        this.boardState = boardState;
        this.score = score;

        // queueSaveToDatabase();
    }

    public Stone[][] getBoardState()
    {
        return boardState;
    }

    public int[] getScore()
    {
        return score;
    }

    // private String serialize() throws IOException {
    //     ObjectMapper mapper = new ObjectMapper();
    //     return mapper.writeValueAsString(this);
    // }

    @Override
    public String toString()
    {
        return "BoardStateMsg [boardState=" + boardState + "]";
    }
}
