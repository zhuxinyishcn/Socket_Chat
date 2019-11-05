package edu.unl.cse.csce361.socket_chat;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Chat {

    private Socket socket;
    private Scanner scanner;
    private boolean isHost;
    private PrintStream[] output = new PrintStream[2];
    private BufferedReader[] input = new BufferedReader[2];

    public Chat() {
        scanner = new Scanner(System.in);
        try {
            socket = connect();
            if (isHost) {
                input[0] = new BufferedReader(new InputStreamReader(System.in));
                input[1] = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output[0] = new PrintStream(socket.getOutputStream());
                output[1] = System.out;
            } else {
                input[0] = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                input[1] = new BufferedReader(new InputStreamReader(System.in));
                output[0] = System.out;
                output[1] = new PrintStream(socket.getOutputStream());
            }
        } catch (IOException ioException) {
            System.err.println("Connection failed: " + ioException);
            System.exit(1);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void communicate() {
        System.out.println("Connection established. Host goes first.");
        String message = "";
        int turn = 0;
        try {
            while (!message.equals("EXIT")) {
                message = input[turn].readLine();
                output[turn].println(message);
                turn = (turn + 1) % 2;
            }
            socket.close();
        } catch (IOException ioException) {
            System.err.println("Connection dropped: " + ioException);
            System.exit(1);
        }
    }

    private Socket connect() throws IOException {
        System.out.print("Are you the chat host? [Y] ");
        String answerString = scanner.nextLine().toUpperCase();
        char answer = answerString.length() > 0 ? answerString.charAt(0) : 'Y';
        isHost = (answer != 'N');
        return isHost ? connectAsServer() : connectAsClient();
    }

    private Socket connectAsServer() throws IOException {
        byte[] address = InetAddress.getLocalHost().getAddress();
        System.out.println("Host address: " + address[0] + "." + address[1] + "." + address[2] + "." + address[3]);
        System.out.print("Select port number: ");
        int port = getPort();
        ServerSocket serverSocket = new ServerSocket(port);
        return serverSocket.accept();
    }

    private Socket connectAsClient() throws IOException {
        System.out.print("Enter IP address of host <##.##.##.##>: ");
        String addressString = scanner.nextLine();
        String[] tokens = addressString.split("\\.");
        byte[] address = new byte[4];
        for (int i = 0; i < 4; i++) {
            address[i] = Byte.parseByte(tokens[i]);
        }
        System.out.print("Enter port host is opening at " +
                address[0] + "." + address[1] + "." + address[2] + "." + address[3] + ": ");
        int port = getPort();
        return new Socket(InetAddress.getByAddress(address), port);
    }

    private int getPort() {
        int port = scanner.nextInt();
        scanner.nextLine();
        return port;
    }

    public static void main(String[] args) {
        Chat chat = new Chat();
        chat.communicate();
    }
}
