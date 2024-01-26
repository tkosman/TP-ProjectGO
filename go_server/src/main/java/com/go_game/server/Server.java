package com.go_game.server;

import java.io.IOException;
import java.net.ServerSocket;
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
    
    ClientConnection clientConnection;
    ClientConnection waitingPlayer9x9Connection;

    public static void main(String[] args)
    {
        new Server().runServer();
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
                clientConnection = new ClientConnection(serverSocket.accept());

                //! 2 OUT
                clientConnection.sendMessage(new IndexSetMsg(player_index));
                player_index++; //? Increment player index

                System.out.println(new Date() + "\nPlayer joined session with IP address " + clientConnection.getSocket().getInetAddress().getHostAddress() + "\n");

                try
                { 
                    //! 3 IN
                    ClientInfoMsg newPlayerInfo = (ClientInfoMsg)clientConnection.receiveMessage();
                    System.out.println(newPlayerInfo);

                    GameMode gameMode = newPlayerInfo.getGameMode();
                    if (gameMode == GameMode.REPLAY)
                    {
                        //! idk if that's how to do it
                        new ReplayThread(toClient, fromClient);
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
                            if(waitingPlayer9x9Connection == null)
                            {
                                waitingPlayer9x9Connection = new ClientConnection(clientConnection.getSocket(), clientConnection.getOutputStream(), clientConnection.getInputStream());
                            }
                            else
                            {
                                new MultiplayerGameThread(clientConnection, waitingPlayer9x9Connection, 9);
                                waitingPlayer9x9Connection = null;
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
