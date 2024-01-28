package com.go_game.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shared.messages.OkMsg;
import java.io.*;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientConnectionTest {

    @Mock
    private Socket socket;

    @Mock
    private ObjectOutputStream outputStream;

    @Mock
    private ObjectInputStream inputStream;

    private ClientConnection clientConnection;

    @BeforeEach
    void setUp() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        outputStream = new ObjectOutputStream(baos);
        outputStream.writeObject(new OkMsg());
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        inputStream = new ObjectInputStream(bais);
        lenient().when(socket.getOutputStream()).thenReturn(baos);
        clientConnection = new ClientConnection(socket, outputStream, inputStream);
    }

    @Test
    void testSendMessage() throws IOException 
    {
        Object message = "Test Message";
        clientConnection.sendMessage(message);
        ByteArrayOutputStream baos = (ByteArrayOutputStream) socket.getOutputStream();
        assertTrue(baos.size() > 0, "Message should be written to the output stream");
    }
    
    @Test
    void testReceiveMessage() throws IOException, ClassNotFoundException 
    {
        String testMessage = "Test Message";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(testMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        inputStream = new ObjectInputStream(bais);

        clientConnection = new ClientConnection(socket, outputStream, inputStream);

        Object receivedMessage = clientConnection.receiveMessage();
        assertEquals(testMessage, receivedMessage, "Received message should match the test message");
    }

    @Test
    void testCloseConnection() 
    {
        assertDoesNotThrow(() -> clientConnection.closeConnection(), "Closing connection should not throw an exception");
    }

}
