package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class GameOverMsg extends AbstractMessage
{
    private String description;
    private float[] score;

    public GameOverMsg(String description, float[] score)
    {
        this.type = MessageType.GAME_OVER;
        this.description = description;
        this.score = score;
    }

    public String getdescription()
    {
        return description;
    }

    public PlayerColors getWinner()
    {
        return (score[0] > score[1]) ? PlayerColors.BLACK : PlayerColors.WHITE;
    }

    public float[] getScore()
    {
        return score;
    }

    @Override
    public String toString()
    {
        return "GameOverMsg{" +
                "description='" + description + '\'' +
                ", winner=" + getWinner() +
                '}';
    }
}
