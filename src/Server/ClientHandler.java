package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import DiscordFeatures.*;
import Handler.ResponseStatus;
import Model.Request;
import Model.RequestType;
import Model.Response;
import UserFeatures.Status;
import UserFeatures.User;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User user;
    private static File usersFile = new File("./Files/userFiles.txt");
    private static ArrayList<ClientHandler> clientHandlers;
    private static ArrayList<User> users;

    public ClientHandler(User user){
        this.user = user;
    }

    public ClientHandler(Socket clientSocket){
//        clientHandlers = new ArrayList<>();
        this.clientSocket = clientSocket;
        try {
            oos = new ObjectOutputStream(this.clientSocket.getOutputStream());
            ois = new ObjectInputStream(this.clientSocket.getInputStream());
        } catch (IOException e) {
            shutDown(ois, oos, clientSocket);
        }
    }

    @Override
    public void run() {
        while (true) {

            Request<?> request;
            do {

                try {
                    request = (Request<?>) ois.readUnshared();
                } catch (IOException | ClassNotFoundException e) {
                    shutDown(ois, oos, clientSocket);
                    return;
                }

                Response response = getResponse(request);

                if (request.getRequestType() == RequestType.SEND_MESSAGE) {
                    try {
                        ClientHandler clientHandler = findClientHandler(getPrivateChatReceiver());
                        if (clientHandler.oos != null && clientHandler.getUser().isInThisChat(this.user.getUsername())) {
                            clientHandler.oos.writeUnshared(response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (request.getRequestType() == RequestType.SEND_MESSAGE_TO_CHANNEL) {
                    try {
                        ArrayList<ClientHandler> clientHandlers = getAllChannelClientHandlers((Request<String>) request);
                        for (ClientHandler channelclientHandler : clientHandlers) {
                            if (channelclientHandler.oos != null && channelclientHandler.getUser().isInThisChat((Channel) user.getCurrentChat())) {
                                channelclientHandler.oos.writeUnshared(response);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response != null)
                            oos.writeUnshared(response);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("ResponseStatus: " + response.responseStatusString() + " data: " + response.getData());
                    break;
                }

            } while (true);
        }
    }

    private ArrayList<ClientHandler> getAllChannelClientHandlers(Request<String> request) {
        System.out.println("is get all channell client handlers being called??");
//        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
//        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        Channel channel = (Channel)user.getCurrentChat();

        ArrayList<ClientHandler> channelClientHandlers = new ArrayList<ClientHandler>();
        ArrayList<User> channelUsers = channel.getUsers();
        for (User user : channelUsers) {
            if ( user != this.user) {
                channelClientHandlers.add(findClientHandler(user));
            }
        }

        return channelClientHandlers;

    }



    private String getPrivateChatReceiver() {
        PrivateChat pv = (PrivateChat) this.user.getCurrentChat();
        String username = pv.getPerson2Username();
        if (username.equals(this.user.getUsername()))
            username = pv.getPerson1Username();

        return username;
    }


    public Response getResponse(Request<?> request){

        RequestType requestType = request.getRequestType();
        Response response = null;

        switch (requestType) {
            case CHECK_USERNAME ->  response = checkUsernameResponse((Request<String>) request);

            case SIGN_UP ->  response = signUpResponse((Request<String>) request);
            case SIGN_IN ->  response = signInResponse((Request<String>) request);
            case SIGN_OUT -> response = signOutResponse();

            case PROFILE_PHOTO -> profilePhotoResponse();
            case SET_STATUS ->  response = setStatusResponse((Request<String>) request);

            case PRINT_FRIEND_REQUESTS ->  response = printFriendRequestResponse((Request<String>) request);
            case FRIEND_REQUEST ->  response = friendRequestResponse((Request<String>) request);
            case ACCEPT_FRIEND_REQUEST ->  response = acceptFriendRequest((Request<String>) request);
            case DELETE_FRIEND_REQUEST ->  response = deleteFriendRequest((Request<String>) request);
            case PRINT_FRIENDS ->  response = printFriendResponse((Request<String>) request);

            case BLOCK_FRIEND ->  response = blockFriendResponse((Request<String>) request);
            case PRINT_BLOCKED_FRIENDS ->  response = printBlockedUsersResponse((Request<String>) request);
            case UNBLOCK_USER ->  response = unblockFriendResponse((Request<String>) request);

            case SEND_MESSAGE -> response = sendMessage(user.getCurrentChat(), (Request<Message>) request);
            case PRINT_PRIVATE_CHATS_USERNAMES ->  response = printPrivateChatUsernamesResponse((Request<String>) request);
            case PRIVATE_CHAT ->  response = privateChatResponse((Request<String>) request);
            case CLOSE_CHAT -> response = closeChat();

            case PRINT_SERVERS ->  response = printServers((Request<String>)request);
            case PRINT_CHANNELS -> response = printChannels((Request<String>)request);
            case NEW_SERVER ->  response = createNewServer((Request<String>)request);
            case INVITE_TO_SERVER -> response = inviteUserToServer((Request<String>) request);
            case ENTER_CHANNEL -> response = enterChannel((Request<String>) request);
            case SEND_MESSAGE_TO_CHANNEL -> response = sendMessage(user.getCurrentChat(), (Request<Message>) request);
            case GET_CHANNEL -> response = getChannel((Request<String>) request);

            case NEW_CHANNEL -> response = createNewChannel((Request<String>) request);
            case DELETE_CHANNEL -> response = deleteChannel((Request<String>) request);
            case CHANGE_SERVER_NAME -> response = changeServerName((Request<String>) request);
            case DELETE_SERVER -> response = deleteServer((Request<String>) request);
        }
        return response;
    }

    private Response deleteServer(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        DiscordServer server = user.getServer(serverIndex);
        ArrayList<User> members = user.getServer(serverIndex).getMembers();
        for (User user : members) {
            user.removeServer(server);
        }

        return new Response(ResponseStatus.VALID_STATUS);
    }

    private Response changeServerName(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        user.getServer(serverIndex).setName(request.getData("newName"));
        return new Response(ResponseStatus.VALID_STATUS);
    }

    private Response deleteChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        if (user.getServer(serverIndex).getChannel(channelIndex) == null) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }
        user.getServer(serverIndex).deleteChannel(channelIndex);
        return new Response(ResponseStatus.VALID_STATUS);
    }

    private Response getChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(serverIndex).getChannel(channelIndex));
    }

    private Response createNewChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        user.getServer(serverIndex).addChannel(request.getData("name"));
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(serverIndex).getRecentlyAddedChannel());
    }

    private Response inviteUserToServer(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        User invitedFriend = findUser(request.getData("username"));
        if(!user.isInFriends(invitedFriend)){
            return new Response(ResponseStatus.INVALID_USERNAME);
        } else if(invitedFriend.isInServer(user.getServer(serverIndex))) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }

        invitedFriend.addServer(user.getServer(serverIndex));
        user.getServer(serverIndex).addMember(invitedFriend);
        return new Response(ResponseStatus.VALID_STATUS, invitedFriend);
    }

    private Response printServers(Request<String> request) {
        return new Response(ResponseStatus.VALID_STATUS, user.serversToString());
    }

    private Response printChannels(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("requestedServer"));
        if (user.getServer(serverIndex) == null) {
            return new Response(ResponseStatus.VALID_STATUS, "This server has been deleted.");
        }
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(serverIndex).getChannelsToString());
    }

    private Response createNewServer(Request<String> request) {
        String serverName = request.getData("name");
        DiscordServer discordServer = new DiscordServer(serverName, user);
        user.addServer(discordServer);
        return new Response(ResponseStatus.VALID_STATUS, discordServer);
    }

    private Response enterChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        if (user.getServer(serverIndex).getChannel(channelIndex) == null) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }
        String channelMessages = "You do not have permission to view chat history.";
        if (user.getServer(serverIndex).haveThisAccessibility(user, Permissions.VIEW_CHAT_HISTORY)) {
            channelMessages = user.getServer(serverIndex).getChannel(channelIndex).getMessages();
        }
        user.setCurrentChat(user.getServer(serverIndex).getChannel(channelIndex));
        return new Response(ResponseStatus.VALID_STATUS, channelMessages);
    }






    private Response checkUsernameResponse(Request<String> request){
        if (findUser(request.getData("username")) != null)
            return new Response(ResponseStatus.INVALID_USERNAME);

        return new Response(ResponseStatus.VALID_USERNAME);
    }

    private Response signUpResponse(Request<String> request) {
        this.user = new User(request.getData("username"), request.getData("password"), request.getData("email"), request.getData("phoneNum"));
        addClientHandler(this);
        return new Response(ResponseStatus.SIGNUP_VALID);
    }


    private Response signInResponse(Request<String> request){
        User user = findUser(request.getData("username"));
        Response response;
        if(user == null || !user.checkPassword(request.getData("pass")))
            response = new Response(ResponseStatus.SIGN_IN_INVALID);
        else{
            response = new Response(ResponseStatus.SIGN_IN_VALID, user);
            if (user.getStatus() == Status.OFFLINE)
                user.setStatus(Status.ONLINE);
            this.user = user;
            clientHandlers.remove(findClientHandler(this.user.getUsername()));
            clientHandlers.add(this);
        }

        return response;
    }

    private Response signOutResponse(){
        user.setStatus(Status.OFFLINE);
        writeUsersToFile();
        return new Response(ResponseStatus.SIGN_OUT_VALID);
    }

    private void profilePhotoResponse(){ }

    private Response setStatusResponse(Request<String> request){
        int statusIndex = Integer.parseInt(request.getData("status"));
        if (statusIndex >= 0 && statusIndex < 5){
            this.user.setStatus(statusIndex);
            return new Response(ResponseStatus.VALID_STATUS);
        }
        return new Response(ResponseStatus.INVALID_STATUS);
    }

    private Response printFriendRequestResponse(Request<String> request){
        User user = findUser(request.getData("username"));
        String data = user.getRequestsListAsString();
        if (data.equals("Empty\n"))
            return new Response(ResponseStatus.EMPTY_FRIEND_REQUEST_LIST, data);
        return new Response(ResponseStatus.NOT_EMPTY_FRIEND_REQUEST_LIST, data);
    }

    private Response friendRequestResponse(Request<String> request){
        String receiverUsername = request.getData("receiver");
        String requesterUsername = request.getData("requester");
        User receiver = findUser(receiverUsername);
        User requester = findUser(requesterUsername);
        Response response;
        if(receiver == null)
            response = new Response(ResponseStatus.INVALID_USERNAME);
        else if(receiver.isRequestedAlready(requester))
            response = new Response(ResponseStatus.DUPLICATE_FRIEND_REQUEST);
        else if (receiver.isInFriends(requester))
            response = new Response(ResponseStatus.FRIEND_REQUEST_TO_FRIEND);
        else {
            response = new Response(ResponseStatus.VALID_FRIEND_REQUEST);
            receiver.addRequest(requester);
        }
        return response;
    }

    private Response acceptFriendRequest(Request<String> request){
        String receiverUsername = request.getData("receiver");
        String requesterUsername = request.getData("requester");
        User receiver = findUser(receiverUsername);
        User requester = receiver.findRequest(requesterUsername);
        if (requester == null)
            return new Response(ResponseStatus.INVALID_ACCEPT_FRIEND_REQUEST);
        receiver.acceptRequest(requester);
        return new Response(ResponseStatus.VALID_ACCEPT_FRIEND_REQUEST);
    }

    private Response deleteFriendRequest(Request<String> request){
        String receiverUsername = request.getData("receiver");
        String requesterUsername = request.getData("requester");
        User receiver = findUser(receiverUsername);
        User requester = receiver.findRequest(requesterUsername);
        if (requester == null)
            return new Response(ResponseStatus.INVALID_DELETE_FRIEND_REQUEST);
        receiver.removeRequest(requester);
        return new Response(ResponseStatus.VALID_DELETE_FRIEND_REQUEST);
    }

    private Response printFriendResponse(Request<String> request){
        String username = request.getData("username");
        User user = findUser(username);
        return new Response(ResponseStatus.VALID_STATUS, user.getFriendsListAsString());
    }

    private Response printBlockedUsersResponse(Request<String> request){
        String username = request.getData("username");
        User user = findUser(username);
        String data = user.getBlockedListAsString();
        if (data.equals("Empty\n"))
            return new Response(ResponseStatus.EMPTY_BLOCKED_USERS_LIST,data);
        return new Response(ResponseStatus.NOT_EMPTY_BLOCKED_USERS_LIST,data);
    }

    private Response blockFriendResponse(Request<String> request){
        String blockedUsername = request.getData("blocked");
        String requesterUsername = request.getData("requester");
        User blocked = findUser(blockedUsername);
        User requester = findUser(requesterUsername);
        Response response;
        if(blocked == null || !requester.isInFriends(blocked))
            response = new Response(ResponseStatus.INVALID_BLOCK);
        else if(requester.isBlockedAlready(blocked))
            response = new Response(ResponseStatus.DUPLICATE_BLOCK);
        else {
            response = new Response(ResponseStatus.VALID_BLOCK);
            requester.addBlockedUser(blocked);
        }
        return response;
    }

    private Response unblockFriendResponse(Request<String> request){
        String unblockUsername = request.getData("unblocked");
        String requesterUsername = request.getData("requester");
        User unblock = findUser(unblockUsername);
        User requester = findUser(requesterUsername);
        Response response;
        if(unblock == null || !requester.isBlockedAlready(unblock))
            response = new Response(ResponseStatus.INVALID_UNBLOCK);
        else {
            response = new Response(ResponseStatus.VALID_UNBLOCK);
            requester.unblock(unblock);
        }
        return response;
    }

    private Response printPrivateChatUsernamesResponse(Request<String> request){
        String username = request.getData("username");
        User user = findUser(username);
        return new Response(ResponseStatus.VALID_STATUS,user.getPrivateChatsUsernamesListAsString());
    }

    private Response privateChatResponse(Request<String> privateChatRequest) {
        String person2username = privateChatRequest.getData("username");
        User person1 = this.user;
        User person2 = findUser(person2username);

        if (person1 == null || person2 == null)
            return new Response(ResponseStatus.INVALID_USERNAME);

        PrivateChat privateChat = person1.doesPrivateChatExist(person2username);

        if (privateChat == null) {
            privateChat = new PrivateChat(person1.getUsername(), person2username);
            person1.addPrivateChat(privateChat);
            person2.addPrivateChat(privateChat);
        }

        person1.setCurrentChat(privateChat);

        return new Response(ResponseStatus.VALID_STATUS, privateChat.getMessagesAsString());
    }

    private Response sendMessage(Chat chat, Request<Message> request){

        Message message = request.getData("message");
        String messageContent = message.getContent();
        System.out.println(messageContent);

        if (messageContent.substring(messageContent.indexOf(":")+2).equals("0"))
            return null;

        chat.addMessage(message);
        //privateChat.addMessage(message);

        return new Response(ResponseStatus.VALID_MESSAGE, message);
    }


    private Response closeChat() {
        user.setChatToNull();
        return new Response(ResponseStatus.CLOSE_THREAD);
    }






    private User getUser() { return user; }

    public ObjectOutputStream getOos() {
        return this.oos;
    }

    protected static void setUsers(ArrayList<User> users) { ClientHandler.users = users; }

    protected static ArrayList<User> readUsersFile() {
        ArrayList<User> c = new ArrayList<>();
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(usersFile))){
            c = (ArrayList<User>) ois.readObject();
        }catch (EOFException ignored){}
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        clientHandlers = new ArrayList<>();
        for (User user : c) {
            clientHandlers.add(new ClientHandler(user));
        }

        return c;
    }

    private void addClientHandler(ClientHandler ch){
        clientHandlers.add(ch);
        users.add(ch.getUser());
        writeUsersToFile();
    }
    private void writeUsersToFile(){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersFile))){
            oos.writeObject(users);
        } catch (IOException e) {
            shutDown(ois, oos, clientSocket);
        }
    }

    public User findUser(String username){
        for (User user : users) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    public ClientHandler findClientHandler(String username){
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUser().getUsername().equals(username))
                return clientHandler;
        }
        return null;
    }

    public ClientHandler findClientHandler(User user){
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUser().equals(user))
                return clientHandler;
        }
        return null;
    }




    private void shutDown(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            System.out.println("User disconnected.");
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            signOutResponse();
        }
    }

}