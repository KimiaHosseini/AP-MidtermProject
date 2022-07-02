package DiscordFeatures;

import UserFeatures.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Each server contains multiple channels in which users can send chats in.
 * Channel extends class Chat meaning that users are able to send messages and
 * have them stored in the chats messages.
 */
public class Channel extends Chat implements Serializable {
    @Serial
    private static final long serialVersionUID = -865468364115954827L;
    private final String name;
    private ArrayList<Message> pinnedMessages;
    private ArrayList<User> users;
    private ArrayList<User> viewOnlyUsers;

    /**
     * Creates new channel with given name and initializes necessary arrayLists
     *
     * @param name String name of channel
     */
    public Channel(String name) {
        super();
        this.name = name;
        pinnedMessages = new ArrayList<>();
        users = new ArrayList<>();
        viewOnlyUsers = new ArrayList<>();
    }

    /**
     * returns name of channel
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * removes a user from the arrayList of users of this channel
     *
     * @param user given user to be removed
     */
    public void removeUser(User user) {
        users.remove(user);
    }

    /**
     * adds a given user to the viewOnly users of this chat
     *
     * @param user user to be made view only
     */
    public void makeViewOnly(User user) {
        viewOnlyUsers.add(user);
    }

    /**
     * @param user given user
     * @return whether the user is a user in this channel or not
     */
    public boolean containsUser(User user) {
        return users.contains(user);
    }

    /**
     * @param user given user
     * @return whether the user is a user in the viewOnly arrayList or not
     */
    public boolean isViewOnly(User user) {
        return viewOnlyUsers.contains(user);
    }

    /**
     * @return users of this channel
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * sets users of this channel equal to given ArrayList of users
     *
     * @param users ArrayList of users to be set
     */
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    /**
     * adds given user to list of channel users
     *
     * @param user user to be added to channel users
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * adds chosen message from chat messages to pinned messages arraylist of Channel.
     *
     * @param messageIndex int message index of message from chat superclass
     * @return whether the message was added successfully or not,
     * adding message is not successful when the message index is not valid
     */
    public boolean pinMessage(int messageIndex) {
        if (messageIndex > super.getMessagesSize())
            return false;
        if (!pinnedMessages.contains(super.getMessage(messageIndex - 1)))
            pinnedMessages.add(super.getMessage(messageIndex - 1));
        return true;
    }

    /**
     * Returns messages stored in pinnedMessages arrayList
     * as string viewable by client
     *
     * @return String of pinned messages
     */
    public String getPinnedMessages() {
        String s = "";
        if (pinnedMessages.isEmpty())
            return "Empty";
        for (Message message : pinnedMessages) {
            s = s.concat(message.toString() + "\n");
        }
        return s;
    }
}
