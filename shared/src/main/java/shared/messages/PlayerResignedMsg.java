package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class PlayerResignedMsg extends AbstractMessage
{
    private PlayerColors playerWhoResigned;
    private String description;

    public PlayerResignedMsg(String description, PlayerColors playerWhoResigned)
    {
        type = MessageType.PLAYER_RESIGNED;
        this.playerWhoResigned = playerWhoResigned;
        this.description = description;
    }

    public PlayerColors playerWhoResigned()
    {
        return playerWhoResigned;
    }

    public String getDescription()
    {
        return description;
    }
}
