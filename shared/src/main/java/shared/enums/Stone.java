package shared.enums;

public enum Stone
{
    BLACK, 
    WHITE, 
    EMPTY;

    public Stone getOpposite()
    {
        if (this == BLACK)
        {
            return WHITE;
        }
        else if (this == WHITE)
        {
            return BLACK;
        }
        else
        {
            return EMPTY;
        }
    }

    public PlayerColors toPlayerColors()
    {
        if (this == BLACK)
        {
            return PlayerColors.BLACK;
        }
        else if (this == WHITE)
        {
            return PlayerColors.WHITE;
        }
        else
        {
            return null;
        }
    }
}
