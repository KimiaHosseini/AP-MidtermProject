package DiscordFeatures;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A message is the data sent between users in chats. These messages each contain the
 * text content as well as the time and date it was sent. These messages are saved in chats
 * and are serializable and saved. These messages are sent from client to server and vise versa.
 */
public class Message implements Serializable {
    private final String content;
    private final LocalDateTime timeSent;

    /**
     * Creates message with String parameter and local time
     *
     * @param content String text of message
     */
    public Message(String content) {
        this.content = content;
        timeSent = LocalDateTime.now();
    }

    /**
     * returns the content of the message
     *
     * @return String text content
     */
    public String getContent() {
        return content;
    }

    /**
     * returns the content we want to print everytime we print message
     *
     * @return String content of message
     */
    @Override
    public String toString() {
        return content;
    }
}
