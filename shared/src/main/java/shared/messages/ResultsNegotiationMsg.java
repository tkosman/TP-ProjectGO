package shared.messages;

import shared.enums.AgreementState;
import shared.enums.MessageType;
import shared.enums.PlayerColors;

public class ResultsNegotiationMsg extends AbstractMessage
{
    private int[] territoryScore;
    private int[] capturesScore;
    private String description;

    private int playerProposition;
    private PlayerColors whoseTurn;

    private AgreementState agreement = AgreementState.DISAGREE;


    public ResultsNegotiationMsg(String description, int[] territoryScore, int[] capturesScore)
    {
        this.type = MessageType.RESULTS_NEGOTIATION;
        this.description = description;
        this.territoryScore = territoryScore;
        this.capturesScore = capturesScore;
    }

    public ResultsNegotiationMsg(int playerProposition)
    {
        this.type = MessageType.RESULTS_NEGOTIATION;
        this.playerProposition = playerProposition;
    }

    public ResultsNegotiationMsg(AgreementState agreement)
    {
        this.type = MessageType.RESULTS_NEGOTIATION;
        this.agreement = agreement;
    }

    public ResultsNegotiationMsg(AgreementState agreement, PlayerColors whoseTurn)
    {
        this.type = MessageType.RESULTS_NEGOTIATION;
        this.agreement = agreement;
        this.whoseTurn = whoseTurn;
    }

    public int[] getTerritoryScore()
    {
        return territoryScore;
    }

    public int[] getCapturesScore()
    {
        return capturesScore;
    }

    public String getDescription()
    {
        return description;
    }

    public int getPlayerProposition()
    {
        return playerProposition;
    }

    public AgreementState getAgreement()
    {
        return agreement;
    }

    public PlayerColors getWhoseTurn()
    {
        return whoseTurn;
    }
}
