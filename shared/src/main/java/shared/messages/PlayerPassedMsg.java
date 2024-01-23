package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class PlayerPassedMsg extends AbstractMessage
{
    private PlayerColors playerColor;

    public PlayerPassedMsg(PlayerColors playerColor)
    {
        type = MessageType.PLAYER_PASSED;
        this.playerColor = playerColor;
    }

    public PlayerColors getPlayerColor()
    {
        return playerColor;
    }

    @Override
    public String toString()
    {
        return "PlayerPassedMsg{" +
                "type=" + type +
                '}';
    }
}
