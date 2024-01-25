package com.go_game.server;

import java.io.IOException;
import shared.enums.PlayerColors;
import shared.messages.AbstractMessage;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;
import shared.other.Logger;


/**
 * Represents a thread that handles a multiplayer game session between two players.
 * This class implements the Runnable interface and is designed to be run in a separate thread.
 * It manages communication between the players.
 * 
 * Sends messages:
 * - GameJoinedMsg()    - sent to both players when the game starts
 * - MoveNotValidMsg()  - sent to both players when a move is not valid
 * - PlayerPassedMsg()  - sent to both players when a player passes
 * - BoardStateMsg()    - sent to both players when a valid move is made
 * - GameOverMsg()      - sent to both players when the game is over
 * 
 * Receives messages:
 * - MoveMsg()          - sent by the player whose turn it is and is about to make a move
 * - OkMsg()            - sent by the other player
 */
public class MultiplayerGameThread implements Runnable
{
    private static int gameID = 0;
    private boolean previousWasPass = false;
    private boolean gameOver = false;

    private ClientConnection player1Connection;
    private ClientConnection player2Connection;
    private GameLogic gameLogic;

    private Logger logger = new Logger();

    public MultiplayerGameThread(ClientConnection player1Connection, ClientConnection player2Connection, int boardSize) throws IOException
    {

        this.player1Connection = player1Connection;
        this.player2Connection = player2Connection;

        gameLogic = new GameLogic(boardSize);
        gameID++;

        player1Connection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.BLACK, gameLogic.getWhoseTurn()));
        player2Connection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.WHITE, gameLogic.getWhoseTurn()));


        Thread fred = new Thread(this);
        fred.start();

        //TODO: close sockets
    }

    /**
     * Executes the game logic for a multiplayer game session.
     * Continuously runs until the game is over.
     * Receives moves from players, processes them, and updates the game state accordingly.
     * Sends messages to players to inform them about the game progress.
     * Handles exceptions related to I/O and class loading.
     */
    @Override
    public void run()
    {
        try
        {
            while(!isGameOver())
            {
                logGameState();
                MoveMsg moveMsg = receiveMove();
                if (moveMsg.playerPassed())
                {
                    //? player PASSED
                    if (previousWasPass)
                    {
                        //? both players passed -> game over
                        gameOver = true;
                        continue;
                    }
                    else
                    {
                        //? only one player passed -> switch turns
                        previousWasPass = true;
                        sendMessageToBothPlayers(new PlayerPassedMsg(gameLogic.getWhoseTurn()));
                        switchTurns();
                        continue;
                    }
                }
                else
                {
                    //? player DID NOT PASS
                    previousWasPass = false;
                }

                int x = moveMsg.getX();
                int y = moveMsg.getY();
                if(isMoveValid(x, y))
                {
                    //? move is VALID
                    gameLogic.processMove(x, y);
                    gameLogic.captureStones(x, y);
                    float[] scores = gameLogic.calculateScore();
                    logger.say("Black: " + scores[0] + ", White: " + scores[1]);
                    sendMessageToBothPlayers(new BoardStateMsg(gameLogic.getBoard()));
                    switchTurns();
                }
                else
                {
                    //? move is INVALID
                    //? info is already sent to players
                    continue;
                }

            }
        }
        //TODO: handle this exception correctly
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Error in MultiplayerGameThread: " + e.getMessage());
        }
    }

    /**
     * Receives a move from the player whose turn it is.
     * 
     * Used messages:
     * - MoveMsg()          - sent by the player whose turn it is and is about to make a move
     * - OkMsg()            - sent by the other player
     * 
     * @return MoveMsg object containing the move coordinates
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private MoveMsg receiveMove() throws ClassNotFoundException, IOException
    {
        MoveMsg moveMsg;
        if (!gameLogic.getWhoseTurn().equals(PlayerColors.WHITE)) 
        {
            logger.log("Player BLACK playing");
            moveMsg = (MoveMsg) player1Connection.receiveMessage();
            OkMsg okMsg = (OkMsg) player2Connection.receiveMessage();
        }
        else
        {
            logger.log("Player WHITE playing");
            moveMsg = (MoveMsg) player2Connection.receiveMessage();
            OkMsg okMsg = (OkMsg) player1Connection.receiveMessage();
        }
        return moveMsg;
    }

    /**
     * Sends a message to both players.
     * 
     * Used messages:
     * - MoveNotValidMsg()  - sent to both players when a move is not valid
     * - PlayerPassedMsg()  - sent to both players when a player passes
     * - BoardStateMsg()    - sent to both players when a valid move is made
     * - GameOverMsg()      - sent to both players when the game is over
     * 
     * @param message AbstractMessage object to be sent to both players
     * @throws IOException
     */
    private void sendMessageToBothPlayers(AbstractMessage message) throws IOException
    {
        logger.log("Sending message to both players: " + message.getType());
        player1Connection.sendMessage(message);
        player2Connection.sendMessage(message);
    }

    /**
     * Checks if a move is valid based on the given coordinates.
     * 
     * @param x The x-coordinate of the move.
     * @param y The y-coordinate of the move.
     * @return true if the move is valid, false otherwise.
     * @throws IOException if there is an error sending a message to the players.
     */
    private boolean isMoveValid(int x, int y) throws IOException
    {
        
        //? check for basic board bounds and empty space
        if (!gameLogic.isInBoundsAndEmptySpace(x, y))
        {
            sendMessageToBothPlayers(new MoveNotValidMsg(gameLogic.getWhoseTurn(), "Move not in bounds or not empty space!"));
            return false;
        }

        else if (gameLogic.isKoSituation(x, y))
        {
            sendMessageToBothPlayers(new MoveNotValidMsg(gameLogic.getWhoseTurn(), "Ko situation!"));
            return false;
        }
        else if (gameLogic.isSuicideMove(x, y))
        {
            sendMessageToBothPlayers(new MoveNotValidMsg(gameLogic.getWhoseTurn(), "Suicide move!"));
            return false;
        }
    
        return true;
    }

    /**
     * Checks if the game is over.
     * If the game is over, calculates the scores and sends a game over message to both players.
     * 
     * Used messages:
     * - GameOverMsg() - sent to both players when the game is over
     * 
     * @return true if the game is over, false otherwise.
     * @throws IOException if there is an error sending the game over message.
     */
    private boolean isGameOver() throws IOException
    {
        if (gameOver)
        {
            float[] scores = gameLogic.calculateScore();
            String description = "###Game Over###\nBlack: " + scores[0] + ", White: " + scores[1];
            PlayerColors winner = (scores[0] > scores[1]) ? PlayerColors.BLACK : PlayerColors.WHITE;
            sendMessageToBothPlayers(new GameOverMsg(description, winner, scores));
            return true;
        }
        return false;
    }

    /**
     * Logs the current ID of the game and players turn.
     */
    private void logGameState()
    {
        logger.say("\n\n####################### GAME " + gameID + " #######################" + "\n" + "TURN: " + gameLogic.getWhoseTurn());
    }

    /**
     * Switches the turns between players in the game.
     */
    private void switchTurns()
    {
        gameLogic.setWhoseTurn(gameLogic.getWhoseTurn().toggle());
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
