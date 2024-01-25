package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection
{
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    
    public ClientConnection(Socket socket) throws IOException
    {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public ClientConnection(Socket socket, ObjectOutputStream outputStream, ObjectInputStream inputStream)
    {
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }
    
    public void sendMessage(Object message) throws IOException
    {
        outputStream.writeObject(message);
        outputStream.reset();
    }
    
    public Object receiveMessage() throws IOException, ClassNotFoundException
    {
        return inputStream.readObject();
    }

    public Socket getSocket()
    {
        return socket;
    }

    public ObjectOutputStream getOutputStream()
    {
        return outputStream;
    }

    public ObjectInputStream getInputStream()
    {
        return inputStream;
    }
}