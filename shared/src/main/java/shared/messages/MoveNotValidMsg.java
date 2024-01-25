package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class MoveNotValidMsg extends AbstractMessage
{
    private PlayerColors playerWhoDidNotValidMove;
    private String description;

    public MoveNotValidMsg(PlayerColors playerWhoDidNotValidMove, String description)
    {
        type = MessageType.MOVE_NOT_VALID;
        this.playerWhoDidNotValidMove = playerWhoDidNotValidMove;
        this.description = description;
    }

    public PlayerColors playerWhoDidNotValidMove()
    {
        return playerWhoDidNotValidMove;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return "MoveNotValidMsg{" +
                "playerWhoDidNotValidMove=" + playerWhoDidNotValidMove +
                ", description='" + description + '\'' +
                '}';
    }
}
