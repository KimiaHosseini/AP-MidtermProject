package Handler;

/**
 * This class is the ResponseStatus sent from server to client with the Response.
 * each response has a response status that determines whether
 * the request was handled successfully or not in the server.
 * The responseStatus represents how the request was handled by the server.
 */

public enum ResponseStatus {
    // signing up
    INVALID_USERNAME,
    VALID_USERNAME,
    SIGNUP_INVALID,
    SIGNUP_VALID,

    // logging in
    SIGN_IN_VALID,
    SIGN_IN_INVALID,
    SIGN_OUT_VALID,

    // user configurations
    SAME_PASSWORD,
    VALID_CHANGE_PASSWORD,
    VALID_STATUS,
    INVALID_STATUS,
    PROFILE_PHOTO,

    // friends and friend requests
    EMPTY_FRIEND_REQUEST_LIST,
    NOT_EMPTY_FRIEND_REQUEST_LIST,
    DUPLICATE_FRIEND_REQUEST,
    FRIEND_REQUEST_TO_FRIEND,
    VALID_FRIEND_REQUEST,
    VALID_ACCEPT_FRIEND_REQUEST,
    INVALID_ACCEPT_FRIEND_REQUEST,
    VALID_DELETE_FRIEND_REQUEST,
    INVALID_DELETE_FRIEND_REQUEST,

    // blocked users
    DUPLICATE_BLOCK,
    INVALID_BLOCK,
    VALID_BLOCK,
    EMPTY_BLOCKED_USERS_LIST,
    NOT_EMPTY_BLOCKED_USERS_LIST,
    INVALID_UNBLOCK,
    VALID_UNBLOCK,

    //private chat
    PRIVATE_CHAT_CREATED,
    CLOSE_THREAD,
    VALID_MESSAGE,
    BLOCKED_USERNAME,
    FILE_DOWNLOADED,
    INVALID_FILE_NAME,

    //server
    VIEW_ONLY,
    BANNED_USER,
}
