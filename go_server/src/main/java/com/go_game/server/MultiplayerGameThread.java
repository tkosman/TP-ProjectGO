package com.go_game.server;

import java.io.IOException;
import shared.enums.PlayerColors;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;
import shared.other.Logger;



//? This class will be responsible for game logic and will be an individual thread for each game session
public class MultiplayerGameThread implements Runnable
{
    private static int gameID = 0;
    private boolean previousWasPass = false;
    private boolean gameOver = false;

    private ClientConnection player1Connection;
    private ClientConnection player2Connection;
    private GameLogic gameLogic;

    private Logger logger = new Logger();

    //TODO: Change assumption that player 1 is always black and player 2 is always white

    public MultiplayerGameThread(ClientConnection player1Connection, ClientConnection player2Connection, int boardSize) throws IOException
    {

        this.player1Connection = player1Connection;
        this.player2Connection = player2Connection;

        gameLogic = new GameLogic(boardSize);
        gameID++;

        //! 4 OUT
        player1Connection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.BLACK, gameLogic.getWhoseTurn()));
        player2Connection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.WHITE, gameLogic.getWhoseTurn()));


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
                    logger.log("Player BLACK playing"); //! for debugging purposes
                    moveMsg = (MoveMsg) player1Connection.receiveMessage();
                    logger.say(moveMsg); //! for debugging purposes
                    OkMsg okMsg = (OkMsg) player2Connection.receiveMessage();
                }
                else
                {
                    //! 1 IN +++++++++ -> Player 2 playing and player 1 sending OK
                    logger.log("Player WHITE playing"); //! for debugging purposes
                    moveMsg = (MoveMsg) player2Connection.receiveMessage();
                    OkMsg okMsg = (OkMsg) player1Connection.receiveMessage();
                }

                if (moveMsg.playerPassed())
                {
                    logger.log("PLAYER PASSED\n"); //! for debugging purposes
                    if (previousWasPass)
                    {
                        //! 2 OUT ##########
                        logger.log(" GAME OVER\n"); //! for debugging purposes

                        gameOver = true;

                        continue;
                    }
                    else
                    {
                        previousWasPass = true;
                        player1Connection.sendMessage(new PlayerPassedMsg(gameLogic.getWhoseTurn()));
                        player2Connection.sendMessage(new PlayerPassedMsg(gameLogic.getWhoseTurn()));
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
                        logger.log("INVALID MOVE BY PLAYER 1\n"); //! for debugging purposes
                        player1Connection.sendMessage(new MoveNotValidMsg(1));
                        player2Connection.sendMessage(new MoveNotValidMsg(1));
                        
                    }
                    else 
                    {
                        //! 2 OUT ##########
                        logger.log(" INVALID MOVE BY PLAYER 2\n"); //! for debugging purposes
                        player1Connection.sendMessage(new MoveNotValidMsg(2));
                        player2Connection.sendMessage(new MoveNotValidMsg(2));
                    }
                }
                else
                {
                    gameLogic.processMove(x, y);
                    gameLogic.captureStones(x, y);

                    float[] scores = gameLogic.calculateScore();
                    logger.say("Black: " + scores[0] + ", White: " + scores[1]);

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
        logger.log("SENDING BOARD STATE TO PLAYER 1"); //! for debugging purposes
        player1Connection.sendMessage(boardStateMsg);
        logger.log("SENDING BOARD STATE TO PLAYER 2 "); //! for debugging purposes
        player2Connection.sendMessage(boardStateMsg);
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
                player1Connection.sendMessage(new GameOverMsg(resultMessage, PlayerColors.BLACK));
                player2Connection.sendMessage(new GameOverMsg(resultMessage, PlayerColors.WHITE));
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
