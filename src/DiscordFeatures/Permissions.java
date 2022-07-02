package DiscordFeatures;

import java.io.Serializable;

/**
 * enum of permissions each role can have. These permissions are used to
 * determine the accesses of each user based on the permissions their roles have.
 */
public enum Permissions implements Serializable {
    ADD_CHANNEL,
    REMOVE_CHANNEL,
    REMOVE_MEMBER,
    CANT_VIEW_CHAT_HISTORY,
    CHANGE_SERVER_NAME,
    PIN_MESSAGES,
    CHANNEL_PRIVACY,
    CREATE_ROLES,
    ASSIGN_ROLE,

    // owner only permission
    DELETE_SERVER,
}
