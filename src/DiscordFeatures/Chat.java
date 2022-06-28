package DiscordFeatures;

import java.io.Serializable;

public abstract class Chat implements Serializable {
    public abstract void addMessage(Message message);
}
