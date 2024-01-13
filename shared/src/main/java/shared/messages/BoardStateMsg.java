package shared.messages;

import shared.enums.MessageType;
import shared.enums.Stone;

public class BoardStateMsg extends AbstractMessage 
{
    private final Stone[][] boardState;

    public BoardStateMsg(Stone[][] boardState)
    {
        this.type = MessageType.BOARD_STATE;
        this.boardState = boardState;
    }

    public Stone[][] getBoardState()
    {
        return boardState;
    }
}
