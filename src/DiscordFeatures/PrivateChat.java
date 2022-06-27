package DiscordFeatures;

import UserFeatures.User;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class PrivateChat implements Serializable {
    @Serial
    private static final long serialVersionUID = 5043264988957705945L;
    private String person1Username;
    private String person2Username;
    private ArrayList<Message> messages;

    public PrivateChat(String person1Username, String person2Username){
        this.person1Username = person1Username;
        this.person2Username = person2Username;
        messages = new ArrayList<>();
    }

    public void addMessage(Message message){
        System.out.println("message with content " + message.getContent() + "was saved to arrayList");
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
            s = s.concat(message.toString() + "\n");
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChat that = (PrivateChat) o;
        return (getPerson1Username().equals(that.person1Username) && getPerson2Username().equals(that.person2Username)) || (getPerson1Username().equals(that.person2Username) && getPerson2Username().equals(that.person2Username));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerson1Username(), getPerson2Username(), messages);
    }
}
