package com.go_game.server.messages;

import com.go_game.server.enums.MessageType;

public abstract class AbstractMessage 
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
