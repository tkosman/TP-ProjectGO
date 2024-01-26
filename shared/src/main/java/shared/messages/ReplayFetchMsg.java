package shared.messages;

import shared.enums.MessageType;
import java.util.List;

public class ReplayFetchMsg extends AbstractMessage
{
    private List<List<String>> replayList;

    public ReplayFetchMsg(List<List<String>> replayList) {
        this.replayList = replayList;
        type = MessageType.REPLAY_FETCH;
    }

    public List<List<String>> getReplayList() {
        return this.replayList;
    }

    @Override
    public String toString() {
        return "ClientInfoMessage {\n" +
                "\ttype = " + type +
                "\n\treplays = " + replayList.toString() +
                "\n}";
    }
}
