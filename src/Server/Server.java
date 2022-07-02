package Server;

import java.io.IOException;
import java.io.Serial;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server contains the server port that clients connect to and wait for clients to connect.
 * Each client that connects has a client Handler created for them that is run on a separate thread.
 */
public class Server implements Runnable{
    private final int port;

    /**
     * creates new Server with given port
     * @param port given port that server will be run on and clients will connect to
     */
    public Server(int port){
        this.port = port;
    }

    /**
     * run method that will be in continuous loop waiting for new clients that want to connect to server.
     * Each client that connects to server has client handlers created for them based on their need and each
     * of these handlers are then run on a separate thread so that the server can continue to wait for new clients.
     */
    @Override
    public void run() {
        GeneralClientHandler.setUsers(GeneralClientHandler.readUsersFile());
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("new user connected");
                Thread thread;
                ClientHandler clientHandler;
                if (port == 8888)
                    clientHandler = new GeneralClientHandler(clientSocket);
                else
                    clientHandler = new ClientHandlerForFiles(clientSocket);
                thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
