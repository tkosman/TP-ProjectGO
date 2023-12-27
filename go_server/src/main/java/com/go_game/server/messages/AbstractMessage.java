package com.go_game.server.messages;

import com.go_game.server.enums.MessageType;
import java.io.Serializable;

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
