package shared.messages;

import shared.enums.MessageType;

public class MoveMsg extends AbstractMessage
{   private int x;
    private int y;
    private boolean pass = false; //indicates if player passed

    public MoveMsg(int x, int y)
    {
        type = MessageType.MOVE;
        this.x = x;
        this.y = y;
    }

    public MoveMsg(boolean pass) {
        this.type = MessageType.MOVE;
        this.pass = pass;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPass() {
        return pass;
    }

    @Override
    public String toString() {
        return "MoveMsg{" +
                "x=" + x +
                ", y=" + y +
                ", pass=" + pass +
                ", type=" + type +
                '}';
    }
    
}
