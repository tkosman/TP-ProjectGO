package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;

import shared.enums.BoardSize;
import shared.enums.PlayerColors;
import shared.enums.Stone;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;



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
    private Stone[][] previousBoard;
    // private boolean isPlayer1Turn = true;
    private PlayerColors whoseTurn = PlayerColors.BLACK;

    //! I assume that player 1 is always black and player 2 is always white

    //TODO: pass socket to close it later
    public GameLogicThread(ObjectOutputStream toPlayer1, ObjectInputStream fromPlayer1, ObjectOutputStream toPlayer2, 
                            ObjectInputStream fromPlayer2, BoardSize enumBoardSize) throws IOException
    {
        this.toPlayer1 = toPlayer1;
        this.fromPlayer1 = fromPlayer1;
        this.toPlayer2 = toPlayer2;
        this.fromPlayer2 = fromPlayer2;

        //TODO: refactor this to an extern class
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
        toPlayer1.writeObject(new GameJoinedMsg(gameID, PlayerColors.BLACK, whoseTurn));
        toPlayer1.reset();
        toPlayer2.writeObject(new GameJoinedMsg(gameID, PlayerColors.WHITE, whoseTurn));
        toPlayer2.reset();


        initializeBoard();

        //! HANDSHAKE FINISHED
        Thread fred = new Thread(this);
        fred.start();

        //TODO: close sockets
    }

    @Override
    public void run()
    {
        try
        {
            while(!isGameOver())
            {
                System.out.println("\n\n####################### GAME " + gameID + " #######################");
                System.out.println("TURN: " + whoseTurn);

                MoveMsg moveMsg;
                if (!whoseTurn.equals(PlayerColors.WHITE))
                {
                    //! 1 IN +++++++++ -> Player 1 playing and player 2 sending OK
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " Player BLACK playing"); //! for debugging purposes
                    moveMsg = (MoveMsg) fromPlayer1.readObject();
                    OkMsg okMsg = (OkMsg) fromPlayer2.readObject();
                }
                else
                {
                    //! 1 IN +++++++++ -> Player 2 playing and player 1 sending OK
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " Player WHITE playing"); //! for debugging purposes
                    moveMsg = (MoveMsg) fromPlayer2.readObject();
                    OkMsg okMsg = (OkMsg) fromPlayer1.readObject();
                }

                if (moveMsg.playerPassed())
                {
                    //TODO: implement pass logic
                    System.exit(0);
                }

                int x = moveMsg.getX();
                int y = moveMsg.getY();

                if (!isMoveValid(x, y))
                {
                    //? move is INVALID
                    //? sending info to player that the move is INVALID he needs to repeat it
                    if (whoseTurn.equals(PlayerColors.BLACK))
                    {   
                        //! 2 OUT ##########
                        System.out.println(new Timestamp(System.currentTimeMillis()) + " INVALID MOVE BY PLAYER 1\n"); //! for debugging purposes
                        toPlayer1.reset();
                        toPlayer1.writeObject(new MoveNotValidMsg(1));
                        toPlayer2.reset();
                        toPlayer2.writeObject(new MoveNotValidMsg(1));
                        
                    }
                    else 
                    {
                        //! 2 OUT ##########
                        System.out.println(new Timestamp(System.currentTimeMillis()) + " INVALID MOVE BY PLAYER 2\n"); //! for debugging purposes
                        toPlayer2.reset();
                        toPlayer2.writeObject(new MoveNotValidMsg(2));
                        toPlayer1.reset();
                        toPlayer1.writeObject(new MoveNotValidMsg(2));
                    }
                }
                else
                {
                    processMove(x, y);
                    checkForCapturedStones(x, y);

                    //! 2 OUT ##########
                    sendBoardState();
                    
                    Thread.sleep(1000); //! for debugging purposes
                    switchTurns();
                }

            }
        }
        //TODO: handle this exception correctly
        catch (InterruptedException | IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void sendBoardState() throws IOException
    {
        BoardStateMsg boardStateMsg = new BoardStateMsg(board);

        //! 2 ########## OUT -> Sending board state to players
        System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING BOARD STATE TO PLAYER 1"); //! for debugging purposes
        toPlayer1.writeObject(boardStateMsg);
        toPlayer1.reset();
        System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING BOARD STATE TO PLAYER 2 "); //! for debugging purposes
        toPlayer2.writeObject(boardStateMsg);
        toPlayer2.reset();
    }

    private void processMove(int x, int y)
    {
        copyBoardState(board, previousBoard); //? for KO situation

        board[x][y] = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;
    }

    //? this method will be called after each move. It will check for captured stones and remove them from the board
    private void checkForCapturedStones(int x, int y)
    {
        Stone playerStone = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;
        Stone opponentStone = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;

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
        whoseTurn = (whoseTurn == PlayerColors.BLACK) ? PlayerColors.WHITE : PlayerColors.BLACK;
    }

    //TODO: yet to be implemented
    private boolean isGameOver()
    {
        //TODO: Implement logic to determine if the game is over
        return false; // Placeholder
    }

    private boolean isMoveValid(int x, int y)
    {
        
        //? check for basic board bounds and empty space
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize || board[x][y] != Stone.EMPTY)
        {
            return false;
        }
    
        //TODO: check for suicide move
        if (isKoSituation(x, y) || isSuicideMove(x, y)) {
            return false;
        }
    
        return true;
    }
    
    private boolean isKoSituation(int x, int y)
    {
        Stone originalState = board[x][y];

        board[x][y] = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;

        boolean isKo = isBoardStateEqual(previousBoard, board);

        board[x][y] = originalState;

        return isKo;
    }

    private boolean isBoardStateEqual(Stone[][] board1, Stone[][] board2)
    {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board1[i][j] != board2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void copyBoardState(Stone[][] source, Stone[][] destination) 
    {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                destination[i][j] = source[i][j];
            }
        }
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
    //! END for debugging purposes


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
