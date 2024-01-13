package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import shared.enums.BoardSize;
import shared.enums.Stone;
import shared.messages.MoveMsg;
import shared.messages.OkMsg;



//? This class will be responsible for game logic and will be an individual thread for each game session
public class GameLogicThread implements Runnable
{
    private ObjectOutputStream toPlayer1;
    private ObjectInputStream fromPlayer1;
    private ObjectOutputStream toPlayer2;
    private ObjectInputStream fromPlayer2;
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

        initializeBoard();
        

        //! 4 for debugging
        toPlayer1.writeObject(new OkMsg());
        toPlayer2.writeObject(new OkMsg());

        run();

        //TODO: close sockets
    }

    //TODO: yet to be implemented
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

                    //TODO: implement checkForCapturedStones
                    checkForCapturedStones();

                    //TODO: implement checkForDeadStones
                    // switchPlayerTurn();

                    //TODO: implement sendBoardState
                    // sendBoardState();

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


    //TODO: yet to be implemented
    private boolean isGameOver()
    {
        // Implement logic to determine if the game is over
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
