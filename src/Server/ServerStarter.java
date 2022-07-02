package Server;

/**
 * The server starter creates a new servers that each deal with different handlers as well
 * as different requests from clients
 */
public class ServerStarter {
    public static void main(String[] args) {
        Server server = new Server(8888);
        Server fileServer = new Server(9999);
        Thread thread = new Thread(server);
        Thread fileThread = new Thread(fileServer);
        thread.start();
        fileThread.start();
    }
}
