package Server;

import java.net.Socket;

/**
 * The abstract class ClientHandler is the parent superclass of classes GeneralClientHandler
 * as well as ClientHandlerForFiles. ClientHandlers must connect to the server with sockets and
 * this inheritance enhances this feature.
 */
public abstract class ClientHandler extends Thread{
    private Socket socket;

    /**
     * creates new ClientHandler with given socket
     * @param socket given socket
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * constructor for subclasses
     */
    public ClientHandler(){}

    /**
     * @return returns socket of this ClientHandler
     */
    public Socket getSocket() {
        return socket;
    }
}
