package Server;

import java.io.IOException;
import java.io.Serial;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    private void startServer(){
        ClientHandler.setUsers(ClientHandler.readUsersFile());
        try {
            while (!serverSocket.isClosed()){
                //wait to connect to a client
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                //start this client's work on a thread
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
//                clientHandler.run();
            }
        } catch (IOException e) {
            closeServer();
        }
    }

    /**
     * close serverSocket
     */
    private void closeServer(){
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * main method
     * @param args String[]
     * @throws IOException when can not make a serverSocket
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4321);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
