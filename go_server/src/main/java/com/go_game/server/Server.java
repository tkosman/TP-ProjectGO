package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import shared.enums.GameMode;
import shared.messages.ClientInfoMsg;
import shared.messages.IndexSetMsg;
import shared.enums.BoardSize;

public class Server
{
    //TODO: change if online server is implemented
    private static final int PORT = 4444;
    private static int player_index = 1;
    
    //TODO
    /*
    * add Client connection class sth like:
    public class ClientConnection {
        private Socket socket;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        
        public ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
        
        public void sendMessage(Object message) throws IOException {
            outputStream.writeObject(message);
        }
        
        public Object receiveMessage() throws IOException, ClassNotFoundException {
            return inputStream.readObject();
        }
    }
    */
    //TODO: to be refactored (look above)
    private static Socket waitingPlayer9x9 = null;
    private static ObjectInputStream fromWaitingPlayer9x9;
    private static ObjectOutputStream toWaitingPlayer9x9;

    public static void main(String[] args)
    {
        new Server().runServer();;
    }
    
    private void runServer()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println(new Date() + "\nServer started successfully at port " + PORT + "\n");

            while (true)
            {
                //! 1 IN
                //? Wait for a new player to join
                Socket clientSocket = serverSocket.accept();
                //? Create a new output for the newly joined player
                ObjectOutputStream toClient = new ObjectOutputStream(clientSocket.getOutputStream());

                //! 2 OUT
                toClient.writeObject(new IndexSetMsg(player_index));
                player_index++; //? Increment player index

                System.out.println(new Date() + "\nPlayer joined session with IP address " + clientSocket.getInetAddress().getHostAddress() + "\n");

                ObjectInputStream fromClient = new ObjectInputStream(clientSocket.getInputStream());
                try
                { 
                    //! 3 IN
                    ClientInfoMsg newPlayerInfo = (ClientInfoMsg)fromClient.readObject();
                    System.out.println(newPlayerInfo);

                    GameMode gameMode = newPlayerInfo.getGameMode();
                    if (gameMode == GameMode.REPLAY)
                    {
                        System.out.println("REPLAY");
                        //TODO: start a new thread for replay
                    }
                    else if (gameMode == GameMode.BOT)
                    {
                        System.out.println("BOT");
                        //TODO: start a new thread for bot
                    }
                    else if (gameMode == GameMode.MULTI_PLAYER)
                    {
                        BoardSize boardSize = newPlayerInfo.getBoardSize();

                        if(boardSize == BoardSize.NINE_X_NINE)
                        {
                            if(waitingPlayer9x9 == null)
                            {
                                waitingPlayer9x9 = clientSocket;
                                fromWaitingPlayer9x9 = fromClient;
                                toWaitingPlayer9x9 = toClient;
                            }
                            else
                            {
                                new MultiplayerGameThread(toClient, fromClient, toWaitingPlayer9x9, fromWaitingPlayer9x9, boardSize);
                                waitingPlayer9x9 = null;
                                fromWaitingPlayer9x9 = null;
                                toWaitingPlayer9x9 = null;
                            }

                        }
                        else if(boardSize == BoardSize.THIRTEEN_X_THIRTEEN)
                        {
                            System.out.println("THIRTEEN_X_THIRTEEN");
                            assert false : "To be implemented"; //TODO: implement
                        }
                        else if(boardSize == BoardSize.NINETEEN_X_NINETEEN)
                        {
                            System.out.println("NINETEEN_X_NINETEEN");
                            assert false : "To be implemented"; //TODO: implement
                        }
                        else { assert false : "Unknown board size"; }
                    }
                    else { assert false : "Unknown game mode"; }

                }
                catch (ClassNotFoundException e) { e.printStackTrace(); }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
