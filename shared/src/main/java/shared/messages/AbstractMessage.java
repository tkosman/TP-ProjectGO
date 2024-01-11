package shared.messages;

import java.io.Serializable;

import shared.enums.MessageType;

public abstract class AbstractMessage implements Serializable
{
    protected MessageType type;

    public MessageType getType() 
    {
        return type;
    }

    @Override
    public String toString() 
    {
        return "Message{\n" +
                "\ttype = " + type +
                "\n}";
    }
    
}
