package Client;

import Handler.InputHandler;
import Handler.MenuHandler;
import Handler.RequestStatus;
import Handler.ResponseStatus;
import Model.Request;
import Model.RequestType;
import Model.Response;
import UserFeatures.Status;
import UserFeatures.User;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;
    private InputHandler inputHandler;
    private Scanner scanner;

    public Client(Socket socket) {
        this.socket = socket;
        scanner = new Scanner(System.in);
        inputHandler = new InputHandler();
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            shutDown(in, out, socket);
        }
    }

    private void sendRequest(Request<?> request) {
        try {
            out.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response giveResponse() {
        Response response = null;
        try {
            response = (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void runClient() {
        System.out.println("\t".repeat(5) + "Welcome to Discord!");
        mainLoop:
        while (!socket.isClosed()) {
            MenuHandler.welcomeMenu();
            String whatToDO = scanner.nextLine();

            switch (whatToDO) {
                case "1": {
                    Request<String> request;
                    String username, pass, email, phoneNum;
                    do {
                        username = inputHandler.getInfo("username");
                        RequestStatus requestStatus = inputHandler.checkInfo(username);
                        if (requestStatus == RequestStatus.INVALID)
                            continue;
                        if (requestStatus == RequestStatus.BACK) {
                            continue mainLoop;
                        }
                        request = new Request<>(RequestType.CHECK_USERNAME);
                        request.addData("username", username);
                        sendRequest(request);

                        Response response = giveResponse();

                        if (response.getResponseStatus() == ResponseStatus.INVALID_USERNAME)
                            System.out.println("Duplicate username");
                        else
                            break;
                    } while (true);

                    do {
                        pass = inputHandler.getInfo("pass");
                        RequestStatus requestStatus = inputHandler.checkInfo(pass);
                        if (requestStatus == RequestStatus.VALID)
                            break;
                        else if (requestStatus == RequestStatus.BACK) {
                            continue mainLoop;
                        }
                    } while (true);

                    do {
                        email = inputHandler.getInfo("email");
                        RequestStatus requestStatus = inputHandler.checkInfo(email);
                        if (requestStatus == RequestStatus.VALID)
                            break;
                        else if (requestStatus == RequestStatus.BACK) {
                            continue mainLoop;
                        }
                    } while (true);

                    do {
                        phoneNum = inputHandler.getInfo("phoneNum");
                        RequestStatus requestStatus = inputHandler.checkInfo(phoneNum);
                        if (requestStatus == RequestStatus.VALID)
                            break;
                        else if (requestStatus == RequestStatus.BACK) {
                            continue mainLoop;
                        }
                    } while (true);

                    request = new Request<>(RequestType.SIGN_UP);
                    request.addData("username", username);
                    request.addData("password", pass);
                    request.addData("email", email);
                    request.addData("phoneNum", phoneNum);

                    sendRequest(request);
                    Response response = giveResponse();
                    if (response.getResponseStatus() == ResponseStatus.SIGNUP_VALID) {
                        System.out.println("successfully signed up");
                    }
                    break;
                }

                case "2": {
                    do {
                        Request<String> request;
                        String username, pass;
                        username = inputHandler.getString("username: ");
                        if (username.equals("0"))
                            continue mainLoop;
                        pass = inputHandler.getString("pass: ");
                        if (pass.equals("0"))
                            continue mainLoop;
                        request = new Request<>(RequestType.SIGN_IN);
                        request.addData("username", username);
                        request.addData("pass",pass);
                        sendRequest(request);

                        Response response = giveResponse();

                        if (response.getResponseStatus() == ResponseStatus.SIGN_IN_INVALID)
                            System.out.println("Wrong username or password");
                        else if (response.getResponseStatus() == ResponseStatus.SIGN_IN_VALID) {
                            this.user = (User) response.getData();
                            System.out.println("Successfully signed in");
                            break;
                        }
                    } while (true);

                    userLoop:
                    do {
                        MenuHandler.userStarterMenu();
                        whatToDO = scanner.nextLine();
                        switch (whatToDO) {
                            case "7" -> {
                                accountSettingLoop:
                                do {
                                    MenuHandler.settingAccountMenu();
                                    whatToDO = scanner.nextLine();
                                    switch (whatToDO) {
                                        case "1": {
                                            String imagePath = inputHandler.getString("image path: ");
                                            if (imagePath.equals("0"))
                                                continue userLoop;
                                            Request<String> request = new Request<>(RequestType.PROFILE_PHOTO);
                                            request.addData("profilePhoto", imagePath);
                                            sendRequest(request);
                                            Response response = giveResponse();
                                            break;
                                        }
                                        case "2": {
                                            MenuHandler.statusMenu();
                                            int statusIndex = scanner.nextInt();
                                            scanner.nextLine();
                                            if (statusIndex == 0)
                                                continue userLoop;
                                            Request<String> request = new Request<>(RequestType.SET_STATUS);
//                                            request.addData("");
                                            request.addData("status", Integer.toString(statusIndex-1));
                                            sendRequest(request);
                                            Response response = giveResponse();
                                            if (response.getResponseStatus() == ResponseStatus.VALID_STATUS)
                                                System.out.println("successfully done");
                                            else if ( response.getResponseStatus() == ResponseStatus.INVALID_STATUS)
                                                System.out.println("invalid input");
                                            break;
                                        }
                                        case "0":
                                            break accountSettingLoop;
                                    }
                                } while (true);
                            }
                            case "3" ->{
                                Request<String> printFriends = new Request<>(RequestType.PRINT_FRIENDS);
                                printFriends.addData("username", user.getUsername());
                                sendRequest(printFriends);
                                Response response = giveResponse();
                                System.out.println((String) response.getData());
                            }
                            case "4" -> {
                                do {
                                    Request<String> printFriendRequests = new Request<>(RequestType.PRINT_FRIEND_REQUEST);
                                    printFriendRequests.addData("username", user.getUsername());
                                    sendRequest(printFriendRequests);
                                    Response response = giveResponse();
                                    //print requests list
                                    System.out.print((String) response.getData());
                                    if (response.getResponseStatus() == ResponseStatus.EMPTY_FRIEND_REQUEST_LIST)
                                        continue userLoop;

                                    System.out.println("write a request's username to accept or delete: ");
                                    String requester = scanner.nextLine();
                                    if (requester.equals("0"))
                                        continue userLoop;
                                    System.out.println("[1] Accept\n[2] Delete\n[0] Exit");
                                    whatToDO = scanner.nextLine();
                                    switch (whatToDO) {
                                        case "1":
                                            Request<String> acceptFriendRequest = new Request<>(RequestType.ACCEPT_FRIEND_REQUEST);
                                            acceptFriendRequest.addData("receiver", user.getUsername());
                                            acceptFriendRequest.addData("requester", requester);
                                            sendRequest(acceptFriendRequest);
                                            response = giveResponse();
                                            if (response.getResponseStatus() == ResponseStatus.VALID_ACCEPT_FRIEND_REQUEST)
                                                System.out.println("Now " + requester + " is your friend");
                                            else if (response.getResponseStatus() == ResponseStatus.INVALID_ACCEPT_FRIEND_REQUEST)
                                                System.out.println("Invalid input");
                                            break;
                                        case "2":
                                            Request<String> deleteFriendRequest = new Request<>(RequestType.DELETE_FRIEND_REQUEST);
                                            deleteFriendRequest.addData("receiver",user.getUsername());
                                            deleteFriendRequest.addData("requester",requester);
                                            sendRequest(deleteFriendRequest);
                                            response = giveResponse();
                                            if (response.getResponseStatus() == ResponseStatus.VALID_DELETE_FRIEND_REQUEST)
                                                System.out.println(requester + " is removed from your requests");
                                            else if (response.getResponseStatus() == ResponseStatus.INVALID_DELETE_FRIEND_REQUEST)
                                                System.out.println("Invalid input");
                                            break;
                                        case "0":
                                            continue userLoop;
                                    }
                                } while (true);
                            }
                            case "5" -> {
                                do {
                                    String receiver = inputHandler.getString("username: ");
                                    if (receiver.equals("0"))
                                        continue userLoop;
                                    Request<String> request = new Request<>(RequestType.FRIEND_REQUEST);
                                    request.addData("receiver", receiver);
                                    request.addData("requester", user.getUsername());
                                    sendRequest(request);
                                    Response response = giveResponse();
                                    if (response.getResponseStatus() == ResponseStatus.FRIEND_REQUEST_TO_FRIEND)
                                        System.out.println("This user is your friend!");
                                    else if (response.getResponseStatus() == ResponseStatus.INVALID_USERNAME)
                                        System.out.println("Invalid input");
                                    else if (response.getResponseStatus() == ResponseStatus.DUPLICATE_FRIEND_REQUEST)
                                        System.out.println("You have already sent request to this user");
                                    else if (response.getResponseStatus() == ResponseStatus.VALID_FRIEND_REQUEST) {
                                        System.out.println("Successfully sent");
                                        break;
                                    }
                                } while (true);
                            }
                            case "0" -> {
                                Request<?> request = new Request<>(RequestType.SIGN_OUT);
                                sendRequest(request);
                                Response response = giveResponse();
                                if (response.getResponseStatus() == ResponseStatus.SIGN_OUT_VALID)
                                    break userLoop;
                            }
                        }
                    } while (true);
                    break;
                }
                case "0": {
                    System.exit(0);
                }
            }
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
    }

    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("localhost", 4321);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Client client = new Client(socket);
        client.runClient();
    }
}
