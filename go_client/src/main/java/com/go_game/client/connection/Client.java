package com.go_game.client.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.sql.Timestamp;

import shared.enums.BoardSize;
import shared.enums.GameMode;
import shared.enums.Stone;
import shared.messages.BoardStateMsg;
import shared.messages.ClientInfoMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.IndexSetMsg;
import shared.messages.MoveMsg;
import shared.messages.OkMsg;


public class Client implements Runnable
{
    private final static String HOST = "localhost";//? in future extended not only to localhost
    private final static int PORT = 4444;

    private Socket socket;
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private int playerNo;
    private boolean isPlayer1Turn;
    
    private Stone playerColor;
    private int gameID;

    private Stone[][] board; //! for debugging purposes


    public static void main(String[] args) throws ClassNotFoundException
    {
        new Client();
    }

    public Client() throws ClassNotFoundException
    {
        establishServerConnection();
    }

    private void establishServerConnection() throws ClassNotFoundException
    {
        try
        {
            //! 1 OUT
            socket = new Socket(HOST, PORT);
            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());

            //! 2 IN
            //? Get player index from server
            IndexSetMsg playerIndex = (IndexSetMsg)fromServer.readObject();
            playerNo = playerIndex.getIndex();
            System.out.println("You are player " + playerIndex.getIndex() + "\n");

            //! 3 OUT
            toServer.writeObject(new ClientInfoMsg(BoardSize.NINE_X_NINE, GameMode.MULTI_PLAYER));

            //! 4 IN
            GameJoinedMsg gameJoinedMsg = (GameJoinedMsg)fromServer.readObject();
            gameID = gameJoinedMsg.getGameID();
            playerColor = gameJoinedMsg.getStoneColor();
            isPlayer1Turn = gameJoinedMsg.isPlayer1Turn();

            //! HANDSHAKE FINISHED
            Thread fred = new Thread(this);
            fred.start();
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
    @Override
    public void run()
    {
        try
        {
            while (!isGameOver())
            {
                if (isMyTurn())
                {
                    //? This player turn
                    //! 1 OUT -> It's my turn and I'm sending move to server
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING MOVE TO SERVER AS " + playerNo + " TURN: " + (isPlayer1Turn ? "BLACK" : "WHITE"));
                    sendMoveToServer();

                    //! 2 IN -> It's my turn and I'm waiting for server to send me info back
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " RECEIVE BOARD FROM SERVER " + playerNo  + " TURN: " + (isPlayer1Turn ? "BLACK" : "WHITE"));
                    receiveInfoFromServer();
                }
                else
                {
                    // //! 1 OUT -> It's NOT my turn so I'm just sending OK to server
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING OK TO SERVER AS " + playerNo + " TURN: " + (isPlayer1Turn ? "BLACK" : "WHITE"));
                    sendOkToServer();

                    //! 2 IN -> It's NOT my turn so I'm waiting for server to send me info back
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " RECEIVE BOARD FROM SERVER " + playerNo  + " TURN: " + (isPlayer1Turn ? "BLACK" : "WHITE"));
                    receiveInfoFromServer();
                }

                switchTurns();
            }
        }
        catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
    }   

     //TODO: yet to be implemented
     private boolean isGameOver()
     {
         return false; // Placeholder
     }

     private boolean isMyTurn()
     {
        return isPlayer1Turn && playerNo == 1 || !isPlayer1Turn && playerNo == 2;
    }

    private void switchTurns()
    {
        isPlayer1Turn = !isPlayer1Turn;
    }

    private void sendOkToServer() throws IOException
    {
        toServer.writeObject(new OkMsg());
    }

     private void sendMoveToServer() throws IOException
     {
         MoveMsg moveMsg = getPlayerMove();
         System.out.println(moveMsg); //! for debugging purposes
        toServer.writeObject(moveMsg);
    }

    private MoveMsg getPlayerMove() {
        //TODO: !DOBREK! here you should get the move from the player return it as a MoveMsg

        //Placeholder
        int x = (int) (Math.random() * 9);
        int y = (int) (Math.random() * 9);
        return new MoveMsg(x, y);
    }

    private void receiveInfoFromServer() throws IOException, ClassNotFoundException 
    {
        //TODO: !DOBREK! here you should receive the info from the server and handle it
        BoardStateMsg message = (BoardStateMsg) fromServer.readObject();
        board = message.getBoardState();
        printBoard();
    }

     //! for debugging purposes
     private void printBoard()
     {
        int boardSize = 9;
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
}
