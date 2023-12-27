package com.go_game.server.messages;

import com.go_game.server.enums.MessageType;

public class OkMsg extends AbstractMessage
{
    public OkMsg()
    {
        type = MessageType.OK;
    }
}
