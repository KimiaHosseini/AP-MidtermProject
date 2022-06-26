package Handler;

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

    public static String friendsListMenu() {
        return "[1] Send DiscordFeatures.Message\n" +
                "[2] Block friend\n "+
                "[2] Back";
    }

    public static String channelMenu() {
        return "[1] React to a message\n" +
                "[2] Reply to a message\n" +
                "[3] Back";
    }
}
