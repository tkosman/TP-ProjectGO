package shared.messages;

import shared.enums.MessageType;

public class CloseMsg extends AbstractMessage{
    public CloseMsg() {
        type = MessageType.CLOSE;
    }
}
