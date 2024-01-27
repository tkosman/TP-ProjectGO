package com.go_game.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import shared.enums.PlayerColors;
import shared.enums.Stone;

public class GameLogicTest {
    private GameLogic gameLogic;

    @BeforeEach
    void setUp() throws IOException
    {
        gameLogic = new GameLogic(9);
    }

    // public static void main(String[] args)
    // {
    //     testSuicideMoveThree();
    // }

    @Test
    void testBoardInitialization()
    {
        Stone[][] board = gameLogic.getBoard(); // Assuming getBoard() is a public method in gameLogic
        assertEquals(9, board.length); // Check if the board length is correct for 9x9 board
        assertEquals(9, board[0].length); // Check if the width of the board is correct
        // Check if all positions are initialized to Stone.EMPTY
        for (int i = 0; i < board.length; i++) 
        {
            for (int j = 0; j < board[i].length; j++)
            {
                assertEquals(Stone.EMPTY, board[i][j]);
            }
        }
    }

    @Test
    void testValidMoveProcessing() 
    {
        for (int i = 0; i < 9; i++) 
        {
            for (int j = 0; j < 9; j++)
            {
                gameLogic.processMove(i, j);
                Stone[][] board = gameLogic.getBoard();
                assertEquals(Stone.BLACK, board[i][j], "After the first move, the position should have a BLACK stone");
            }
        }
    }

    @Test
    void testInvalidMoveProcessing()
    {
        gameLogic.processMove(4, 4);

        assertFalse(gameLogic.isInBoundsAndEmptySpace(4, 4), "Move should be invalid for an already occupied position");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(-1, 4), "Move should be invalid for out-of-bounds positions");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(9, 9), "Move should be invalid for out-of-bounds positions");
    }

    @Test
    public void testCaptureSingleStone() 
    {
        gameLogic = new GameLogic(3);
        gameLogic.setBoard(decodeBoardString(".B.\\BWB\\.B."));
        gameLogic.setWhoseTurn(PlayerColors.BLACK);
        gameLogic.captureStones(2, 1);
        assertEquals(gameLogic.getBoard()[1][1], Stone.EMPTY, "The stone at (1,1) should be captured and thus EMPTY");
    }

    @Test
    public void testCaptureSingleStoneDifferentScenario()
    {
        GameLogic gameLogic = new GameLogic(5);
        gameLogic.setBoard(decodeBoardString(".....\\..B\\.BWB\\..B\\....."));
        gameLogic.setWhoseTurn(PlayerColors.BLACK);
        gameLogic.captureStones(2, 1);
        assertEquals(gameLogic.getBoard()[2][2], Stone.EMPTY, "The white stone should be captured");
    }

    @Test
    public void testCaptureMultipleStones() 
    {
        gameLogic = new GameLogic(5);
        gameLogic.setBoard(decodeBoardString(".BB..\\BWWB.\\BWWB.\\BWWB.\\.BB..\\"));
        gameLogic.setWhoseTurn(PlayerColors.BLACK);
        gameLogic.captureStones(2, 0);

        assertAll("Multiple stones should be captured",
            () -> assertEquals(gameLogic.getBoard()[1][1], Stone.EMPTY, "Expected the stone at (1,1) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogic.getBoard()[1][2], Stone.EMPTY, "Expected the stone at (1,2) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogic.getBoard()[1][3], Stone.EMPTY, "Expected the stone at (1,3) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogic.getBoard()[2][1], Stone.EMPTY, "Expected the stone at (2,1) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogic.getBoard()[2][2], Stone.EMPTY, "Expected the stone at (2,2) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogic.getBoard()[2][3], Stone.EMPTY, "Expected the stone at (2,3) to be captured and thus EMPTY")
        );
    }

    @Test
    public void testCaptureMultipleStonesDifferentScenario()
    {
        gameLogic = new GameLogic(5);
        gameLogic.setBoard(decodeBoardString(".....\\.BBB.\\BWWB.\\BWWB.\\.BB..\\"));
        gameLogic.setWhoseTurn(PlayerColors.BLACK);
        gameLogic.captureStones(3, 3);

        assertAll("Multiple stones in a different scenario should be captured",
            () -> assertEquals(gameLogic.getBoard()[1][2], Stone.EMPTY, "The white stone at (1,2) should be captured"),
            () -> assertEquals(gameLogic.getBoard()[1][3], Stone.EMPTY, "The white stone at (1,3) should be captured"),
            () -> assertEquals(gameLogic.getBoard()[2][2], Stone.EMPTY, "The white stone at (2,2) should be captured"),
            () -> assertEquals(gameLogic.getBoard()[2][3], Stone.EMPTY, "The white stone at (2,3) should be captured")
        );
    }

    @Test
    public void testInBoundsAndEmptySpace()
    {
        gameLogic = new GameLogic(5);
        gameLogic.setBoard(decodeBoardString(".BB..\\BWWB.\\BWWB.\\BWWB.\\.BB..\\"));
        assertTrue(gameLogic.isInBoundsAndEmptySpace(0, 0), "The position (0,0) should be in bounds and empty");
        assertTrue(gameLogic.isInBoundsAndEmptySpace(4, 4), "The position (4,4) should be in bounds and empty");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(5, 5), "The position (5,5) should be out of bounds");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(4, 5), "The position (5,5) should be out of bounds");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(-1, -1), "The position (-1,-1) should be out of bounds");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(4, -1), "The position (-1,-1) should be out of bounds");
        assertFalse(gameLogic.isInBoundsAndEmptySpace(2, 1), "The position (2,1) should be occupied");
    }

    @Test
    public void testIsKoSituationOne()
    {
        gameLogic = new GameLogic(4);
        String boardSetup =  ".BW.\\"
                            + "B.BW\\"
                            + ".BW.\\"
                            + "....\\";
        gameLogic.setPreviousBoard(decodeBoardString(boardSetup));

        Stone[][] boardAfterBlackMove = decodeBoardString(".BW.\\"
                                                        + "BW.W\\"
                                                        + ".BW.\\"
                                                        + "....\\");
        gameLogic.setBoard(boardAfterBlackMove);
        gameLogic.setWhoseTurn(PlayerColors.BLACK);

        // Check for Ko at (2, 1) - where White would recapture
        assertTrue(gameLogic.isKoSituation(2, 1), "The method should identify a Ko situation correctly");
    }

    @Test
    public void testIsKoSituationPreviousBoardNull()
    {
        gameLogic = new GameLogic(4);
        gameLogic.setPreviousBoard(null);
        assertFalse(gameLogic.isKoSituation(2, 1), "The method should identify a Ko situation correctly");
    }

    @Test
    public void testNoKoSituation()
    {
        gameLogic = new GameLogic(4);
        String boardSetup =  ".BW.\\"
                            + "B.BW\\"
                            + ".BW.\\"
                            + "....\\";
        gameLogic.setPreviousBoard(decodeBoardString(boardSetup));

        Stone[][] boardAfterBlackMove = decodeBoardString(".BW.\\"
                                                        + "BW.W\\"
                                                        + ".BW.\\"
                                                        + "....\\");
        gameLogic.setBoard(boardAfterBlackMove);
        gameLogic.setWhoseTurn(PlayerColors.BLACK);

        // Check for Ko at (2, 1) - where White would recapture
        assertFalse(gameLogic.isKoSituation(2, 2), "The method should identify a Ko situation correctly");
    }

    @Test
    public void testIsKoSituationTwo() {
        gameLogic = new GameLogic(5);
        String previousBoardSetup =  ".....\\"
                                    + "..B..\\"
                                    + ".BWB.\\"
                                    + "..B..\\"
                                    + ".....\\";

        gameLogic.setPreviousBoard(decodeBoardString(previousBoardSetup));
        String currentBoardSetup =    ".....\\"
                                    + "..B..\\"
                                    + ".BBB.\\"
                                    + "..B..\\"
                                    + ".....\\";
        gameLogic.setBoard(decodeBoardString(currentBoardSetup));
        gameLogic.setWhoseTurn(PlayerColors.WHITE);

        // Check for Ko at (2, 2) - where White would recapture
        assertTrue(gameLogic.isKoSituation(2, 2), "The method should identify a Ko situation correctly");
    }    

    @Test
    public void testSuicideMoveOutOfBounds()
    {
        gameLogic = new GameLogic(2);
        assertFalse(gameLogic.isSuicideMove(1000, -300));

        gameLogic.setBoard(
            decodeBoardString("WW\\"
                            + "WW\\")
        );
        assertFalse(gameLogic.isSuicideMove(1, 1));
    }
    
    @Test
    public void testSuicideMoveOne() {
        gameLogic = new GameLogic(5);
        String boardSetup =  ".....\\"
                            + "..B..\\"
                            + ".B.B.\\"
                            + "..B..\\"
                            + ".....\\";
        gameLogic.setBoard(decodeBoardString(boardSetup));
        gameLogic.setWhoseTurn(PlayerColors.WHITE);
        assertTrue(gameLogic.isSuicideMove(2, 2), "Placing a stone at (2,2) should be identified as a suicide move");
    }

    @Test
    public void testSuicideMoveTwo()
    {
        gameLogic = new GameLogic(4);
        String boardSetup =   "....\\"
                            + ".WWW\\"
                            + "WBBB\\"
                            + "WWB.\\";
        gameLogic.setBoard(decodeBoardString(boardSetup));
        gameLogic.setWhoseTurn(PlayerColors.WHITE);
        assertFalse(gameLogic.isSuicideMove(3, 3), "Placing a stone at (3,1) should not be identified as a suicide move");
    }

    @Test
    public void testSuicideMoveThree()
    {
        gameLogic = new GameLogic(3);
        String boardSetup =   ".W.\\"
                            + "W.B\\"
                            + ".B.";
        gameLogic.setBoard(decodeBoardString(boardSetup));
        gameLogic.setWhoseTurn(PlayerColors.WHITE);
        assertFalse(gameLogic.isSuicideMove(1, 1), "Placing a stone at (1, 1) should not be identified as a suicide move");

        gameLogic.setBoard(decodeBoardString(boardSetup));
        gameLogic.setWhoseTurn(PlayerColors.BLACK);
        assertFalse(gameLogic.isSuicideMove(1, 1), "Placing a stone at (1, 1) should not be identified as a suicide move");
    }

    @Test
    void testEmptyBoardTerritory()
    {
        int[] territory = gameLogic.countTerritory();
        assertEquals(0, territory[0], "No territory should be owned by Black on an empty board");
        assertEquals(0, territory[1], "No territory should be owned by White on an empty board");
    }

    @Test
    void testSimpleDividedTerritory()
    {
        gameLogic = new GameLogic(3);
        String boardSetup =  "B..\\"
                            + "...\\"
                            + "..W";
        gameLogic.setBoard(decodeBoardString(boardSetup));

        int[] territory = gameLogic.countTerritory();
        assertEquals(0, territory[0], "Black should own 4 points of territory");
        assertEquals(0, territory[1], "White should own 4 points of territory");
    }

    @Test
    void testComplexDividedTerritory()
    {
        gameLogic = new GameLogic(3);
        String boardSetup =  "...\\"
                            + "BBB\\"
                            + "W.W";
        gameLogic.setBoard(decodeBoardString(boardSetup));
        
        
        int[] territory = gameLogic.countTerritory();
        assertEquals(3, territory[0], "Black should own 2 points of territory");
        assertEquals(0, territory[1], "White should own 3 points of territory");

        gameLogic = new GameLogic(3);
        boardSetup =  "...\\"
                            + "WWW\\"
                            + ".B.";
        gameLogic.setBoard(decodeBoardString(boardSetup));
        
        
        territory = gameLogic.countTerritory();
        assertEquals(0, territory[0], "Black should own 2 points of territory");
        assertEquals(3, territory[1], "White should own 3 points of territory");
    }

    @Test
    void testCalculateScoreWithEqualTerritory() {
        gameLogic.setCapturedStones(new int[]{0, 0}); // No captured stones
        float[] scores = gameLogic.calculateScore(new int[]{10, 10}); // Equal territory

        assertEquals(10, scores[0], "Black's score should be 10");
        assertEquals(16.5, scores[1], "White's score should be 16.5 with Komi");
    }

    @Test
    void testCalculateScoreWithCapturedStones() {
        gameLogic.setCapturedStones(new int[]{3, 2}); // Black captured 3, White captured 2 stones
        float[] scores = gameLogic.calculateScore(new int[]{10, 10}); // Equal territory

        assertEquals(13, scores[0], "Black's score should be 13 (10 territory + 3 captured)");
        assertEquals(18.5, scores[1], "White's score should be 18.5 (10 territory + 2 captured + 6.5 Komi)");
    }

    @Test
    void testCalculateScoreWithUnequalTerritory() {
        gameLogic.setCapturedStones(new int[]{0, 0}); // No captured stones
        float[] scores = gameLogic.calculateScore(new int[]{15, 5}); // Unequal territory

        assertEquals(15, scores[0], "Black's score should be 15");
        assertEquals(11.5, scores[1], "White's score should be 11.5 with Komi");
    }

    @Test
    void testCountCapturedStones() {
        // Assuming a method to set captured stones, if not use appropriate approach to set them
        gameLogic.setCapturedStones(new int[]{3, 5});
        int[] capturedStones = gameLogic.countCapturedStones();
        assertArrayEquals(new int[]{3, 5}, capturedStones, "Captured stones should match the set values");
    }

    @Test
    void testGetWhoseTurn() {
        // Assuming a method to set whose turn it is
        gameLogic.setWhoseTurn(PlayerColors.WHITE);
        assertEquals(PlayerColors.WHITE, gameLogic.getWhoseTurn(), "It should be White's turn");
    }

    @Test
    void testGetPreviousBoard() {
        // Assuming a method to set previous board state
        Stone[][] expectedBoard = decodeBoardString("...\\"
                                                    + "BBB\\"
                                                    + "W.W");
        gameLogic.setPreviousBoard(expectedBoard);
        Stone[][] actualBoard = gameLogic.getPreviousBoard();
        assertArrayEquals(expectedBoard, actualBoard, "Previous board should match the set board");
    }



    private static Stone[][] decodeBoardString(String boardString)
    {
        int boardSize = boardString.indexOf("\\");
        Stone[][] board = new Stone[boardSize][boardSize];
        
        for (int i = 0; i < boardSize; i++) {
            Arrays.fill(board[i], Stone.EMPTY);
        }
        
        int row = 0, col = 0;
        for (int i = 0; i < boardString.length(); i++) {
            char ch = boardString.charAt(i);
            switch (ch) {
                case 'W':
                board[col][row] = Stone.WHITE;
                col++;
                break;
                case 'B':
                board[col][row] = Stone.BLACK;
                col++;
                break;
                case '.':
                board[col][row] = Stone.EMPTY;
                col++;
                break;
                case '\\':
                row++;
                col = 0;
                break;
                default:
                System.out.println("Unexpected character: " + ch);
                break;
            }
            
            if (row > boardSize || col > boardSize) {
                System.out.println("Breaking out of loop - row: " + row + ", col: " + col);
                break;
            }
        }
        // printBoard(board);
        
        return board;
    }
    
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
}
