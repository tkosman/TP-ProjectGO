package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import shared.enums.BoardSize;
import shared.enums.Stone;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.MoveMsg;
import shared.messages.OkMsg;
import shared.messages.StringMsg;



//? This class will be responsible for game logic and will be an individual thread for each game session
public class GameLogicThread implements Runnable
{
    private ObjectOutputStream toPlayer1;
    private ObjectInputStream fromPlayer1;
    private ObjectOutputStream toPlayer2;
    private ObjectInputStream fromPlayer2;
    private static int gameID = 0;
    private int boardSize;
    private Stone[][] board;
    private boolean isPlayer1Turn = true;

    //! I assume that player 1 is always black and player 2 is always white

    //TODO: pass socket to close it later
    public GameLogicThread(ObjectOutputStream toPlayer1, ObjectInputStream fromPlayer1, ObjectOutputStream toPlayer2, 
                            ObjectInputStream fromPlayer2, BoardSize enumBoardSize) throws IOException
    {
        this.toPlayer1 = toPlayer1;
        this.fromPlayer1 = fromPlayer1;
        this.toPlayer2 = toPlayer2;
        this.fromPlayer2 = fromPlayer2;

        if (enumBoardSize == BoardSize.NINE_X_NINE)
        {
            boardSize = 9;
        }
        else if (enumBoardSize == BoardSize.THIRTEEN_X_THIRTEEN)
        {
            boardSize = 13;
        }
        else if (enumBoardSize == BoardSize.NINETEEN_X_NINETEEN)
        {
            boardSize = 19;
        }

        gameID++;
        //! 4 OUT
        toPlayer1.writeObject(new GameJoinedMsg(gameID, Stone.BLACK, isPlayer1Turn));
        toPlayer2.writeObject(new GameJoinedMsg(gameID, Stone.WHITE, isPlayer1Turn));


        initializeBoard();

        //! HANDSHAKE FINISHED
        // run();

        //TODO: close sockets
    }

    //! testing
    public static void main(String[] args)
    {
        try
        {
            GameLogicThread glt = new GameLogicThread(null, null, null, null, BoardSize.NINE_X_NINE);
            glt.initializeBoard();
            glt.printBoard();
            glt.processMove(1, 1);
            glt.switchTurns();
            glt.processMove(0, 1);
            glt.processMove(1, 0);
            glt.processMove(2, 1);

            glt.printBoard();
            glt.processMove(1, 2);
            glt.checkForCapturedStones(1, 2);
            glt.printBoard();
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    //! END testing

    @Override
    public void run()
    {
        try
        {
            while(!isGameOver())
            {
                MoveMsg moveMsg = waitForPlayerMove();
                int x = moveMsg.getX();
                int y = moveMsg.getY();

                //TODO: check if player passed

                if (isMoveValid(x, y))
                {
                    processMove(x, y);
                    checkForCapturedStones(x, y);
                    switchTurns();
                    sendBoardState();

                }
            }
        }
        //TODO: handle this exception correctly
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void processMove(int x, int y)
    {
        board[x][y] = isPlayer1Turn ? Stone.BLACK : Stone.WHITE;
    }

    //? this method will be called after each move. It will check for captured stones and remove them from the board
    private void checkForCapturedStones(int x, int y)
    {
        Stone playerStone = isPlayer1Turn ? Stone.BLACK : Stone.WHITE;
        Stone opponentStone = isPlayer1Turn ? Stone.WHITE : Stone.BLACK;

        //? we need to check the 4 directions around the stone
        captureGroupIfSurrounded(x + 1, y, opponentStone);
        captureGroupIfSurrounded(x - 1, y, opponentStone);
        captureGroupIfSurrounded(x, y + 1, opponentStone);
        captureGroupIfSurrounded(x, y - 1, opponentStone);
    }

    private void captureGroupIfSurrounded(int x, int y, Stone opponentStone)
    {
        //? if the stone is out of board or it is not an opponent stone, we can simply return
        if (isOutOfBoard(x, y) || board[x][y] != opponentStone)
        {
            return;
        }

        ArrayList<Point> group = new ArrayList<>();
        ArrayList<Point> liberties = new ArrayList<>();
        findGroupWithLiberties(x, y, opponentStone, group, liberties);

        if (liberties.isEmpty())
        {
            for (Point stone : group)
            {
                board[stone.x][stone.y] = Stone.EMPTY;
            }
        }
    }
    //? check if the stone is out of board
    private boolean isOutOfBoard(int x, int y)
    {
        return x < 0 || x >= boardSize || y < 0 || y >= boardSize;
    }

    //? this method will recursively find the group of stones with their liberties
    private void findGroupWithLiberties(int x, int y, Stone stone, ArrayList<Point> group, ArrayList<Point> liberties)
    {
        if (isOutOfBoard(x, y) || group.contains(new Point(x, y)))
        {
            return;
        }
    
        if (board[x][y] == stone)
        {
            group.add(new Point(x, y));
            findGroupWithLiberties(x + 1, y, stone, group, liberties);
            findGroupWithLiberties(x - 1, y, stone, group, liberties);
            findGroupWithLiberties(x, y + 1, stone, group, liberties);
            findGroupWithLiberties(x, y - 1, stone, group, liberties);
        }
        else if (board[x][y] == Stone.EMPTY)
        {
            liberties.add(new Point(x, y));
        }
    }

    private void switchTurns()
    {
        isPlayer1Turn = !isPlayer1Turn;
    }

    private void sendBoardState() throws IOException
    {
        BoardStateMsg boardStateMsg = new BoardStateMsg(board);
        toPlayer1.writeObject(boardStateMsg);
        toPlayer2.writeObject(boardStateMsg);
    }


    //TODO: yet to be implemented
    private boolean isGameOver()
    {
        //TODO: Implement logic to determine if the game is over
        return false; // Placeholder
    }

    private MoveMsg waitForPlayerMove() throws IOException, ClassNotFoundException
    {
        try
        {
            if (isPlayer1Turn)
            {
                return (MoveMsg) fromPlayer1.readObject();
            } else {
                return (MoveMsg) fromPlayer2.readObject();
            }

        //TODO: handle this exception correctly
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private boolean isMoveValid(int x, int y)
    {
        
        //? check for basic board bounds and empty space
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize || board[x][y] != Stone.EMPTY)
        {
            return false;
        }
    
        //TODO: check ko situation & suicide move
        if (isKoSituation(x, y) || isSuicideMove(x, y)) {
            return false;
        }
    
        return true;
    }
    
    //TODO: yet to be implemented
    private boolean isKoSituation(int x, int y) {
        // Implement the logic to check for a Ko situation
        return false; // Placeholder
    }
    
    //TODO: yet to be implemented
    private boolean isSuicideMove(int x, int y) {
        // Implement the logic to check if the move is a suicide move
        return false; // Placeholder
    }

    //! for debugging purposes
    private void printBoard() {
        for (int y = 0; y < boardSize; y++) {
            for (int x = 0; x < boardSize; x++) {
                switch (board[x][y]) {
                    case BLACK:
                        System.out.print("B ");
                        break;
                    case WHITE:
                        System.out.print("W ");
                        break;
                    default:
                        System.out.print(". ");
                        break;
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }


    private void initializeBoard() 
    {
        board = new Stone[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++)
        {
            for (int j = 0; j < boardSize; j++) 
            {
                board[i][j] = Stone.EMPTY;
            }
        }
    }
}
