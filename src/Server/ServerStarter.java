package Server;

public class ServerStarter {
    public static void main(String[] args) {
        Server server = new Server(7777);
        Server fileServer = new Server(4321);
        Thread thread = new Thread(server);
        Thread fileThread = new Thread(fileServer);
        thread.start();
        fileThread.start();
    }
}
