package shared.messages;

import shared.enums.BoardSize;
import shared.enums.GameMode;
import shared.enums.MessageType;

public class ClientInfoMsg extends AbstractMessage
{
    private BoardSize boardSize;
    private GameMode gameMode;

    public ClientInfoMsg(BoardSize boardSize, GameMode gameMode)
    {
        type = MessageType.CLIENT_INFO;
        this.boardSize = boardSize;
        this.gameMode = gameMode;
    }

    public ClientInfoMsg(GameMode gameMode)
    {
        type = MessageType.CLIENT_INFO;
        this.gameMode = gameMode;
    }
    
    public GameMode getGameMode()
    {
        return gameMode;
    }

    public BoardSize getBoardSize()
    {
        return boardSize;
    }

    @Override
    public String toString()
    {
        return "ClientInfoMessage {\n" +
                "\ttype = " + type +
                "\n\tboardSize = " + boardSize +
                "\n\tgameMode = " + gameMode +
                "\n}";
    }
}
