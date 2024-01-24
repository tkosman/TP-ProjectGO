package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import shared.enums.BoardSize;
import shared.enums.PlayerColors;
import shared.enums.Stone;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;



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
    private PlayerColors whoseTurn = PlayerColors.BLACK;
    private boolean previousWasPass = false;
    private int[] capturedStones = new int[2]; // Index 0 for Black, 1 for White
    private boolean gameOver = false;

    //! I assume that player 1 is always black and player 2 is always white


    //? constructor for tests
    public GameLogicThread(int boardSize)
    {
        this.boardSize = boardSize;
        initializeBoard();
        gameID++;
    }

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
                        toPlayer1.writeObject(new PlayerPassedMsg(whoseTurn));
                        toPlayer2.reset();
                        toPlayer2.writeObject(new PlayerPassedMsg(whoseTurn));
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
                    captureStones(x, y);

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
        board[x][y] = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;
        previousBoard = copyBoard(board);
    }

    //? this method will be called after each move. It will check for captured stones and remove them from the board
    private void captureStones(int x, int y)
    {
        Stone playerStone = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;
        Stone opponentStone = (whoseTurn == PlayerColors.BLACK) ? Stone.WHITE : Stone.BLACK;

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

                // if (whoseTurn == PlayerColors.BLACK) {
                //     capturedStones[0]++; // Black captures a white stone
                // } else {
                //     capturedStones[1]++; // White captures a black stone
                // }
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

    private boolean isGameOver() 
    {
        if (gameOver) {
            int[] scores = calculateScore();
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
    

    private boolean isMoveValid(int x, int y)
    {
        
        //? check for basic board bounds and empty space
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize || board[x][y] != Stone.EMPTY)
        {
            return false;
        }
    
        if (isKoSituation(x, y) || isSuicideMove(x, y)) {
            return false;
        }
    
        return true;
    }
    
    private boolean isKoSituation(int x, int y)
    {
        Stone[][] saveBoard = copyBoard(board);
        board[x][y] = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;
        captureStones(x, y);
        boolean isKo = isBoardStateEqual(previousBoard, board);
        if(!isKo)
        {
            board = saveBoard;
            return false;
        }
        return true;
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

    private Stone[][] copyBoard(Stone[][] source)
    {
        Stone[][] destination = new Stone[source.length][];
        for (int i = 0; i < source.length; i++)
        {
            destination[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return destination;
    }
    
    private boolean isSuicideMove(int x, int y) {
        if (isOutOfBoard(x, y) || board[x][y] != Stone.EMPTY) {
            return false;
        }
    
        Stone currentPlayerStone = (whoseTurn == PlayerColors.BLACK) ? Stone.BLACK : Stone.WHITE;
        board[x][y] = currentPlayerStone;
    
        boolean captures = capturesOpponent(x, y, currentPlayerStone);
    
        boolean hasLiberty = hasLiberty(x, y, currentPlayerStone);
    
        board[x][y] = Stone.EMPTY;
    
        return !captures && !hasLiberty;
    }

    private boolean hasLiberty(int x, int y, Stone stone) {
        boolean[][] visited = new boolean[boardSize][boardSize];
        return checkLiberty(x, y, stone, visited);
    }
    
    private boolean checkLiberty(int x, int y, Stone stone, boolean[][] visited) {
        if (isOutOfBoard(x, y) || visited[x][y]) {
            return false;
        }
    
        visited[x][y] = true;
    
        if (board[x][y] == Stone.EMPTY) {
            return true;
        } else if (board[x][y] != stone) {
            return false;
        }
    
        // Check all adjacent points
        return checkLiberty(x + 1, y, stone, visited) ||
               checkLiberty(x - 1, y, stone, visited) ||
               checkLiberty(x, y + 1, stone, visited) ||
               checkLiberty(x, y - 1, stone, visited);
    }
    
    
    private boolean capturesOpponent(int x, int y, Stone currentPlayerStone) {
        Stone opponentStone = (currentPlayerStone == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
        return checkCapture(x + 1, y, opponentStone) ||
               checkCapture(x - 1, y, opponentStone) ||
               checkCapture(x, y + 1, opponentStone) ||
               checkCapture(x, y - 1, opponentStone);
    }
    
    private boolean checkCapture(int x, int y, Stone opponentStone) {
        if (isOutOfBoard(x, y) || board[x][y] != opponentStone) {
            return false;
        }
    
        ArrayList<Point> group = new ArrayList<>();
        ArrayList<Point> liberties = new ArrayList<>();
        findGroupWithLiberties(x, y, opponentStone, group, liberties);
        return liberties.isEmpty();
    }
    

    //! for debugging purposes
    private static void printBoard(Stone[][] board) {
        int boardSize = board.length;
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

    private TerritoryResult checkTerritory(int x, int y, boolean[][] visited) {
        if (isOutOfBoard(x, y) || visited[x][y]) {
            return new TerritoryResult(null, 0);
        }
    
        visited[x][y] = true;
        if (board[x][y] != Stone.EMPTY) {
            return new TerritoryResult(getPlayerColor(board[x][y]), 0);
        }
    
        TerritoryResult result = new TerritoryResult(null, 1);
        TerritoryResult[] adjacentResults = {
            checkTerritory(x + 1, y, visited),
            checkTerritory(x - 1, y, visited),
            checkTerritory(x, y + 1, visited),
            checkTerritory(x, y - 1, visited)
        };
    
        for (TerritoryResult adjacentResult : adjacentResults) {
            if (adjacentResult.owner != null) {
                if (result.owner == null) {
                    result.owner = adjacentResult.owner;
                } else if (result.owner != adjacentResult.owner) {
                    result.owner = null;
                    break;
                }
            }
            result.size += adjacentResult.size;
        }
    
        return result;
    }

    private PlayerColors getPlayerColor(Stone stone) {
        return (stone == Stone.BLACK) ? PlayerColors.BLACK : PlayerColors.WHITE;
    }
    
    //TODO: to be refactored
    private class TerritoryResult 
    {
        PlayerColors owner;
        int size;
    
        TerritoryResult(PlayerColors owner, int size) {
            this.owner = owner;
            this.size = size;
        }
    }
    
    private int[] calculateTerritory() {
        boolean[][] visited = new boolean[boardSize][boardSize];
        int blackTerritory = 0;
        int whiteTerritory = 0;
    
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (board[x][y] == Stone.EMPTY && !visited[x][y]) {
                    TerritoryResult result = checkTerritory(x, y, visited);
                    if (result.owner == PlayerColors.BLACK) {
                        blackTerritory += result.size;
                    } else if (result.owner == PlayerColors.WHITE) {
                        whiteTerritory += result.size;
                    }
                }
            }
        }
        return new int[] {blackTerritory, whiteTerritory};
    }

    private int[] calculateScore() {
        int[] territoryScores = calculateTerritory();
        int blackScore = territoryScores[0]; // Black's territory
        int whiteScore = territoryScores[1]; // White's territory

        blackScore += capturedStones[PlayerColors.BLACK.ordinal()];
        whiteScore += capturedStones[PlayerColors.WHITE.ordinal()];
    
        return new int[] {blackScore, whiteScore};
    }




    //! Getters and setters
    
    public Stone[][] getBoard()
    {
        return board;
    }

    public void setBoard(Stone[][] board)
    {
        this.board = board;
    }

    public void doProcessMove(int x, int y)
    {
        processMove(x, y);
    }

    public void setPreviousBoard(Stone[][] previousBoard)
    {
        this.previousBoard = previousBoard;
    }

    public Stone[][] getPreviousBoard()
    {
        return previousBoard;
    }

    public boolean getIsMoveValid(int x, int y)
    {
        return isMoveValid(x, y);
    }

    public void setWhoseTurn(PlayerColors whoseTurn) {
        this.whoseTurn = whoseTurn;
    }

    public boolean testIsKoSituation(int x, int y) {
        return isKoSituation(x, y);
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public void testCaptureStones(int x, int y) {
        captureStones(x, y);
    }
}
