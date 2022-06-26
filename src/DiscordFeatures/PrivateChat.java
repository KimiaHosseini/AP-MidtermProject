package DiscordFeatures;

import UserFeatures.User;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class PrivateChat implements Serializable {
    private String person1Username;
    private String person2Username;
    private ArrayList<Message> messages;

    public PrivateChat(String person1Username, String person2Username){
        this.person1Username = person1Username;
        this.person2Username = person2Username;
        messages = new ArrayList<>();
    }

    public void addMessage(Message message){
        messages.add(message);
    }

    public String getPerson2Username() {
        return person2Username;
    }

    public String getPerson1Username() {
        return person1Username;
    }

    public String getMessagesAsString(){
        String s ="";
        if (messages.isEmpty())
            return "Empty";
        for (Message message : messages) {
            s = s.concat(message.toString());
        }
        return s;
    }
}
