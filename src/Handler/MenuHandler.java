package Handler;

import DiscordFeatures.DiscordServer;
import DiscordFeatures.Permissions;
import UserFeatures.User;

public class MenuHandler {

    public static String welcomeMenu() {
        return """
                [1] Sign Up
                [2] Log In
                [3] About Us
                [0] Exit""";
    }

    public static String userStarterMenu() {
        return """
                [1] Servers
                [2] Private Chats //یا یکی رو انختاب کن یا یه یوزر نیم جدید بنویس
                [3] View Friends List
                [4] View Friend Requests
                [5] Send Friend Request
                [6] Block A Friend
                [7] View Blocked Users
                [8] Creat Server
                [9] Setting
                [0] Log Out""";
    }

    public static String settingAccountMenu(){
        return """
                [1] Change profile photo
                [2] Set Status
                [0] Exit""";
    }

    public static String statusMenu(){
        return """
                [1] Online
                [2] Idle
                [3] Do Not Disturb
                [4] Invisible
                [0] Back""";
    }

    public static String channelMenu() {
        return "[1] React to a message\n" +
                "[2] Reply to a message\n" +
                "[3] Back";
    }

    public static void serverMenu() {
        System.out.println("" +
                "[1] View Channels\n" +
                "[2] Invite a friend\n" +
                "[3] Settings\n");
    }

    public static void serverSettings(DiscordServer server, User user) {
        if (server.haveThisAccessibility(user, Permissions.CHANGE_SERVER_NAME)) {
            System.out.println("[1] Change server name");
        }
        if (server.haveThisAccessibility(user, Permissions.PIN_MESSAGES)) {
            System.out.println("[2] Pin Message");
        }
        if (server.haveThisAccessibility(user, Permissions.ADD_CHANNEL)) {
            System.out.println("[3] Create channel");
        }
        if (server.haveThisAccessibility(user, Permissions.REMOVE_CHANNEL)) {
            System.out.println("[4] Delete Channel");
        }
        if (server.haveThisAccessibility(user, Permissions.CHANNEL_PRIVACY)) {
            System.out.println("[5] Modify Channel access");
        }
        if (server.haveThisAccessibility(user, Permissions.REMOVE_MEMBER)) {
            System.out.println("[6] Ban member from server");
        }
        if (server.haveThisAccessibility(user, Permissions.CREATE_ROLES)) {
            System.out.println("[7] Create new role");
        }
        if (server.haveThisAccessibility(user, Permissions.ASSIGN_ROLE)) {
            System.out.println("[8] Assign role to member");
        }
        if (server.haveThisAccessibility(user, Permissions.DELETE_SERVER)) {
            System.out.println("[9] Delete Server");
        }
    }

}
