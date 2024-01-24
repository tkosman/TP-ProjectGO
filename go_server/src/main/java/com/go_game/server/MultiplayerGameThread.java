package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;

import shared.enums.PlayerColors;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;



//? This class will be responsible for game logic and will be an individual thread for each game session
public class MultiplayerGameThread implements Runnable
{
    private ObjectOutputStream toPlayer1;
    private ObjectInputStream fromPlayer1;
    private ObjectOutputStream toPlayer2;
    private ObjectInputStream fromPlayer2;
    private static int gameID = 0;
    private boolean previousWasPass = false;
    private boolean gameOver = false;

    private GameLogic gameLogic;

    //! I assume that player 1 is always black and player 2 is always white

    //TODO: pass socket to close it later
    public MultiplayerGameThread(ClientConnection player1Connection, ClientConnection player2Connection, int boardSize) throws IOException
    {
        this.toPlayer1 = player1Connection.getOutputStream();
        this.fromPlayer1 = player1Connection.getInputStream();
        this.toPlayer2 = player2Connection.getOutputStream();
        this.fromPlayer2 = player2Connection.getInputStream();

        gameLogic = new GameLogic(boardSize);
        gameID++;

        //! 4 OUT
        toPlayer1.writeObject(new GameJoinedMsg(gameID, PlayerColors.BLACK, gameLogic.getWhoseTurn()));
        toPlayer1.reset();
        toPlayer2.writeObject(new GameJoinedMsg(gameID, PlayerColors.WHITE, gameLogic.getWhoseTurn()));
        toPlayer2.reset();


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
                System.out.println("TURN: " + gameLogic.getWhoseTurn());

                MoveMsg moveMsg;
                if (!gameLogic.getWhoseTurn().equals(PlayerColors.WHITE)) 
                {
                    //! 1 IN +++++++++ -> Player 1 playing and player 2 sending OK
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " Player BLACK playing"); //! for debugging purposes
                    moveMsg = (MoveMsg) fromPlayer1.readObject();
                    System.out.println(moveMsg); //! for debugging purposes
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
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " PLAYER PASSED\n"); //! for debugging purposes
                    if (previousWasPass)
                    {
                        //! 2 OUT ##########
                        System.out.println(new Timestamp(System.currentTimeMillis()) + " GAME OVER\n"); //! for debugging purposes

                        gameOver = true;

                        continue;
                    }
                    else
                    {
                        previousWasPass = true;
                        toPlayer1.writeObject(new PlayerPassedMsg(gameLogic.getWhoseTurn()));
                        toPlayer2.reset();
                        toPlayer2.writeObject(new PlayerPassedMsg(gameLogic.getWhoseTurn()));
                        switchTurns();
                        continue;
                    }
                }
                else
                {
                    previousWasPass = false;
                }

                int x = moveMsg.getX();
                int y = moveMsg.getY();

                if (!isMoveValid(x, y))
                {
                    //? move is INVALID
                    //? sending info to player that the move is INVALID he needs to repeat it
                    if (gameLogic.getWhoseTurn().equals(PlayerColors.BLACK))
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
                    gameLogic.processMove(x, y);
                    gameLogic.captureStones(x, y);

                    float[] scores = gameLogic.calculateScore();
                    System.out.println("Black: " + scores[0] + ", White: " + scores[1]);

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
        BoardStateMsg boardStateMsg = new BoardStateMsg(gameLogic.getBoard());

        //! 2 ########## OUT -> Sending board state to players
        System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING BOARD STATE TO PLAYER 1"); //! for debugging purposes
        toPlayer1.writeObject(boardStateMsg);
        toPlayer1.reset();
        System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING BOARD STATE TO PLAYER 2 "); //! for debugging purposes
        toPlayer2.writeObject(boardStateMsg);
        toPlayer2.reset();
    }

    
    private boolean isMoveValid(int x, int y)
    {
        
        //? check for basic board bounds and empty space
        if (!gameLogic.isInBoundsAndEmptySpace(x, y))
        {
            return false;
        }

        else if (gameLogic.isKoSituation(x, y))
        {
            System.out.println("isKoSituation");
            return false;
        }
        else if (gameLogic.isSuicideMove(x, y))
        {
            System.out.println("isSuicideMove");
            return false;
        }
    
        return true;
    }

    private void switchTurns()
    {
        gameLogic.setWhoseTurn(gameLogic.getWhoseTurn().toggle());
    }

    private boolean isGameOver() 
    {
        if (gameOver) {
            float[] scores = gameLogic.calculateScore();
            String resultMessage = "###Game Over###\nBlack: " + scores[0] + ", White: " + scores[1];
            try {
                toPlayer1.writeObject(new GameOverMsg(resultMessage, PlayerColors.BLACK));
                toPlayer2.writeObject(new GameOverMsg(resultMessage, PlayerColors.WHITE));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    //! for debugging purposes
    // private static void printBoard(Stone[][] board) {
    //     int boardSize = board.length;
    //     for (int y = 0; y < boardSize; y++) {
    //         for (int x = 0; x < boardSize; x++) {
    //             switch (board[x][y]) {
    //                 case BLACK:
    //                     System.out.print("B ");
    //                     break;
    //                 case WHITE:
    //                     System.out.print("W ");
    //                     break;
    //                 default:
    //                     System.out.print(". ");
    //                     break;
    //             }
    //         }
    //         System.out.println();
    //     }
    //     System.out.println();
    //     System.out.println();
    // }
    //! END for debugging purposes
}
