package DiscordFeatures;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    @Serial
    private static final long serialVersionUID = -1330740477414674237L;
    private ArrayList<Message> messages;
    private ArrayList<File> files;

    public Chat(){
        messages = new ArrayList<>();
        files = new ArrayList<>();
    }

    public void addMessage(Message message){
        messages.add(message);
    }

    public void addFile(File file){
        files.add(file);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public String getMessagesAsString(){
        String s ="";
        if (messages.isEmpty())
            return "Empty";
        for (Message message : messages) {
            s = s.concat(message.toString() + "\n");
        }
        return s;
    }

    public String getFilesNames(){
        String s = "";
        if (files.isEmpty())
            return "Empty";
        for (File file : files) {
            s = s.concat(file.getName() + "\n");
        }
        return s;
    }

    public File findFile(String fileName){
        for (File file : files) {
            if (file.getName().equals(fileName))
                return file;
        }
        return null;
    }
}
