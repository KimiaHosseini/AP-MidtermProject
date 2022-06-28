package Model;

public enum RequestType {

    // signing up
    CHECK_USERNAME,
    SIGN_UP,
    SIGN_IN,
    SIGN_OUT,

    // user configurations
    PROFILE_PHOTO,
    SET_STATUS,

    // friends and friend requests
    FRIEND_REQUEST,
    ACCEPT_FRIEND_REQUEST,
    DELETE_FRIEND_REQUEST,
    PRINT_FRIEND_REQUESTS,
    PRINT_FRIENDS,

    // blocking users
    PRINT_BLOCKED_FRIENDS,
    BLOCK_FRIEND,
    UNBLOCK_USER,

    // private chat
    PRIVATE_CHAT,
    PRINT_PRIVATE_CHATS_USERNAMES,
    SEND_MESSAGE,
    CLOSE_CHAT,

    // server
    NEW_SERVER,
    PRINT_SERVERS,
    PRINT_CHANNELS,
    INVITE_TO_SERVER,
    ENTER_CHANNEL,
    GET_CHANNEL,
    SEND_MESSAGE_TO_CHANNEL,

    //server accessibility
    NEW_CHANNEL,
    DELETE_CHANNEL,
    CHANGE_SERVER_NAME,
    DELETE_SERVER,
}
