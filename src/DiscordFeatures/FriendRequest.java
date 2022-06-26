package DiscordFeatures;

import UserFeatures.User;

public class FriendRequest {
    private User requester;
    private User receiver;

    public FriendRequest(User requester, User receiver) {
        this.requester = requester;
        this.receiver = receiver;
    }

    public User getReceiver() {
        return receiver;
    }

    public User getRequester() {
        return requester;
    }
}
