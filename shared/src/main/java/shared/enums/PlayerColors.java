package shared.enums;

public enum PlayerColors 
{
    BLACK,
    WHITE;

    public PlayerColors toggle() 
    {
        if (this == BLACK)
        {
            return WHITE;
        } 
        else 
        {
            return BLACK;
        }
    }

    public PlayerColors getOpposite() 
    {
        if (this == BLACK)
        {
            return WHITE;
        } 
        else 
        {
            return BLACK;
        }
    }

    public Stone toStone() 
    {
        if (this == BLACK)
        {
            return Stone.BLACK;
        } 
        else 
        {
            return Stone.WHITE;
        }
    }

    public String toString() {
        if (this == BLACK) return "BLACK";
        else return "WHITE";
    }

    public int toInt() {
        if (this == BLACK) return 0;
        else return 1;
    }
}