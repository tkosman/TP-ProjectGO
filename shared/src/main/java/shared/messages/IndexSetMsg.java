package shared.messages;

import shared.enums.MessageType;

public class IndexSetMsg extends AbstractMessage
{
    private int index;

    public IndexSetMsg(int index)
    {
        this.index = index;
        type = MessageType.INDEX_SET;
    }

    public int getIndex()
    {
        return index;
    }   

    @Override
    public String toString()
    {
        return "IndexSet{\n" +
                "\ttype = " + type +
                "\n\tindex = " + index +
                "\n}";
    }
}
