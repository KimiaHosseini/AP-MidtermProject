package Server;

import java.net.Socket;

public class ClientHandler extends Thread{
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public ClientHandler(){}

    public Socket getSocket() {
        return socket;
    }
}
