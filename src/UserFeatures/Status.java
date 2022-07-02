package UserFeatures;

import java.io.Serializable;

/**
 * This enum contains all the different statuses a user may
 * have on the program. This status can be chosen by the user
 * and is otherwise chosen automatically when client is logged in or out
 */
public enum Status implements Serializable {
    ONLINE,
    IDLE,
    DND,
    INVISIBLE,
    OFFLINE,
}
