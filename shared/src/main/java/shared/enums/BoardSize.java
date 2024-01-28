package shared.enums;

public enum BoardSize 
{
    NINE_X_NINE,
    THIRTEEN_X_THIRTEEN,
    NINETEEN_X_NINETEEN;

    public int toInt()
    {
        switch (this)
        {
            case NINE_X_NINE:
                return 9;
            case THIRTEEN_X_THIRTEEN:
                return 13;
            case NINETEEN_X_NINETEEN:
                return 19;
            default:
                return -5;
        }
    }

}
