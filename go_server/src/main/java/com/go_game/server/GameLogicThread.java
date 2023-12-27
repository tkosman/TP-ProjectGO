package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.go_game.server.messages.OkMsg;

import main.java.com.go_game.server.enums.BoardSize;


//? This class will be responsible for game logic and will be an individual thread for each game session
public class GameLogicThread implements Runnable
{
    private ObjectOutputStream toPlayer1;
    private ObjectInputStream fromPlayer1;
    private ObjectOutputStream toPlayer2;
    private ObjectInputStream fromPlayer2;
    private BoardSize boardSize;


    public GameLogicThread(ObjectOutputStream toPlayer1, ObjectInputStream fromPlayer1, ObjectOutputStream toPlayer2, 
                            ObjectInputStream fromPlayer2, BoardSize boardSize) throws IOException
    {
        this.toPlayer1 = toPlayer1;
        this.fromPlayer1 = fromPlayer1;
        this.toPlayer2 = toPlayer2;
        this.fromPlayer2 = fromPlayer2;
        this.boardSize = boardSize;

        //! for debugging
        toPlayer1.writeObject(new OkMsg());
        toPlayer2.writeObject(new OkMsg());

        // run();
    }

    //TODO: yet to be implemented
    @Override
    public void run()
    {
        //? Here will be game logic
    }
}
