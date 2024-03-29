package com.go_game.server;

import java.io.IOException;
import java.util.Random;

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
import shared.other.Logger;

/**
 * Represents a thread that handles a game session between a human player and a bot.
 * This class implements the Runnable interface and is designed to be run in a separate thread.
 * It manages communication between the human player and the bot.
 */
public class BotThread implements Runnable {
    private static int gameID = 0;
    private boolean gameOver = false;
    private volatile boolean running = true;

    private ClientConnection playerConnection;
    private GameLogic gameLogic;
    private Logger logger = new Logger();
    private Random random = new Random();
    private int boardSize;
    private int moveNumber;

    public BotThread(ClientConnection playerConnection, int boardSize) throws IOException {
        this.playerConnection = playerConnection;
        this.boardSize = boardSize;

        gameLogic = new GameLogic(boardSize);
        
        DBQueuer dbQueuer = new DBQueuer(new DBManager());
        gameID = dbQueuer.getHighestGameNumber() + 1;
        moveNumber = 0;

        playerConnection.sendMessage(new GameJoinedMsg(gameID, PlayerColors.BLACK, gameLogic.getWhoseTurn()));

        Thread thread = new Thread(this);
        thread.start();

        // TODO: Handle socket closure properly
    }

    //? For testing purposes
    public BotThread(ClientConnection playerConnection, GameLogic gameLogic, int boardSize) throws IOException {
        this.playerConnection = playerConnection;
        this.gameLogic = gameLogic;
        this.boardSize = boardSize;
        gameID++;
    }

    @Override
    public void run() {
        try {
            while (!isGameOver() && running)
            {
                logGameState();
                if (gameLogic.getWhoseTurn().equals(PlayerColors.BLACK))
                {
                    logger.log("PLAYER");
                    handlePlayerMove();
                } else {
                    logger.log("BOT MOVE");
                    handleBotMove();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log("Error in BotThread: " + e.getMessage());
            running = false;
            cleanupResources();
            Thread.currentThread().interrupt();
            logger.log("BotThread interrupted");
        }
    }
    private void cleanupResources()
    {
        try {
            playerConnection.getSocket().close();
        } catch (IOException e) {
            logger.log("Error closing socket: " + e.getMessage());
        }
        logger.log("Connection securely closed");
    }

    public void handlePlayerMove() throws ClassNotFoundException, IOException {
        MoveMsg moveMsg = (MoveMsg) playerConnection.receiveMessage();
        processMove(moveMsg);
    }

    public void handleBotMove() throws IOException {
        int attempts = 0;
        while (attempts < 10) {
            int x = random.nextInt(boardSize);
            int y = random.nextInt(boardSize);
            if (isMoveValid(x, y)) {
                gameLogic.processMove(x, y);
                gameLogic.captureStones(x, y);
                sendMessage(new BoardStateMsg(gameLogic.getBoard(), gameLogic.countCapturedStones(), gameID, moveNumber++));
                switchTurns();
                return;
            }
            attempts++;
        }
        // If 10 attempts fail, bot passes
        sendMessage(new PlayerPassedMsg(gameLogic.getWhoseTurn()));
        switchTurns();
    }

    private void processMove(MoveMsg moveMsg) throws IOException {
        int x = moveMsg.getX();
        int y = moveMsg.getY();

        if (moveMsg.getUnusualMove() == UnusualMove.RESIGN)
        {
            sendMessage(new PlayerResignedMsg("Player resigned", gameLogic.getWhoseTurn()));
            gameOver = true;
            return;
        }
        else if (moveMsg.playerPassed())
        {
            gameOver = true;
        }
        else if (isMoveValid(x, y)) {
            gameLogic.processMove(x, y);
            gameLogic.captureStones(x, y);
            switchTurns();
        }
        else
        {
            sendMessage(new MoveNotValidMsg(gameLogic.getWhoseTurn(), "Invalid move!"));
        }
    }

    public boolean isMoveValid(int x, int y) throws IOException {
        if (!gameLogic.isInBoundsAndEmptySpace(x, y) ||
            gameLogic.isKoSituation(x, y) ||
            gameLogic.isSuicideMove(x, y)) {
            return false;
        }
        return true;
    }

    private void switchTurns() {
        gameLogic.setWhoseTurn(gameLogic.getWhoseTurn().toggle());
    }

    public void sendMessage(AbstractMessage message) throws IOException {
        logger.log("Sending message: " + message.getType());
        playerConnection.sendMessage(message);
    }

    private void logGameState() {
        logger.say("\n\n####################### BOT GAME " + gameID + " #######################" + "\n" + "TURN: " + gameLogic.getWhoseTurn());
    }

    public boolean isGameOver() throws IOException, ClassNotFoundException
    {
        if (!gameOver)
        {
            return false;
        }
        int[] territoryScore = gameLogic.countTerritory();
        int[] capturedStones = gameLogic.countCapturedStones();
        String description = "Both players passed, negotiating results";

        sendMessage(new ResultsNegotiationMsg(description, territoryScore, capturedStones));

        ResultsNegotiationMsg playerResults = (ResultsNegotiationMsg) playerConnection.receiveMessage();
        int playerProposition = playerResults.getPlayerProposition();
        territoryScore[0] = playerProposition;

        playerConnection.sendMessage(new ResultsNegotiationMsg(territoryScore[1]));

        ResultsNegotiationMsg negotiationMsg = (ResultsNegotiationMsg) playerConnection.receiveMessage();


        if (negotiationMsg.getAgreement() == AgreementState.AGREE)
        {
            int[] finalTerritoryScore = new int[2];
            finalTerritoryScore[0]  = negotiationMsg.getPlayerProposition();
            float[] finalResults = gameLogic.calculateScore(finalTerritoryScore);

            sendMessage(new GameOverMsg("Game over, results agreed", finalResults));
            return true;
        }
        else
        {
            gameOver = false;
            sendMessage(new ResultsNegotiationMsg(AgreementState.DISAGREE, gameLogic.getWhoseTurn()));
            return false;
        }

    }

    public void setGameOver(boolean gameOver)
    {
        this.gameOver = gameOver;
    }

}

