package Handler;

import DiscordFeatures.DiscordServer;
import DiscordFeatures.Permissions;
import UserFeatures.User;

/**
 * Contains all the various menus used for communication with the client.
 * All menus are returned as strings and printed to console in client.
 * All menus are called with static methods in order to prevent the
 * unnecessary creation of multiple instances
 */
public class MenuHandler {

    /**
     * @return menu when client opens the program
     */
    public static String welcomeMenu() {
        return """
                [1] Sign Up
                [2] Log In
                [3] About Us
                [0] Exit""";
    }

    /**
     * @return menu when client has logged in as user
     */
    public static String userStarterMenu() {
        return """
                [1] Servers
                [2] Private Chats
                [3] View Friends List
                [4] View Friend Requests
                [5] Send Friend Request
                [6] Block A Friend
                [7] View Blocked Users
                [8] Creat Server
                [9] Setting
                [0] Log Out""";
    }

    /**
     * @return menu when user enters user settings
     */
    public static String settingAccountMenu() {
        return """
                [1] Change profile photo
                [2] Set Status
                [3] Change Password
                [0] Exit""";
    }

    /**
     * @return menu when user wants to change status
     */
    public static String statusMenu() {
        return """
                [1] Online
                [2] Idle
                [3] Do Not Disturb
                [4] Invisible
                [0] Back""";
    }

    /**
     * @return menu when user enters a server
     */
    public static String serverMenu() {
        return """
                [1] View Channels
                [2] View Members
                [3] Invite a friend
                [4] Settings""";
    }

    /**
     * menu when user enters server settings. These settings change based on the roles
     * each member has therefore the parameters allow for the menu to be printed correctly
     *
     * @param server server in which the menu is being printed in
     * @param user   the user which this menu is being printed for
     * @return menu for the user trying to access server settings
     */
    public static String serverSettings(DiscordServer server, User user) {
        String s = "";
        if (server.haveThisAccessibility(user, Permissions.CHANGE_SERVER_NAME)) {
            s = s.concat("[1] Change server name\n");
        }
        if (server.haveThisAccessibility(user, Permissions.ADD_CHANNEL)) {
            s = s.concat("[2] Create channel\n");
        }
        if (server.haveThisAccessibility(user, Permissions.REMOVE_CHANNEL)) {
            s = s.concat("[3] Delete Channel\n");
        }
        if (server.haveThisAccessibility(user, Permissions.CHANNEL_PRIVACY)) {
            s = s.concat("[4] Modify Channel access\n");
        }
        if (server.haveThisAccessibility(user, Permissions.REMOVE_MEMBER)) {
            s = s.concat("[5] Ban member from server\n");
        }
        if (server.haveThisAccessibility(user, Permissions.CREATE_ROLES)) {
            s = s.concat("[6] Create new role\n");
        }
        if (server.haveThisAccessibility(user, Permissions.ASSIGN_ROLE)) {
            s = s.concat("[7] Assign role to member\n");
        }
        if (server.haveThisAccessibility(user, Permissions.DELETE_SERVER)) {
            s = s.concat("[8] Delete Server\n");
        }
        s = s.concat("[9] Leave Server\n");

        return s;
    }

    /**
     * menu when user enters channel. This menu changes based on the roles
     * each member has therefore the parameters allow for the menu to be printed correctly
     *
     * @param server server in which the menu is being printed in
     * @param user   the user which this menu is being printed for
     * @return menu for the user trying to access the channel
     */
    public static String channelMenu(DiscordServer server, User user) {
        if (server.haveThisAccessibility(user, Permissions.PIN_MESSAGES))
            return """
                    [1] Send message
                    [2] Send file
                    [3] Download file
                    [4] React to a message
                    [5] Reply to a message
                    [6] View pinned messages
                    [7] Pin message
                    [0] Back
                    """;

        return """
                [1] Send message
                [2] Send file
                [3] Download file
                [4] React to a message
                [5] Reply to a message
                [6] View pinned messages
                [0] Back
                """;
    }

}
