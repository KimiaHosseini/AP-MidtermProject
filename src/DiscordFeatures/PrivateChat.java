package DiscordFeatures;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Private chat is the entity created when two users want to communicate.
 * Each private chat contains both users that are messaging.
 * <p>
 * Private chat extends chat, meaning that it contains an arrayList of all
 * the messages sent by both users in this chat. These chats can be accessed
 * and modified by the messages sent by both users.
 */
public class PrivateChat extends Chat implements Serializable {
    @Serial
    private static final long serialVersionUID = 5043264988957705945L;
    private final String person1Username;
    private final String person2Username;

    /**
     * Creates new Private chat between two users
     *
     * @param person1Username first user trying to communicate
     * @param person2Username user trying to be communicated with
     */
    public PrivateChat(String person1Username, String person2Username) {
        super();
        this.person1Username = person1Username;
        this.person2Username = person2Username;
    }

    /**
     * returns username of the user that created the private chat and initialized contact
     *
     * @return user 1
     */
    public String getPerson1Username() {
        return person1Username;
    }

    /**
     * returns username of the user that was attempted to be communicated with
     *
     * @return user 2
     */
    public String getPerson2Username() {
        return person2Username;
    }

    /**
     * Override equals method, changed so that any two private chats that have equal
     * users in the chat are deemed equal
     *
     * @param o private chat in question
     * @return whether the two private chats are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChat that = (PrivateChat) o;
        return (getPerson1Username().equals(that.person1Username) && getPerson2Username().equals(that.person2Username)) || (getPerson1Username().equals(that.person2Username) && getPerson2Username().equals(that.person2Username));
    }

    /**
     * necessary method created with equals method
     *
     * @return int hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(getPerson1Username(), getPerson2Username(), super.getMessages());
    }
}
