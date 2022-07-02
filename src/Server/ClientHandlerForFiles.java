package Server;

import java.io.*;
import java.net.Socket;

/**
 * The ClientHandlerForFiles class reads received file, downloads it
 * and saves it in Downloads directory
 *
 * It has a final field that determines user's home directory
 */
public class ClientHandlerForFiles extends ClientHandler implements Runnable{

    private final String home = System.getProperty("user.home");

    /**
     * creates new ClientHandler with given socket
     * @param socket given socket
     */
    public ClientHandlerForFiles(Socket socket) {
        super(socket);
    }

    /**
     * find file's type (String after '.' in file's name)
     * @param fileName String
     * @return file's type (String)
     */
    private String getFileExtension(String fileName){
        int temp = fileName.lastIndexOf('.');
        if (temp > 0)
            return fileName.substring(temp+1);
        else
            return "No extension found";
    }

    /**
     * close dataInputStream, FileOutPutStream and socket
     * @param in DataInputStream over socket
     * @param out FileOutputStream to write read data to new file
     * @param socket Socket
     */
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

    /**
     * this method gives a file name and its type and make an appropriate path
     * this path includes user's home directory + file's name + a number + file's type
     * the number is for making a new file to prevent writing on an existing file
     * @param fileName file's name like "file.txt"
     * @param type file's type like "jpg"
     * @return File with generated path
     */
    private File getTargetFile(String fileName, String type){
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

    /**
     * run method for downloading file
     */
    @Override
    public void run() {
        while (getSocket().isConnected()) {
            DataInputStream dataInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                //make dataInputStream over socket
                dataInputStream = new DataInputStream(super.getSocket().getInputStream());

                //read fileNameBytes length
                int fileNameLength = dataInputStream.readInt();

                //if the file name is valid
                if (fileNameLength > 0) {
                    //read file's name bytes and assign it to fileNameBytes(byte[])
                    byte[] fileNameBytes = new byte[fileNameLength];
                    dataInputStream.readFully(fileNameBytes, 0, fileNameLength);

                    //set fileName with read fileNameBytes
                    String fileName = new String(fileNameBytes);

                    //read fileContentBytes length
                    int fileContentLength = dataInputStream.readInt();

                    //if file's content is valid
                    if (fileContentLength > 0) {
                        //read file's content bytes and assign it to fileContentBytes(byte[])
                        byte[] fileContentBytes = new byte[fileContentLength];
                        dataInputStream.readFully(fileContentBytes, 0, fileContentLength);

                        //make a file to write read data to it
                        File fileToDownload = getTargetFile(fileName, getFileExtension(fileName));

                        //make a fileOutPutStream over this file
                        fileOutputStream = new FileOutputStream(fileToDownload);

                        //write read content to this file
                        fileOutputStream.write(fileContentBytes);
                    }
                }
            } catch (IOException e) {
                shutDown(dataInputStream, fileOutputStream, getSocket());
                return;
            }
        }
    }
}