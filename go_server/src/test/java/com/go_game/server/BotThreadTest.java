package com.go_game.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import shared.enums.AgreementState;
import shared.enums.PlayerColors;
import shared.messages.BoardStateMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.PlayerPassedMsg;
import shared.messages.ResultsNegotiationMsg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
public class BotThreadTest {

    @Mock
    private ClientConnection mockConnection = mock(ClientConnection.class);
    @Mock
    private GameLogic mockGameLogic = mock(GameLogic.class);

    @Test
    public void testPlayerMakesValidMove() throws IOException, ClassNotFoundException
    {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        when(mockConnection.receiveMessage()).thenReturn(new MoveMsg(5, 5));
        when(mockGameLogic.isInBoundsAndEmptySpace(anyInt(), anyInt())).thenReturn(true);
        when(mockGameLogic.getWhoseTurn()).thenReturn(PlayerColors.BLACK);

        botThread.handlePlayerMove();

        verify(mockGameLogic, atLeast(1)).processMove(5, 5);
        verify(mockGameLogic, atLeast(1)).captureStones(5, 5);
        verify(mockConnection, atLeast(1)).sendMessage(any(BoardStateMsg.class));
    }

    @Test
    public void testPlayerMakesInValidMove() throws IOException, ClassNotFoundException
    {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        when(mockConnection.receiveMessage()).thenReturn(new MoveMsg(true));
        botThread.handlePlayerMove();

    }

    @Test
    public void testPlayerMakePass() throws IOException, ClassNotFoundException
    {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        when(mockConnection.receiveMessage()).thenReturn(new MoveMsg(-5, -3));
        when(mockGameLogic.isInBoundsAndEmptySpace(anyInt(), anyInt())).thenReturn(false);

        botThread.handlePlayerMove();
        verify(mockConnection, atLeast(1)).sendMessage(any(MoveNotValidMsg.class));
    }
    

    @Test
    public void testBotMakesMove() throws IOException {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        when(botThread.isMoveValid(anyInt(), anyInt())).thenReturn(true).thenReturn(false).thenReturn(false);
        when(mockGameLogic.getWhoseTurn()).thenReturn(PlayerColors.WHITE);

        botThread.handleBotMove();

        verify(mockGameLogic, atLeast(1)).processMove(anyInt(), anyInt());
        verify(mockConnection, atLeast(1)).sendMessage(any(BoardStateMsg.class));
    }

    @Test
    public void testBotMakesInvalidMove() throws IOException {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        when(botThread.isMoveValid(anyInt(), anyInt())).thenReturn(false);
        when(mockGameLogic.getWhoseTurn()).thenReturn(PlayerColors.WHITE);

        botThread.handleBotMove();

        verify(mockConnection, atLeast(1)).sendMessage(any(PlayerPassedMsg.class));
    }

    @Test
    public void testGameOverAgree() throws IOException, ClassNotFoundException
    {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        assertFalse(botThread.isGameOver());

        botThread.setGameOver(true);
        when(mockConnection.receiveMessage()).thenReturn(new ResultsNegotiationMsg(AgreementState.AGREE));
        assertTrue(botThread.isGameOver());
        verify(mockConnection, atLeast(1)).sendMessage(any(GameOverMsg.class));
    }

    @Test
    public void testGameOverDisagree() throws IOException, ClassNotFoundException
    {
        BotThread botThread = new BotThread(mockConnection, mockGameLogic, 19);

        botThread.setGameOver(true);

        when(mockConnection.receiveMessage()).thenReturn(new ResultsNegotiationMsg(AgreementState.DISAGREE));
        assertFalse(botThread.isGameOver());
        verify(mockConnection, atLeast(1)).sendMessage(any (ResultsNegotiationMsg.class));
    }
}
