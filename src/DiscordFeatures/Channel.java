package DiscordFeatures;

import UserFeatures.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Channel extends Chat implements Serializable {
    @Serial
    private static final long serialVersionUID = -865468364115954827L;
    private String name;
    private ArrayList<Message> pinnedMessages;
    private ArrayList<User> users;

    public Channel(String name){
        super();
        this.name = name;
        pinnedMessages = new ArrayList<>();
        users = new ArrayList<>();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public String getName() {
        return name;
    }

    public void pinMessage(Message message){
        pinnedMessages.add(message);
    }
}
