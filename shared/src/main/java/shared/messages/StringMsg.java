package shared.messages;

import shared.enums.MessageType;

public class StringMsg extends AbstractMessage
{
    private String message;

    public StringMsg(String message)
    {
        this.message = message;
        type = MessageType.STRING_MESSAGE;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return message;
    }
}
