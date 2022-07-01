package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import DiscordFeatures.*;
import Handler.ResponseStatus;
import Model.Request;
import Model.RequestType;
import Model.Response;
import UserFeatures.Status;
import UserFeatures.User;

public class GeneralClientHandler extends ClientHandler implements Runnable{

    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User user;
    private static File usersFile = new File("./Files/userFiles.txt");
    private static ArrayList<GeneralClientHandler> clientHandlers;
    private static ArrayList<User> users;

    public GeneralClientHandler(User user){
        super();
        this.clientSocket = null;
        this.user = user;
    }

    public GeneralClientHandler(Socket clientSocket){
        super(clientSocket);
//        clientHandlers = new ArrayList<>();
        try {
            oos = new ObjectOutputStream(super.getSocket().getOutputStream());
            ois = new ObjectInputStream(super.getSocket().getInputStream());
        } catch (IOException e) {
            shutDown(ois, oos, clientSocket);
            System.out.println("shut down2");
        }
        this.clientSocket = clientSocket;
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
                    System.out.println("shut down3");
                    return;
                }

                Response response = getResponse(request);

                if (request.getRequestType() == RequestType.SEND_MESSAGE) {
                    try {
                        String username = getChatReceiver((String) request.getData("username"));
                        if (username == null)
                            break;
                        GeneralClientHandler clientHandler = findClientHandler(username);

                        if (clientHandler.oos != null && clientHandler.getUser().isInThisChat(this.user.getUsername())) {
                            clientHandler.oos.writeObject(response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (request.getRequestType() == RequestType.SEND_MESSAGE_TO_CHANNEL) {
                    try {
                        boolean b = getChatReceiver((String) request.getData("serverIndex"), (String) request.getData("channelIndex"));
                        if (!b){
                            System.out.println(request.getData("message"));
                            break;
                        }
                        ArrayList<GeneralClientHandler> clientHandlers = getAllChannelClientHandlers(
                                (String) request.getData("channelIndex"), (String) request.getData("serverIndex"));
                        for (GeneralClientHandler channelClientHandler : clientHandlers) {
                            if (channelClientHandler.oos != null && channelClientHandler.getUser().isInThisChat(user.getCurrentChat())
                                    && user.isBlockedAlready(channelClientHandler.user)) {
                                channelClientHandler.oos.writeUnshared(response);
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
                    break;
                }

            } while (true);
        }
    }

    private ArrayList<GeneralClientHandler> getAllChannelClientHandlers(String channelIndex, String serverIndex) {
        Channel channel = findChannel(channelIndex, serverIndex);

        ArrayList<GeneralClientHandler> channelClientHandlers = new ArrayList<>();
        ArrayList<User> channelUsers = channel.getUsers();
        for (User user : channelUsers) {
            if ( user != this.user) {
                channelClientHandlers.add(findClientHandler(user));
            }
        }

        return channelClientHandlers;

    }



    private String getChatReceiver(String username) {
        PrivateChat pv = (PrivateChat) findChat(username);
        User user = findUser(username);
        if (user.getCurrentChat() == null || !user.getCurrentChat().equals(pv))
            return null;
        return username;
    }

    private boolean getChatReceiver(String serverIndex, String channelIndex) {
        Channel channel = findChannel(channelIndex, serverIndex);

        if (user.getCurrentChat() == null || !user.getCurrentChat().equals(channel))
            return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    public Response getResponse(Request<?> request){

        RequestType requestType = request.getRequestType();
        Response response = null;

        //میتونیم wildcard بالا رو string بنویسیم و همه این کست ها حذف شوند
        switch (requestType) {
            case CHECK_USERNAME -> response = checkUsernameResponse((Request<String>) request);
            case SIGN_UP ->  response = signUpResponse((Request<String>) request);
            case SIGN_IN ->  response = signInResponse((Request<String>) request);
            case SIGN_OUT -> response = signOutResponse();

            case PROFILE_PHOTO -> profilePhotoResponse((Request<String>) request);
            case SET_STATUS ->  response = setStatusResponse((Request<String>) request);
            case CHANGE_PASSWORD -> response = changePassword((Request<String>) request);

            case PRINT_FRIEND_REQUESTS ->  response = printFriendRequestResponse((Request<String>) request);
            case FRIEND_REQUEST ->  response = friendRequestResponse((Request<String>) request);
            case ACCEPT_FRIEND_REQUEST ->  response = acceptFriendRequest((Request<String>) request);
            case DELETE_FRIEND_REQUEST ->  response = deleteFriendRequest((Request<String>) request);
            case PRINT_FRIENDS ->  response = printFriendResponse((Request<String>) request);

            case BLOCK_FRIEND ->  response = blockFriendResponse((Request<String>) request);
            case PRINT_BLOCKED_FRIENDS ->  response = printBlockedUsersResponse((Request<String>) request);
            case UNBLOCK_USER ->  response = unblockFriendResponse((Request<String>) request);

            case PRINT_PRIVATE_CHATS_USERNAMES ->  response = printPrivateChatUsernamesResponse((Request<String>) request);
            case PRIVATE_CHAT_MESSAGES ->  response = privateChatResponse((Request<String>) request);
            case CHECK_USERNAME_FOR_CHAT -> response = checkUsernameForChat((Request<String>) request);
            case ADD_FILE_TO_CHAT -> addFileToChat((Request<String>) request);
            case ADD_FILE_TO_CHANNEL -> addFileToChannel((Request<String>) request);
            case PRINT_FILE_NAMES -> response = printFileNames((Request<String>) request);
            case PRINT_FILE_NAMES_IN_CHANNEL -> response = printFileNamesInChannel((Request<String>) request);
            case DOWNLOAD_FILE -> response = downloadFile((Request<String>) request);
            case DOWNLOAD_FILE_IN_CHANNEL -> response = downLoadFileInChannel((Request<String>) request);
            case CLOSE_CHAT -> response = closeChat();
            case CLOSE_THREAD -> this.user.setChatToNull();
            case SET_CURRENT_CHAT -> setCurrentChat((Request<String>) request);

            case SEND_MESSAGE -> response = sendMessage((Request<String>) request);

            case SEND_MESSAGE_TO_CHANNEL -> response = sendChannelMessage((Request<String>) request);

            case PRINT_SERVERS ->  response = printServers();
            case PRINT_CHANNELS -> response = printChannels((Request<String>)request);
            case NEW_SERVER ->  response = createNewServer((Request<String>)request);
            case INVITE_TO_SERVER -> response = inviteUserToServer((Request<String>) request);
            case ENTER_CHANNEL -> response = enterChannel((Request<String>) request);
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
        Channel channel = user.getServer(serverIndex).getChannel(channelIndex);
        user.setCurrentChat(channel);
        return new Response(ResponseStatus.VALID_STATUS, channel);
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

    private Response printServers() {
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
            channelMessages = user.getServer(serverIndex).getChannel(channelIndex).getMessagesAsString();
        }
//        user.setCurrentChat(user.getServer(serverIndex).getChannel(channelIndex));
        user.setChatToNull();
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
        if (user != null) {
            user.setStatus(Status.OFFLINE);
            user.setChatToNull();
        }
        writeUsersToFile();
        return new Response(ResponseStatus.SIGN_OUT_VALID);
    }

    private void profilePhotoResponse(Request<String> request){
        String profilePhoto = request.getData("profilePhoto");
        this.user.setPfp(new File(profilePhoto));
    }

    private Response changePassword(Request<String> request){
        String username = request.getData("username");
        String newPass = request.getData("pass");
        User user = findUser(username);
        if (user.checkPassword(newPass))
            return new Response(ResponseStatus.SAME_PASSWORD);
        user.setPassword(newPass);
        return new Response(ResponseStatus.VALID_CHANGE_PASSWORD);
    }

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

    private Response checkUsernameForChat(Request<String> request){
        String person2username = request.getData("username");
        User person1 = this.user;
        User person2 = findUser(person2username);

        if (person1 == null || person2 == null)
            return new Response(ResponseStatus.INVALID_USERNAME);

        if(person1.isBlockedAlready(person2) || person2.isBlockedAlready(person1))
            return new Response(ResponseStatus.BLOCKED_USERNAME);

        return new Response(ResponseStatus.VALID_STATUS);
    }

    private Response privateChatResponse(Request<String> privateChatRequest) {
        String person2username = privateChatRequest.getData("username");
        PrivateChat privateChat = (PrivateChat) findChat(person2username);

        this.user.setChatToNull();
        return new Response(ResponseStatus.VALID_STATUS, privateChat.getMessagesAsString());
    }

    private Chat findChat(String username){
        User person1 = this.user;
        User person2 = findUser(username);

        PrivateChat privateChat = person1.doesPrivateChatExist(username);

        if (privateChat == null) {
            privateChat = new PrivateChat(person1.getUsername(), username);
            person1.addPrivateChat(privateChat);
            person2.addPrivateChat(privateChat);
        }
        return privateChat;
    }

    private void setCurrentChat(Request<String> request){
        String person2username = request.getData("username");
        PrivateChat privateChat = (PrivateChat) findChat(person2username);
        this.user.setCurrentChat(privateChat);
    }

    private Response sendMessage(Request<String> request){
        String text = request.getData("message");
        String username = request.getData("username");
        Message message = new Message(text);

        if (text.substring(text.indexOf(":")+2).equals("0"))
            return null;

        findChat(username).addMessage(message);

        return new Response(ResponseStatus.VALID_MESSAGE, message);
    }

    private Response sendChannelMessage(Request<String> request){
        String text = request.getData("message");
        Channel channel = findChannel(request.getData("serverIndex"), request.getData("channelIndex"));
        Message message = new Message(text);
        channel.addMessage(message);
        return new Response(ResponseStatus.VALID_MESSAGE, message);
    }

    private Channel findChannel(String channelIndexStr, String serverIndexStr){
        int serverIndex = Integer.parseInt(serverIndexStr);
        int channelIndex = Integer.parseInt(channelIndexStr);
        return user.getServer(serverIndex).getChannel(channelIndex);
    }

    private void addFileToChat(Request<String> request){
        String username = request.getData("username");
        findChat(username).addFile(new File(request.getData("file")));
    }

    private void addFileToChannel(Request<String> request){
        findChannel(request.getData("channelIndex"), request.getData("serverIndex")).addFile(new File(request.getData("file")));
    }

    private Response printFileNames(Request<String > request){
        return new Response(null, findChat(request.getData("username")).getFilesNames());
    }

    private Response printFileNamesInChannel(Request<String> request){
        return new Response(null, findChannel(request.getData("channelIndex"), request.getData("serverIndex")).getFilesNames());
    }
    private Response downloadFile(Request<String> request){
        Chat chat = findChat(request.getData("username"));
        String fileName = request.getData("fileName");
        File file = chat.findFile(fileName);
        if (file != null)
            return new Response(ResponseStatus.FILE_DOWNLOADED, file);
        return new Response(ResponseStatus.INVALID_FILE_NAME);
    }

    private Response downLoadFileInChannel(Request<String> request){
        Channel channel = findChannel(request.getData("channelIndex"), request.getData("serverIndex"));
        String fileName = request.getData("fileName");
        File file = channel.findFile(fileName);
        if (file != null)
            return new Response(ResponseStatus.FILE_DOWNLOADED, file);
        return new Response(ResponseStatus.INVALID_FILE_NAME);
    }
    private Response closeChat() {
        user.setChatToNull();
        return new Response(ResponseStatus.CLOSE_THREAD);
    }

    private User getUser() { return user; }

    protected static void setUsers(ArrayList<User> users) { GeneralClientHandler.users = users; }

    @SuppressWarnings("unchecked")
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
            clientHandlers.add(new GeneralClientHandler(user));
        }

        return c;
    }

    private void addClientHandler(GeneralClientHandler ch){
        clientHandlers.add(ch);
        users.add(ch.getUser());
        writeUsersToFile();
    }
    private void writeUsersToFile(){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersFile))){
            oos.writeObject(users);
        } catch (IOException e) {
            shutDown(ois, oos, clientSocket);
            System.out.println("shut down1");
        }
    }

    public User findUser(String username){
        for (User user : users) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    public GeneralClientHandler findClientHandler(String username){
        for (GeneralClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUser().getUsername().equals(username))
                return clientHandler;
        }
        return null;
    }

    public GeneralClientHandler findClientHandler(User user){
        for (GeneralClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUser().equals(user))
                return clientHandler;
        }
        return null;
    }




    private void shutDown(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            System.out.println("User disconnected.");
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            signOutResponse();
        }
    }

}