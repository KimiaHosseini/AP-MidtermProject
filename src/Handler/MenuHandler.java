package Handler;

public class MenuHandler {

    public static void welcomeMenu() {
        System.out.println("""
                [1] Sign Up
                [2] Log In
                [3] About Us
                [0] Exit""");
    }

    public static void userStarterMenu() {
        System.out.println("""
                [1] View Channels
                [2] View new notifications
                [3] View Friends List
                [4] View Friend Requests
                [5] Send Friend Request
                [6] Creat Server
                [7] Setting
                [0] Log Out""");
    }

    public static void settingAccountMenu(){
        System.out.println("""
                [1] Change profile photo
                [2] Set Status
                [0] Exit""");
    }

    public static void statusMenu(){
        System.out.println("""
                [1] Online
                [2] Idle
                [3] Do Not Disturb
                [4] Invisible
                [0] Back""");
    }

    public static void friendsListMenu() {
        System.out.println("[1] Send DiscordFeatures.Message\n" +
                "[2] Block friend\n "+
                "[2] Back");
    }

    public static void channelMenu() {
        System.out.println("[1] React to a message\n" +
                "[2] Reply to a message\n" +
                "[3] Back");
    }
}
