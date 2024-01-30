package shared.messages;

import shared.enums.MessageType;
import shared.enums.UnusualMove;

public class MoveMsg extends AbstractMessage
{   private int x;
    private int y;
    private boolean pass = false; //indicates if player passed
    private UnusualMove unusualMove;

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
    
    public MoveMsg(UnusualMove unusualMove)
    {
        this.type = MessageType.MOVE;
        this.unusualMove = unusualMove;
    }

    public UnusualMove getUnusualMove() {
        return unusualMove;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean playerPassed() {
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
