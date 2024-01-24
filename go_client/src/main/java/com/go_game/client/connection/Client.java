package com.go_game.client.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Scanner;

import com.go_game.client.game_model.Player;

import shared.enums.BoardSize;
import shared.enums.GameMode;
import shared.enums.MessageType;
import shared.enums.PlayerColors;
import shared.enums.Stone;
import shared.messages.AbstractMessage;
import shared.messages.BoardStateMsg;
import shared.messages.ClientInfoMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.GameOverMsg;
import shared.messages.IndexSetMsg;
import shared.messages.MoveMsg;
import shared.messages.MoveNotValidMsg;
import shared.messages.OkMsg;
import shared.messages.PlayerPassedMsg;


public class Client implements Runnable
{
    private final static String HOST = "localhost";//? in future extended not only to localhost
    private final static int PORT = 4444;

    private Socket socket;
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private int playerNo;
    // private boolean isPlayer1Turn = true;
    private PlayerColors playerColor;
    private int gameID;
    private PlayerColors whoseTurn = PlayerColors.BLACK;
    private static Scanner scanner; //! for debugging purposes

    private Stone[][] board; //! for debugging purposes


    public static void main(String[] args) throws ClassNotFoundException
    {
        scanner = new Scanner(System.in);
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
            toServer.reset();

            //! 4 IN
            GameJoinedMsg gameJoinedMsg = (GameJoinedMsg)fromServer.readObject();
            gameID = gameJoinedMsg.getGameID();
            playerColor = gameJoinedMsg.getPlayerColors();
            whoseTurn = gameJoinedMsg.getWhoseTurn();

            System.out.println("Game ID: " + gameID);

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
                System.out.println("\n\nTURN: " + whoseTurn);
                //! EACH WILL BE SENT TO SERVER
                if (isMyTurn())
                {
                    //? This player turn
                    //! 1 OUT +++++++++ -> It's my turn and I'm sending move to server
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING MOVE TO SERVER AS PLAYER " + playerNo + " TURN: " + whoseTurn);
                    sendMoveToServer();

                    //! 2 IN ########## -> It's my turn and I'm waiting for server to send me info back
                    receiveInfoFromServer();
                }
                else
                {
                    // //! 1 OUT +++++++++ -> It's NOT my turn so I'm just sending OK to server
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " SENDING OK TO SERVER AS PLAYER " + playerNo + " TURN: " + whoseTurn);
                    sendOkToServer();

                    //! 2 IN ########## -> It's NOT my turn so I'm waiting for server to send me info back
                    receiveInfoFromServer();
                }

            }
        }
        catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
    }   

    private void receiveInfoFromServer() throws IOException, ClassNotFoundException 
    {
        //TODO: !DOBREK! here you should receive the info from the server and handle it

        AbstractMessage message = (AbstractMessage) fromServer.readObject();
        System.out.println("MESSAGE " + message);

        if (message.getType() == MessageType.BOARD_STATE)
        {
            switchTurns();
            //? Player did a valid move OR it was not his trun
            //? server sent back the updated board
            System.out.println(new Timestamp(System.currentTimeMillis()) + " RECEIVE BOARD FROM SERVER " + playerNo  + " TURN: " + whoseTurn);
            BoardStateMsg boardStateMsg = (BoardStateMsg) message;
            board = boardStateMsg.getBoardState();
            printBoard();
        }
        else if (message.getType() == MessageType.MOVE_NOT_VALID)
        {
            //? Player did an invalid move
            //? we need to resent the move
            if (playerNo == ((MoveNotValidMsg)message).playerWhoDidNotValidMove())
            {
                System.out.println(new Timestamp(System.currentTimeMillis()) + " MOVE INVALID BY #" + playerNo  + " TURN: " + whoseTurn);
            }
            else
            {
                System.out.println(new Timestamp(System.currentTimeMillis()) + " MOVE INVALID BY #" + playerNo  + " TURN: " + whoseTurn);
            }

        }
        else if (message.getType() == MessageType.PLAYER_PASSED)
        {
            //? Player passed
            //? we need to print who passed
            PlayerPassedMsg playerPassedMsg = (PlayerPassedMsg) message;
            PlayerColors playerColor = playerPassedMsg.getPlayerColor();
            System.out.println(new Timestamp(System.currentTimeMillis()) + " PLAYER PASSED " + playerColor);
            switchTurns();
        }

        else if (message.getType() == MessageType.GAME_OVER)
        {
            //? Game over
            //? we need to print the winner and the reason
            GameOverMsg gameOverMsg = (GameOverMsg) message;
            PlayerColors winner = gameOverMsg.getWinner();
            String reason = gameOverMsg.getReasonOrResult();
            System.out.println(new Timestamp(System.currentTimeMillis()) + " GAME OVER\nWinner: " + winner + "\nReason: " + reason);
            System.exit(0);
        }
        else
        {
            //? Something went wrong, simply won't happen
            System.err.println("Sorry sth went wrong :(");
        }
    }

     //TODO: yet to be implemented
     private boolean isGameOver()
     {
        return false; // Placeholder
     }

     private boolean isMyTurn()
     {
        // return isPlayer1Turn && playerNo % 2 == 1  || !isPlayer1Turn && playerNo % 2 == 0;
        return whoseTurn == playerColor;
    }

    private void switchTurns()
    {
        // isPlayer1Turn = !isPlayer1Turn;
        whoseTurn = (whoseTurn == PlayerColors.BLACK) ? PlayerColors.WHITE : PlayerColors.BLACK;
    }

    private void sendOkToServer() throws IOException
    {
        toServer.writeObject(new OkMsg());
    }

     private void sendMoveToServer() throws IOException
     {
        MoveMsg moveMsg = getPlayerMove();
        toServer.writeObject(moveMsg);
        toServer.reset();
    }

    private MoveMsg getPlayerMove() {
        //TODO: !DOBREK! here you should get the move from the player return it as a MoveMsg

        System.out.println("Do you want to pass? (y/n)");
        String pass = scanner.nextLine();
        if (pass.equals("y")) {
            return new MoveMsg(true);
        }
        System.out.print("Enter x coordinate: ");
        int x = scanner.nextInt();
        System.out.print("Enter y coordinate: ");
        int y = scanner.nextInt();

        return new MoveMsg(x, y);
    }

     //! for debugging purposes
     private void printBoard()
     {
        // System.out.print("\033[H\033[2J");  
        // System.out.flush();  
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
