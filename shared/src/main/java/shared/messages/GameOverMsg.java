package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class GameOverMsg extends AbstractMessage
{
    private String reason;
    private PlayerColors winner;

    public GameOverMsg(String reason, PlayerColors winner)
    {
        this.type = MessageType.GAME_OVER;
        this.reason = reason;
        this.winner = winner;
    }

    public String getReason()
    {
        return reason;
    }

    public PlayerColors getWinner()
    {
        return winner;
    }

    @Override
    public String toString()
    {
        return "GameOverMsg{" +
                "reason='" + reason + '\'' +
                ", winner=" + winner +
                '}';
    }
}
