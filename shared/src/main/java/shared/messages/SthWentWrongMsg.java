package shared.messages;

import shared.enums.MessageType;

public class SthWentWrongMsg extends AbstractMessage
{
    private String description;

    public SthWentWrongMsg(String description)
    {
        type = MessageType.STH_WENT_WRONG;
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return "SthWentWrongMsg{" +
                "description='" + description + '\'' +
                '}';
    }

}
