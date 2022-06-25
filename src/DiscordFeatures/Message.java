package DiscordFeatures;

import java.time.LocalDateTime;

public class Message {
    private String messengerUsername;
    private String content;
    private LocalDateTime timeSent;

    public Message(String messengerUsername, String content) {
        this.messengerUsername = messengerUsername;
        this.content = content;
        timeSent = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return  messengerUsername + ':' + content + '(' + timeSent + ')';
    }
}
