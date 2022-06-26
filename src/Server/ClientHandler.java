package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import DiscordFeatures.Message;
import DiscordFeatures.PrivateChat;
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

    public ObjectOutputStream getOos() {
        return this.oos;
    }

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

    private void shutDown(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setStatus(Status.OFFLINE);
    }

    protected static void setUsers(ArrayList<User> users) {
        ClientHandler.users = users;
    }

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
//        clientHandlers.remove(findClientHandler(ch.getUser().getUsername()));
        clientHandlers.add(ch);
        users.add(ch.getUser());
        writeUsersFile();
    }

    private void writeUsersFile(){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersFile))){
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User getUser() {
        return user;
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

    private Response signUpResponse(Request<String> request) {
        String username = request.getData("username");
        String password = request.getData("password");
        String email = request.getData("email");
        String phoneNum = request.getData("phoneNum");
        this.user = new User(username, password, email, phoneNum);
        addClientHandler(this);
        return new Response(ResponseStatus.SIGNUP_VALID);
    }

    private Response checkUsernameResponse(Request<String> request){
        String username = request.getData("username");
        if (findUser(username) != null)
            return new Response(ResponseStatus.INVALID_USERNAME);
        return new Response(ResponseStatus.VALID_USERNAME);
    }

    private Response signInResponse(Request<String> request){
        String username = request.getData("username");
        String pass = request.getData("pass");
        User user = findUser(username);
        Response response;
        if(user == null || !user.checkPassword(pass))
            response = new Response(ResponseStatus.SIGN_IN_INVALID);
        else{
            response = new Response(ResponseStatus.SIGN_IN_VALID,user);
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
        writeUsersFile();
        return new Response(ResponseStatus.SIGN_OUT_VALID);
    }

    private void profilePhotoResponse(){

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
        String username = request.getData("username");
        User user = findUser(username);
        String data = user.getRequestsListAsString();
        if (data.equals("Empty\n"))
            return new Response(ResponseStatus.EMPTY_FRIEND_REQUEST_LIST,data);
        return new Response(ResponseStatus.NOT_EMPTY_FRIEND_REQUEST_LIST,data);
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
        return new Response(ResponseStatus.VALID_STATUS,user.getFriendsListAsString());
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
        person1.setCurrentPrivateChat(privateChat);
        return new Response(ResponseStatus.VALID_STATUS, privateChat);
    }

    @Override
    public void run() {
        while (true) {

            Request<?> request = null;
            do {

                try {
                    request = (Request<?>) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                Response response = getResponse(request);

                if (request.getRequestType() == RequestType.CLOSE_CHAT) {
                    response = new Response(ResponseStatus.CLOSE_THREAD);
                    try {
                        System.out.println("sent close thread message");
                        oos.writeObject(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (request.getRequestType() == RequestType.SEND_MESSAGE){
                    try {
//                        System.out.println("in run method "+ ((Message)response.getData()).getContent());
//                        ClientHandler clientHandler = y((Request<Object>) request);
                        PrivateChat pv = this.user.getCurrentPrivateChat();
                        String username = pv.getPerson2Username();
                        if (username.equals(this.user.getUsername()))
                            username = pv.getPerson1Username();
                        ClientHandler clientHandler = findClientHandler(username);

                        if (response == null)
                            break;

                        if (clientHandler.oos != null && clientHandler.getUser().isInThisPrivateChat(this.user.getUsername())) {
                            clientHandler.oos.writeObject(response);
                        }
//                        System.out.println("in run method" + ((Message)response.getData()).getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        if (response != null)
                            oos.writeObject(response);
                        System.out.println(response.responseStatusString() + " data: " +  response.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }while (true);
        }
    }
    private PrivateChat x(Request<Object> request){
        String person2username = (String) request.getData("person2");
        ClientHandler person2Handler = findClientHandler(person2username);

        return this.user.doesPrivateChatExist(person2username);
    }

    private ClientHandler y(Request<Object> request){
        String person2username = (String) request.getData("person2");
        return findClientHandler(person2username);
    }

    private Response sendMessage(PrivateChat privateChat, Request<Message> request){
        Message message = request.getData("message");
        String messageContent = message.getContent();
        System.out.println(messageContent);
        if (messageContent.substring(messageContent.indexOf(":")+2).equals("0"))
            return null;
//        System.out.println
//                ("message printed in sendMessage CLientHandler" + message.getContent());
        privateChat.addMessage(message);
        return new Response(ResponseStatus.VALID_MESSAGE, message);
    }

    public Response getResponse(Request<?> request){
        RequestType requestType = request.getRequestType();
        Response response = null;
        switch (requestType) {
            case SEND_MESSAGE -> {
                response = sendMessage(this.user.getCurrentPrivateChat(), (Request<Message>) request);
            }
            case CHECK_USERNAME ->
                response = checkUsernameResponse((Request<String>) request);
            case SIGN_UP ->
                response = signUpResponse((Request<String>) request);
            case SIGN_IN ->
                response = signInResponse((Request<String>) request);
            case SIGN_OUT ->
                response = signOutResponse();
            case PROFILE_PHOTO ->
                profilePhotoResponse();
            case SET_STATUS ->
                response = setStatusResponse((Request<String>) request);
            case PRINT_FRIEND_REQUEST ->
                response = printFriendRequestResponse((Request<String>) request);
            case FRIEND_REQUEST ->
                response = friendRequestResponse((Request<String>) request);
            case ACCEPT_FRIEND_REQUEST ->
                response = acceptFriendRequest((Request<String>) request);
            case DELETE_FRIEND_REQUEST ->
                response = deleteFriendRequest((Request<String>) request);
            case PRINT_FRIENDS ->
                response = printFriendResponse((Request<String>) request);
            case BLOCK_FRIEND ->
                response = blockFriendResponse((Request<String>) request);
            case PRINT_BLOCKED_FRIENDS ->
                response = printBlockedUsersResponse((Request<String>) request);
            case UNBLOCK_USER ->
                response = unblockFriendResponse((Request<String>) request);
            case PRINT_PRIVATE_CHATS_USERNAMES ->
                response = printPrivateChatUsernamesResponse((Request<String>) request);
            case PRIVATE_CHAT ->
                response = privateChatResponse((Request<String>) request);
        }
        return response;
    }

}