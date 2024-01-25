package shared.messages;

import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class GameOverMsg extends AbstractMessage
{
    private PlayerColors winner;
    private String description;
    private float[] score;

    public GameOverMsg(String description, PlayerColors winner, float[] score)
    {
        this.type = MessageType.GAME_OVER;
        this.description = description;
        this.winner = winner;
        this.score = score;
    }

    public String getdescription()
    {
        return description;
    }

    public PlayerColors getWinner()
    {
        return winner;
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
                ", winner=" + winner +
                '}';
    }
}
