package DiscordFeatures;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String content;
    private LocalDateTime timeSent;

    public Message(String content) {
        this.content = content;
        timeSent = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}
