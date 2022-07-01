package Server;

import java.io.IOException;
import java.io.Serial;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private final int port;

    public Server(int port){
        this.port = port;
    }

    @Override
    public void run() {
        GeneralClientHandler.setUsers(GeneralClientHandler.readUsersFile());
        try {
            ServerSocket serverSocket = new ServerSocket(port); //7777
            while (true){
                Socket clientSocket = serverSocket.accept(); //7777
                System.out.println("new user connected");
                Thread thread;
                ClientHandler clientHandler;
                if (port == 7777)
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
