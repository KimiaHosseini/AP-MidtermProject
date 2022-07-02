package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandlerForFiles extends ClientHandler implements Runnable{
    public ClientHandlerForFiles(Socket socket) {
        super(socket);
    }
    private final String home = System.getProperty("user.home");

    private String getFileExtension(String fileName){
        int temp = fileName.lastIndexOf('.');
        if (temp > 0)
            return fileName.substring(temp+1);
        else
            return "No extension found";
    }

    private void shutDown(DataInputStream in, FileOutputStream out, Socket socket) {
        try {
            System.out.println("User disconnected.");
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void file() {
        while (true) {
            DataInputStream dataInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                dataInputStream = new DataInputStream(super.getSocket().getInputStream());
                if (!getSocket().isConnected()){
                    return;
                }
                int fileNameLength = dataInputStream.readInt();

                System.out.println(fileNameLength);

                if (fileNameLength > 0) {
                    byte[] fileNameBytes = new byte[fileNameLength];
                    dataInputStream.readFully(fileNameBytes, 0, fileNameLength);
                    String fileName = new String(fileNameBytes);

                    System.out.println(fileName);

                    int fileContentLength = dataInputStream.readInt();
                    if (fileContentLength > 0) {
                        byte[] fileContentBytes = new byte[fileContentLength];
                        dataInputStream.readFully(fileContentBytes, 0, fileContentLength);

                        System.out.println("read it");


                        File fileToDownload = getFile(fileName, getFileExtension(fileName));
                        fileOutputStream = new FileOutputStream(fileToDownload);
                        fileOutputStream.write(fileContentBytes);

                    }
                }
            } catch (IOException e) {
                shutDown(dataInputStream, fileOutputStream, getSocket());
                return;
            }
        }
    }

    private File getFile(String fileName, String type){
        File fileToDownload;
        String s = "";
        int i = 0;
        do{
            fileToDownload = new File(home + "\\Downloads\\" + fileName.substring(0,fileName.indexOf('.')) + s + "." + type);
            if (!fileToDownload.exists())
                break;
            i++;
            s = Integer.toString(i);
        }while (true);
        return fileToDownload;
    }

    @Override
    public void run() {
        file();
    }
}