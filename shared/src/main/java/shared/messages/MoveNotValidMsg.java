package shared.messages;

import shared.enums.MessageType;

public class MoveNotValidMsg extends AbstractMessage
{
    private int playerWhoDidNotValidMove;

    public MoveNotValidMsg(int playerWhoDidNotValidMove)
    {
        type = MessageType.MOVE_NOT_VALID;
        this.playerWhoDidNotValidMove = playerWhoDidNotValidMove;
    }

    public int playerWhoDidNotValidMove()
    {
        return playerWhoDidNotValidMove;
    }

    @Override
    public String toString()
    {
        return "MoveNotValidMsg{" +
                " MOVE INVALID -> " +
                "player1didNotValidMove=" + playerWhoDidNotValidMove +
                '}';
    }
}
