package DiscordFeatures;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * The class chat is the superclass of classes private chat and channel,
 * which are the entities in which we can send messages in. The superclass
 * chat allows for these messages to be stored in an arraylist and contains
 * methods that allow for these alterations to happen.
 */
public abstract class Chat implements Serializable {
    @Serial
    private static final long serialVersionUID = -1330740477414674237L;
    private ArrayList<Message> messages;
    private ArrayList<File> files;

    /**
     * Creates new chat by initializing the arrayLists that will contain messages and files
     */
    public Chat() {
        messages = new ArrayList<>();
        files = new ArrayList<>();
    }

    /**
     * adds a message to arrayList messages.
     *
     * @param message the message being added to this chat messages
     */
    public void addMessage(Message message) {
        messages.add(message);
    }

    /**
     * adds a file to arrayList messages.
     *
     * @param file the message being added to this chat files
     */
    public void addFile(File file) {
        files.add(file);
    }

    /**
     * returns the messages of this chat
     *
     * @return ArrayList of Messages
     */
    public ArrayList<Message> getMessages() {
        return messages;
    }

    /**
     * returns all the messages in this chat as a string
     * numbered in the order in which they were stored in the arrayList
     *
     * @return String of all messages numbered
     */
    public String getMessagesAsString() {
        String s = "";
        int i = 1;
        if (messages.isEmpty())
            return "Empty";
        for (Message message : messages) {
            s = s.concat("(" + i + ")" + message.toString() + "\n");
            i++;
        }
        return s;
    }

    /**
     * returns all the messages in this chat as a string
     *
     * @return String of all messages
     */
    public String getMessagesNotNumbered() {
        String s = "";
        if (messages.isEmpty())
            return "Empty";
        for (Message message : messages) {
            s = s.concat(message.toString() + "\n");
        }
        return s;
    }

    /**
     * returns all the names of the files in this chat as a string
     *
     * @return String of all file names
     */
    public String getFilesNames() {
        String s = "";
        if (files.isEmpty())
            return "Empty";
        for (File file : files) {
            s = s.concat(file.getName() + "\n");
        }
        return s;
    }

    /**
     * returns file with file name given as parameter
     *
     * @param fileName String name of file
     * @return File with given name
     */
    public File findFile(String fileName) {
        for (File file : files) {
            if (file.getName().equals(fileName))
                return file;
        }
        return null;
    }

    /**
     * returns message of given index
     *
     * @param index int index of wanted message
     * @return Message of given index
     */
    public Message getMessage(int index) {
        return messages.get(index);
    }

    /**
     * returns the number of messages stored in chats messages
     *
     * @return size of messages
     */
    public int getMessagesSize() {
        return messages.size();
    }
}
