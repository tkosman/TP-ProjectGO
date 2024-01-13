package shared.enums;


//TODO:change this enum for actual states used in the game
public enum MessageType
{
    CLIENT_INFO,   //? client info
    OK,            //? sent to say that the message was received
    INDEX_SET,     //? sent to say that the player index was set
    MOVE,           //? sent to say that the player made a move and to update the board
    BOARD_STATE,    //? sent to update the board
    STRING_MESSAGE, //? used to send a string message
    GAME_JOINED,    //? sent to say that the game was joined
}
