package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class GameOverMsg extends AbstractMessage
{
    private String reasonOrResult;
    private PlayerColors winner;

    public GameOverMsg(String reasonOrResult, PlayerColors winner)
    {
        this.type = MessageType.GAME_OVER;
        this.reasonOrResult = reasonOrResult;
        this.winner = winner;
    }

    public String getReasonOrResult()
    {
        return reasonOrResult;
    }

    public PlayerColors getWinner()
    {
        return winner;
    }

    @Override
    public String toString()
    {
        return "GameOverMsg{" +
                "reasonOrResult='" + reasonOrResult + '\'' +
                ", winner=" + winner +
                '}';
    }
}
