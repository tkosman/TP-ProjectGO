package shared.messages;

import shared.enums.MessageType;

public class OkMsg extends AbstractMessage
{
    public OkMsg()
    {
        type = MessageType.OK;
    }
}
