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
