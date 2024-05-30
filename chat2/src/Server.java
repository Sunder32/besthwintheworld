import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 8888;
    private static Set<String> userNames = new HashSet<>();
    private static Set<UserThread> userThreads = new HashSet<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Сервер запущен на порту " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Новое подключение: " + socket);

            UserThread newUser = new UserThread(socket);
            userThreads.add(newUser);
            newUser.start();
        }
    }

    public static void broadcast(String message, UserThread excludeUser) {
        for (UserThread user : userThreads) {
            if (user != excludeUser) {
                user.sendMessage(message);
            }
        }
    }

    public static void addUserName(String userName) {
        userNames.add(userName);
    }

    public static void removeUser(String userName, UserThread user) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(user);
            System.out.println("Пользователь " + userName + " вышел из чата.");
        }
    }

    public static Set<String> getUserNames() {
        return userNames;
    }

    public static boolean hasUsers() {
        return !userNames.isEmpty();
    }
}

class UserThread extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private String userName;

    public UserThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            userName = reader.readLine();
            Server.addUserName(userName);

            String serverMessage = "Новый пользователь подключен: " + userName;
            Server.broadcast(serverMessage, this);
            System.out.println("Отправлено сообщение о подключении: " + serverMessage);

            String clientMessage;
            do {
                clientMessage = reader.readLine();
                System.out.println("Получено сообщение от " + userName + ": " + clientMessage);
                if (clientMessage != null) {
                    serverMessage = "[" + userName + "]: " + clientMessage;
                    Server.broadcast(serverMessage, this);
                    System.out.println("Отправлено сообщение: " + serverMessage);
                }
            } while (clientMessage != null && !clientMessage.equals("bye"));

            Server.removeUser(userName, this);
            socket.close();

            serverMessage = userName + " вышел из чата.";
            Server.broadcast(serverMessage, this);
            System.out.println("Отправлено сообщение о выходе: " + serverMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
        System.out.println("Отправлено сообщение " + userName + ": " + message);
    }

    public String getUserName() {
        return userName;
    }
}