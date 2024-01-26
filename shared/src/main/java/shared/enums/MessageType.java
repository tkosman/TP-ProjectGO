package shared.enums;

public enum MessageType
{
    CLIENT_INFO,            //? client info
    OK,                     //? sent to say that the message was received
    INDEX_SET,              //? sent to say that the player index was set
    MOVE,                   //? sent to say that the player made a move and to update the board
    BOARD_STATE,            //? sent to update the board
    STRING_MESSAGE,         //? used to send a string message
    GAME_JOINED,            //? sent to say that the game was joined
    MOVE_NOT_VALID,         //? sent to say that the move was invalid
    GAME_OVER,              //? sent to say that the game is over
    PLAYER_PASSED,          //? sent to say that the player passed
    RESULTS_NEGOTIATION,     //? sent to negotiate the results
    REPLAY_FETCH,   //? sent to show all the replays
    CLOSE,          //? sent to close connection
}
