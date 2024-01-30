package com.go_game.server;

import java.util.ArrayList;
import java.util.Arrays;

import shared.enums.PlayerColors;
import shared.enums.Stone;

public class GameLogic
{
    private Stone[][] board;
    private Stone[][] previousBoard;
    private PlayerColors whoseTurn;
    private int[] capturedStones;
    private int boardSize;

    public GameLogic(int boardSize)
    {
        this.boardSize = boardSize;
        initializeBoard();
        whoseTurn = PlayerColors.BLACK;
        capturedStones = new int[2];
    }


    public void processMove(int x, int y)
    {
        previousBoard = copyBoard(board);
        board[x][y] = whoseTurn.toStone();
    }

    //? this method will be called after each move. It will check for captured stones and remove them from the board
    public void captureStones(int x, int y)
    {
        // Stone playerStone = whoseTurn.toStone();
        Stone opponentStone = whoseTurn.getOpposite().toStone();

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

                if (whoseTurn == PlayerColors.BLACK) {
                    capturedStones[0]++; // Black captures a white stone
                } else {
                    capturedStones[1]++; // White captures a black stone
                }
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

    public boolean isInBoundsAndEmptySpace(int x, int y)
    {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize || board[x][y] != Stone.EMPTY)
            return false;
        return true;
    }
    
    public boolean isKoSituation(int x, int y)
    {
        if (previousBoard == null)
        {
            return false;
        }

        Stone[][] saveBoard = copyBoard(board);
        board[x][y] = whoseTurn.toStone();
        captureStones(x, y);
        boolean isKo = isBoardStateEqual(previousBoard, board);
        if(!isKo)
        {
            board = saveBoard;
            return false;
        }
        board = saveBoard;
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

    public boolean isSuicideMove(int x, int y)
    {
        if (isOutOfBoard(x, y) || board[x][y] != Stone.EMPTY)
        {
            return false;
        }
        Stone[][] saveBoard = copyBoard(board);

        Stone currentPlayerStone = whoseTurn.toStone();
        board[x][y] = currentPlayerStone;
        boolean captures = capturesOpponent(x, y, currentPlayerStone);
        boolean hasLiberties = hasLiberty(x, y);

        board = saveBoard;

        return !captures && !hasLiberties;
    }
    private boolean hasLiberty(int x, int y) 
    {
        Stone stoneColor = board[x][y];
        boolean[][] visited = new boolean[boardSize][boardSize];
        return checkGroupLiberties(x, y, stoneColor, visited);
    }
    private boolean checkGroupLiberties(int x, int y, Stone stoneColor, boolean[][] visited) {
        if (isOutOfBoard(x, y) || visited[x][y]) {
            return false;
        }
        if (board[x][y] != stoneColor && board[x][y] != Stone.EMPTY) {
            return false;
        }
        if (board[x][y] == Stone.EMPTY) {
            return true;
        }
        visited[x][y] = true;
        boolean hasLiberty = false;
        hasLiberty |= checkGroupLiberties(x + 1, y, stoneColor, visited);
        hasLiberty |= checkGroupLiberties(x - 1, y, stoneColor, visited);
        hasLiberty |= checkGroupLiberties(x, y + 1, stoneColor, visited);
        hasLiberty |= checkGroupLiberties(x, y - 1, stoneColor, visited);
    
        return hasLiberty;
    }
    
    private boolean capturesOpponent(int x, int y, Stone currentPlayerStone)
    {
        Stone opponentStone = whoseTurn.getOpposite().toStone();
        return checkCapture(x + 1, y, opponentStone) ||
                checkCapture(x - 1, y, opponentStone) ||
                checkCapture(x, y + 1, opponentStone) ||
                checkCapture(x, y - 1, opponentStone);
    }
    private boolean checkCapture(int x, int y, Stone opponentStone)
    {
        if (isOutOfBoard(x, y) || board[x][y] != opponentStone)
        {
            return false;
        }
    
        ArrayList<Point> group = new ArrayList<>();
        ArrayList<Point> liberties = new ArrayList<>();
        findGroupWithLiberties(x, y, opponentStone, group, liberties);
        if (liberties.isEmpty()) {
            for (Point stone : group) {
                board[stone.x][stone.y] = Stone.EMPTY;
            }
            return true;
        }
        return false;
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

    public int[] countTerritory()
    {
        boolean[][] visited = new boolean[boardSize][boardSize];
        int[] territory = new int[2];
        for (int x = 0; x < boardSize; x++)
        {
            for (int y = 0; y < boardSize; y++)
            {
                if (!visited[x][y] && board[x][y] == Stone.EMPTY)
                {
                    ArrayList<Point> area = new ArrayList<>();
                    PlayerColors owner = findTerritoryOwner(x, y, visited, area);
                    if (owner != null)
                    {
                        territory[owner == PlayerColors.BLACK ? 0 : 1] += area.size();
                    }
                    //? unresolved territory is not counted to the score
                }
            }
        }
        return territory;
    }

    private PlayerColors findTerritoryOwner(int x, int y, boolean[][] visited, ArrayList<Point> area)
    {
        if (isOutOfBoard(x, y) || visited[x][y])
        {
            return null;
        }
        visited[x][y] = true;
        if (board[x][y] != Stone.EMPTY)
        {
            return getPlayerColor(board[x][y]);
        }
        area.add(new Point(x, y));

        PlayerColors ownerNorth = findTerritoryOwner(x, y + 1, visited, area);
        PlayerColors ownerSouth = findTerritoryOwner(x, y - 1, visited, area);
        PlayerColors ownerEast = findTerritoryOwner(x + 1, y, visited, area);
        PlayerColors ownerWest = findTerritoryOwner(x - 1, y, visited, area);

        PlayerColors determinedOwner = ownerNorth;
        if (determinedOwner != null && ownerSouth != null && determinedOwner != ownerSouth) return null;
        if (determinedOwner != null && ownerEast != null && determinedOwner != ownerEast) return null;
        if (determinedOwner != null && ownerWest != null && determinedOwner != ownerWest) return null;

        return determinedOwner;
    }

    private PlayerColors getPlayerColor(Stone stone) {
        return stone.toPlayerColors();
    }

    public int[] countCapturedStones()
    {
        return capturedStones;
    }

    public float[] calculateScore(int[] territoryScore)
    {
        float[] score = new float[2];
        score[0] = territoryScore[0] + capturedStones[0];
        score[1] = territoryScore[1] + capturedStones[1];
        score[1] += 6.5; //? Komi
        return score;
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

    public void setPreviousBoard(Stone[][] previousBoard)
    {
        this.previousBoard = previousBoard;
    }

    public Stone[][] getPreviousBoard()
    {
        return previousBoard;
    }

    public PlayerColors getWhoseTurn() {
        return whoseTurn;
    }

    public void setWhoseTurn(PlayerColors whoseTurn) {
        this.whoseTurn = whoseTurn;
    }

    public void setCapturedStones(int[] capturedStones) {
        this.capturedStones = capturedStones;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    // private void printBoard(Stone[][] boardlocal)
    // {
    //     // System.out.print("\033[H\033[2J");  
    //     // System.out.flush();  
    //     for (int y = 0; y < boardSize; y++) {
    //         for (int x = 0; x < boardSize; x++) {
    //             switch (boardlocal[x][y]) {
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
}
