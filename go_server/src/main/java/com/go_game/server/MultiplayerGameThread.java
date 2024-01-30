package com.go_game.server;

import java.io.IOException;

import shared.db.DBManager;
import shared.db.DBQueuer;
import shared.enums.AgreementState;
import shared.enums.PlayerColors;
import shared.enums.UnusualMove;
import shared.messages.AbstractMessage;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.PlayerPassedMsg;
import shared.messages.PlayerResignedMsg;
import shared.messages.ResultsNegotiationMsg;
import shared.messages.SthWentWrongMsg;
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
    private int moveNumber;

    private Logger logger = new Logger();

    public MultiplayerGameThread(ClientConnection player1Connection, ClientConnection player2Connection, int boardSize)
    {

        this.player1Connection = player1Connection;
        this.player2Connection = player2Connection;

        gameLogic = new GameLogic(boardSize);
        DBQueuer dbQueuer = new DBQueuer(new DBManager());

        gameID = dbQueuer.getHighestGameNumber() + 1;
        moveNumber = 0;


        if(handshake())
        {
            Thread fred = new Thread(this);
            fred.start();
        }
    }

    /**
     * Executes the game logic for a multiplayer game session.
     * Continuously runs until the game is over.
     * Receives moves from players, processes them, and updates the game state accordingly.
     * Sends messages to players to inform them about the game progress.
     * Handles exceptions related to I/O and class loading.
     *
     * @throws IOException if an I/O error occurs during communication with the players.
     * @throws ClassNotFoundException if a class cannot be found during deserialization of messages.
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
                if (moveMsg.getUnusualMove() != UnusualMove.RESIGN)
                {
                    //? player RESIGNED
                    gameOver = true;
                    sendMessageToBothPlayers(new PlayerResignedMsg("Player resigned", gameLogic.getWhoseTurn()));
                    continue;
                }
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

                    sendMessageToBothPlayers(new BoardStateMsg(gameLogic.getBoard(), gameLogic.countCapturedStones(), gameID, moveNumber++));
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
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("ERROR: SOMETHING WENT WRONG IN GAME " + gameID + " FLOW");
            closeConnection(player1Connection, "Something went wrong in game flow");
            closeConnection(player2Connection, "Something went wrong in game flow");
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
            //? Should receive OkMsg
            player2Connection.receiveMessage();
        }
        else
        {
            logger.log("Player WHITE playing");
            moveMsg = (MoveMsg) player2Connection.receiveMessage();
            //? Should receive OkMsg
            player1Connection.receiveMessage();
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
        System.out.println(!gameLogic.isInBoundsAndEmptySpace(x, y));
        System.out.println((gameLogic.isKoSituation(x, y)));
        System.out.println( (gameLogic.isSuicideMove(x, y)));
        
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
     * Checks if the game is over and handles the negotiation of results between players.
     * If both players agree on the results, sends the final results and ends the game.
     * If players disagree, continues the game with the first player who disagreed.
     * 
     * Sends messages:
     * - ResultsNegotiationMsg(desc, teritory, capturedStones)  - sends calculated results
     * - ResultsNegotiationMsg(playerXproposition)              - sends results proposed by opponent
     * - GameOverMsg()                                          - sends final results and allows to calculate 
     *
     * @return true if the game is over, false otherwise.
     * @throws IOException            if there is an I/O error while sending or receiving messages.
     * @throws ClassNotFoundException if the received message cannot be cast to the expected type.
     */
    private boolean isGameOver() throws IOException, ClassNotFoundException
    {
        if (!gameOver)
        {
            return false;
        }
        int[] territoryScore = gameLogic.countTerritory();
        int[] capturedStones = gameLogic.countCapturedStones();
        String description = "Both players passed, negotiating results";

        //? send calculated results to BOTH players
        sendMessageToBothPlayers(new ResultsNegotiationMsg(description, territoryScore, capturedStones));

        //? get from both players their suggested results
        ResultsNegotiationMsg player1Results = (ResultsNegotiationMsg) player1Connection.receiveMessage();
        ResultsNegotiationMsg player2Results = (ResultsNegotiationMsg) player2Connection.receiveMessage();
        int player1Proposition = player1Results.getPlayerProposition();
        int player2Proposition = player2Results.getPlayerProposition();

        //? send results to BOTH opposite players
        player1Connection.sendMessage(new ResultsNegotiationMsg(player2Proposition));
        player2Connection.sendMessage(new ResultsNegotiationMsg(player1Proposition));
        
        //? receive approval from BOTH players
        ResultsNegotiationMsg player1Approval = (ResultsNegotiationMsg) player1Connection.receiveMessage();
        ResultsNegotiationMsg player2Approval = (ResultsNegotiationMsg) player2Connection.receiveMessage();
        
        if (player1Approval.getAgreement() == AgreementState.AGREE && player2Approval.getAgreement() == AgreementState.AGREE)
        {
            int[] finalTerritoryScore = new int[2];
            finalTerritoryScore[0]  = player1Proposition;
            finalTerritoryScore[1]  = player2Proposition;

            float[] finalResults = gameLogic.calculateScore(finalTerritoryScore);

            sendMessageToBothPlayers(new GameOverMsg("Game over, results agreed", finalResults));
            return true;
        }
        else
        {
            if (player1Approval.getAgreement() == AgreementState.DISAGREE)
            {
                //? player 1 disagreed
                sendMessageToBothPlayers(new ResultsNegotiationMsg(AgreementState.DISAGREE, PlayerColors.WHITE));
                gameLogic.setWhoseTurn(PlayerColors.WHITE);
            }
            else if (player2Approval.getAgreement() == AgreementState.DISAGREE)
            {
                //? player 2 disagreed
                sendMessageToBothPlayers(new ResultsNegotiationMsg(AgreementState.DISAGREE, PlayerColors.BLACK));
                gameLogic.setWhoseTurn(PlayerColors.BLACK);
            }
            else 
            {
                //? both disagreed
                sendMessageToBothPlayers(new ResultsNegotiationMsg(AgreementState.DISAGREE, gameLogic.getWhoseTurn()));
            }
            gameOver = false;
            return false;
        }            
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


    /**
     * Sends a message to both players when the game starts.
     * 
     * Used messages:
     * - GameJoinedMsg()    - sent to both players when the game starts
     * 
     * @return true if the handshake was successful, false otherwise.
     */
    private boolean handshake()
    {
        try 
        {
            player1Connection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.BLACK, gameLogic.getWhoseTurn()));
        } 
        catch (IOException e) 
        {
            System.out.println("ERROR: PLAYER BLACK DISCONNECTED, GAME " + gameID + " ENDED");
            closeConnection(player2Connection, "Player 1 connection error");
            return false;
        }
        try 
        {
            player2Connection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.WHITE, gameLogic.getWhoseTurn()));
        }
        catch (IOException e) 
        {
            System.out.println("ERROR: PLAYER WHITE DISCONNECTED, GAME " + gameID + " ENDED");
            closeConnection(player1Connection, "Player 1 connection error");
            return false;
        }

        return true;
    }

    /**
     * Closes the connection with the given client and prints the given error message.
     * 
     * Used messages:
     * - SthWentWrongMsg()  - sent to the client when the connection is closed
     * 
     * @param connection    ClientConnection object representing the connection to be closed.
     * @param errorMessage  String object representing the error message to be printed.
     */
    private void closeConnection(ClientConnection connection, String errorMessage)
    {
        try
        {
            connection.sendMessage(new SthWentWrongMsg(errorMessage));
            System.exit(0);
        } 
        catch (IOException e)
        {} 
    }
}
