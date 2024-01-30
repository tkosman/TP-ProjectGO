package shared.messages;

import shared.enums.MessageType;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ReplayFetchMsg extends AbstractMessage
{
    private List<BoardStateMsg> replaysList;
    private int gameID;
    private Map<Integer, Date> gameIDsAndDates;

    public ReplayFetchMsg(List<BoardStateMsg> replaysList) 
    {
        type = MessageType.REPLAY_FETCH;
        this.replaysList = replaysList;
    }

    public ReplayFetchMsg(int gameID) 
    {
        type = MessageType.REPLAY_FETCH;
        this.gameID = gameID;
    }

    public ReplayFetchMsg(Map<Integer, Date> gameIDsAndDates) 
    {
        type = MessageType.REPLAY_FETCH;
        this.gameIDsAndDates = gameIDsAndDates;
    }

    public List<BoardStateMsg> getReplaysList() 
    {
        return replaysList;
    }

    public int getGameID() 
    {
        return gameID;
    }

    public Map<Integer, Date> getGameIDsAndDates() 
    {
        return gameIDsAndDates;
    }

    @Override
    public String toString() {
        return "ReplayFetchMsg{" +
                "replayList=" + replaysList +
                '}';
    }
}
