package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import Handler.ResponseStatus;
import Model.Request;
import Model.RequestType;
import Model.Response;
import UserFeatures.Status;
import UserFeatures.User;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private User user;
    private static File usersFile = new File("./Files/userFiles.txt");
    private static ArrayList<ClientHandler> clientHandlers;
    private static ArrayList<User> users;

    public ClientHandler(Socket clientSocket){
        clientHandlers = new ArrayList<>();
        this.clientSocket = clientSocket;
        try {
            objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return c;
    }

    private void addClientHandler(ClientHandler ch){
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

    @Override
    public void run() {
        while (true){
            Request<?> request = null;
            try {
                request = (Request<?>) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            Response response = getResponse((Request<String>) request);
            try {
                if (response != null)
                    objectOutputStream.writeObject(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            clientHandlers.add(this);
        }
        return response;
    }

    private Response signOutResponse(){
        if (user.getStatus() == Status.ONLINE)
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
//            writeUsersFile();
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
//            writeUsersFile();
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
//        writeUsersFile();
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
//        writeUsersFile();
        return new Response(ResponseStatus.VALID_DELETE_FRIEND_REQUEST);
    }

    private Response printFriendResponse(Request<String> request){
        String username = request.getData("username");
        User user = findUser(username);
        return new Response(null,user.getFriendsListAsString());
    }

    public Response getResponse(Request<String> request){
        RequestType requestType = request.getRequestType();
        Response response = null;
        switch (requestType) {
            case CHECK_USERNAME ->
                response = checkUsernameResponse(request);
            case SIGN_UP ->
                response = signUpResponse(request);
            case SIGN_IN ->
                response = signInResponse(request);
            case SIGN_OUT ->
                response = signOutResponse();
            case PROFILE_PHOTO ->
                profilePhotoResponse();
            case SET_STATUS ->
                response = setStatusResponse(request);
            case PRINT_FRIEND_REQUEST ->
                response = printFriendRequestResponse(request);
            case FRIEND_REQUEST ->
                response = friendRequestResponse(request);
            case ACCEPT_FRIEND_REQUEST ->
                response = acceptFriendRequest(request);
            case DELETE_FRIEND_REQUEST ->
                response = deleteFriendRequest(request);
            case PRINT_FRIENDS ->
                response = printFriendResponse(request);
        }
        return response;
    }

}