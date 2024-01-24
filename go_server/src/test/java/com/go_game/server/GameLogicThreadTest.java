package com.go_game.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class GameLogicThreadTest {
    private GameLogicThread gameLogicThread;

    @BeforeEach
    void setUp() throws IOException
    {
        gameLogicThread = new GameLogicThread(9);
    }

    public static void main(String[] args)
    {
        testSuicideMoveThree();
    }

    @Test
    void testBoardInitialization()
    {
        Stone[][] board = gameLogicThread.getBoard(); // Assuming getBoard() is a public method in GameLogicThread
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
                gameLogicThread.doProcessMove(i, j);
                Stone[][] board = gameLogicThread.getBoard();
                assertEquals(Stone.BLACK, board[i][j], "After the first move, the position should have a BLACK stone");
            }
        }
    }

    @Test
    void testInvalidMoveProcessing()
    {
        gameLogicThread.doProcessMove(4, 4);

        assertFalse(gameLogicThread.getIsMoveValid(4, 4), "Move should be invalid for an already occupied position");
        assertFalse(gameLogicThread.getIsMoveValid(-1, 4), "Move should be invalid for out-of-bounds positions");
        assertFalse(gameLogicThread.getIsMoveValid(9, 9), "Move should be invalid for out-of-bounds positions");
    }

    @Test
    public void testCaptureSingleStone() 
    {
        GameLogicThread gameLogicThread = new GameLogicThread(3);
        gameLogicThread.setBoard(decodeBoardString(".B.\\BWB\\.B."));
        gameLogicThread.setWhoseTurn(PlayerColors.BLACK);
        gameLogicThread.testCaptureStones(2, 1);
        assertEquals(gameLogicThread.getBoard()[1][1], Stone.EMPTY, "The stone at (1,1) should be captured and thus EMPTY");
    }

    @Test
    public void testCaptureSingleStoneDifferentScenario()
    {
        GameLogicThread gameLogicThread = new GameLogicThread(5);
        gameLogicThread.setBoard(decodeBoardString(".....\\..B\\.BWB\\..B\\....."));
        gameLogicThread.setWhoseTurn(PlayerColors.BLACK);
        gameLogicThread.testCaptureStones(2, 1);
        assertEquals(gameLogicThread.getBoard()[2][2], Stone.EMPTY, "The white stone should be captured");
    }

    @Test
    public void testCaptureMultipleStones() 
    {
        GameLogicThread gameLogicThread = new GameLogicThread(5);
        gameLogicThread.setBoard(decodeBoardString(".BB..\\BWWB.\\BWWB.\\BWWB.\\.BB..\\"));
        gameLogicThread.setWhoseTurn(PlayerColors.BLACK);
        gameLogicThread.testCaptureStones(2, 0);

        assertAll("Multiple stones should be captured",
            () -> assertEquals(gameLogicThread.getBoard()[1][1], Stone.EMPTY, "Expected the stone at (1,1) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogicThread.getBoard()[1][2], Stone.EMPTY, "Expected the stone at (1,2) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogicThread.getBoard()[1][3], Stone.EMPTY, "Expected the stone at (1,3) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogicThread.getBoard()[2][1], Stone.EMPTY, "Expected the stone at (2,1) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogicThread.getBoard()[2][2], Stone.EMPTY, "Expected the stone at (2,2) to be captured and thus EMPTY"),
            () -> assertEquals(gameLogicThread.getBoard()[2][3], Stone.EMPTY, "Expected the stone at (2,3) to be captured and thus EMPTY")
        );
    }

    @Test
    public void testCaptureMultipleStonesDifferentScenario()
    {
        GameLogicThread gameLogicThread = new GameLogicThread(5);
        gameLogicThread.setBoard(decodeBoardString(".....\\.BBB.\\BWWB.\\BWWB.\\.BB..\\"));
        gameLogicThread.setWhoseTurn(PlayerColors.BLACK);
        gameLogicThread.testCaptureStones(3, 3);

        assertAll("Multiple stones in a different scenario should be captured",
            () -> assertEquals(gameLogicThread.getBoard()[1][2], Stone.EMPTY, "The white stone at (1,2) should be captured"),
            () -> assertEquals(gameLogicThread.getBoard()[1][3], Stone.EMPTY, "The white stone at (1,3) should be captured"),
            () -> assertEquals(gameLogicThread.getBoard()[2][2], Stone.EMPTY, "The white stone at (2,2) should be captured"),
            () -> assertEquals(gameLogicThread.getBoard()[2][3], Stone.EMPTY, "The white stone at (2,3) should be captured")
        );
    }

    @Test
    public void testIsKoSituationOne()
    {
        gameLogicThread = new GameLogicThread(4);
        String boardSetup =  ".BW.\\"
                            + "B.BW\\"
                            + ".BW.\\"
                            + "....\\";
        gameLogicThread.setPreviousBoard(decodeBoardString(boardSetup));

        Stone[][] boardAfterBlackMove = decodeBoardString(".BW.\\"
                                                        + "BW.W\\"
                                                        + ".BW.\\"
                                                        + "....\\");
        gameLogicThread.setBoard(boardAfterBlackMove);
        gameLogicThread.setWhoseTurn(PlayerColors.BLACK);

        // Check for Ko at (2, 1) - where White would recapture
        assertTrue(gameLogicThread.testIsKoSituation(2, 1), "The method should identify a Ko situation correctly");
    }

    @Test
    public void testIsKoSituationTwo() {
        gameLogicThread = new GameLogicThread(5);
        String previousBoardSetup =  ".....\\"
                                    + "..B..\\"
                                    + ".BWB.\\"
                                    + "..B..\\"
                                    + ".....\\";

        gameLogicThread.setPreviousBoard(decodeBoardString(previousBoardSetup));
        String currentBoardSetup =    ".....\\"
                                    + "..B..\\"
                                    + ".BBB.\\"
                                    + "..B..\\"
                                    + ".....\\";
        gameLogicThread.setBoard(decodeBoardString(currentBoardSetup));
        gameLogicThread.setWhoseTurn(PlayerColors.WHITE);

        // Check for Ko at (2, 2) - where White would recapture
        assertTrue(gameLogicThread.testIsKoSituation(2, 2), "The method should identify a Ko situation correctly");
    }    
    
    @Test
    public void testSuicideMoveOne() {
        gameLogicThread = new GameLogicThread(5);
        String boardSetup =  ".....\\"
                            + "..B..\\"
                            + ".B.B.\\"
                            + "..B..\\"
                            + ".....\\";
        gameLogicThread.setBoard(decodeBoardString(boardSetup));
        gameLogicThread.setWhoseTurn(PlayerColors.WHITE);
        assertTrue(gameLogicThread.testIsSuicideMove(2, 2), "Placing a stone at (2,2) should be identified as a suicide move");
    }

    @Test
    public void testSuicideMoveTwo()
    {
        gameLogicThread = new GameLogicThread(4);
        String boardSetup =   "....\\"
                            + ".WWW\\"
                            + "WBBB\\"
                            + "WWB.\\";
        gameLogicThread.setBoard(decodeBoardString(boardSetup));
        gameLogicThread.setWhoseTurn(PlayerColors.WHITE);
        assertFalse(gameLogicThread.testIsSuicideMove(3, 3), "Placing a stone at (3,1) should not be identified as a suicide move");
    }

    @Test
    public static void testSuicideMoveThree()
    {
        GameLogicThread gameLogicThread = new GameLogicThread(3);
        String boardSetup =   ".W.\\"
                            + "W.B\\"
                            + ".B.";
        gameLogicThread.setBoard(decodeBoardString(boardSetup));
        gameLogicThread.setWhoseTurn(PlayerColors.WHITE);
        assertFalse(gameLogicThread.testIsSuicideMove(1, 1), "Placing a stone at (1, 1) should not be identified as a suicide move");

        gameLogicThread.setBoard(decodeBoardString(boardSetup));
        gameLogicThread.setWhoseTurn(PlayerColors.BLACK);
        assertFalse(gameLogicThread.testIsSuicideMove(1, 1), "Placing a stone at (1, 1) should not be identified as a suicide move");
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
}
