package edu.unl.cse.csce361.socket_chat;

import java.io.*;
import java.net.*;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Chat {

    private static final int MAXIMUM_CONNECTION_ATTEMPTS = 10;
    private Socket socket;
    private boolean isHost;

    public Chat() {
        socket = connect(new Scanner(System.in));
    }

    // THESE METHODS SET UP AND TEAR DOWN CONNECTION

    @SuppressWarnings("WeakerAccess")
    public Socket connect(Scanner userInput) {
        System.out.print("Are you the chat host? [Y] ");
        String answerString = userInput.nextLine().toUpperCase();
        char answer = answerString.length() > 0 ? answerString.charAt(0) : 'Y';
        isHost = (answer != 'N');
        Socket socket = null;
            try {
                socket = isHost ? connectAsServer(userInput) : connectAsClient(userInput);
            } catch (IOException ioException) {
                System.err.println("Connection failed: " + ioException);
                System.exit(1);
            }
        return socket;
    }

    @SuppressWarnings("WeakerAccess")
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ioException) {
            System.err.println("Error while closing socket: " + ioException);
            // We're terminating anyway, note the error and continue normal termination
        }
    }

    private Socket connectAsServer(Scanner userInput) throws IOException {
        byte[] address = InetAddress.getLocalHost().getAddress();
        System.out.println("Host address: " + address[0] + "." + address[1] + "." + address[2] + "." + address[3]);
        int port;
        ServerSocket serverSocket;
        do {
            String prompt = "Select port number";
            port = getPort(prompt, userInput);
            try {
                serverSocket = new ServerSocket(port);
            } catch (BindException ignored) {
                System.out.println("Port " + port + " is already in use.");
                serverSocket = null;
            }
        } while (serverSocket == null);
        System.out.println("Waiting for client.");
        return serverSocket.accept();
    }

    private Socket connectAsClient(Scanner userInput) throws IOException {
        byte[] address = getRemoteHostAddress(userInput);
        String prompt = "Enter port host is opening at " +
                address[0] + "." + address[1] + "." + address[2] + "." + address[3];
        int port = getPort(prompt, userInput);
        Socket socket = null;
        int attemptCount = 0;
        do {
            try {
                sleep(1000*attemptCount++);
            } catch (InterruptedException ignored) {
            }
            try {
                socket = new Socket(InetAddress.getByAddress(address), port);
            } catch (ConnectException ignored) {
                System.out.println("Attempt " + attemptCount + ": Chat server is not yet ready at " +
                        address[0] + "." + address[1] + "." + address[2] + "." + address[3] + ":" + port);
                if (attemptCount < MAXIMUM_CONNECTION_ATTEMPTS) {
                    System.out.println("Will attempt to connect again in " + attemptCount + " seconds");
                    socket = null;
                } else {
                    System.err.println("Exceeded maximum number of connection attempts. Terminating.");
                    System.exit(1);
                }
            }
        } while (socket == null);
        return socket;
    }

    private byte[] getRemoteHostAddress(Scanner userInput) {
        // This assumes IPv4. Probably a good assumption.
        boolean haveGoodAddress = false;
        byte[] address = new byte[4];
        while (!haveGoodAddress) {
            System.out.print("Enter IP address of host <##.##.##.##>: ");
            haveGoodAddress = true;
            try {
                String addressString = userInput.nextLine();
                String[] tokens = addressString.split("\\.");
                if (tokens.length == 4) {
                    for (int i = 0; i < 4; i++) {
                        address[i] = Byte.parseByte(tokens[i]);
                    }
                } else {
                    System.out.println("The IP address should be four dot-separated numbers; <"
                            + addressString + "> does not conform.");
                    haveGoodAddress = false;
                }
            } catch (NumberFormatException nfException) {
                System.out.println("The IP address should be exactly as reported to the host user.");
                String message = nfException.getMessage();
                if (message.startsWith("Value out of range. Value")) {
                    String[] messageTokens = message.split("\"");
                    long value = Long.parseLong(messageTokens[1]);   // this may break if message format changes
                    if ((127 < value) && (value < 256)) {
                        System.out.println("Note that Java does not have unsigned integers, so subtract 256 from " +
                                "values greater than 127. For example, " + value + " should be " + (value - 256) + ".");
                    }
                }
                haveGoodAddress = false;
            }
        }
        return address;
    }

    private int getPort(String prompt, Scanner userInput) {
        boolean haveGoodNumber = false;
        int port = 0;
        while (!haveGoodNumber) {
            System.out.print(prompt + ": ");
            haveGoodNumber = true;
            try {
                port = userInput.nextInt();
                if (port <= 0) throw new InputMismatchException("Expected positive value, got " + port);
                if (port >= 2 * (Short.MAX_VALUE + 1)) {
                    throw new InputMismatchException("Expected value less than 65536, got " + port);
                }
            } catch (InputMismatchException ignored) {
                System.out.println("The port number must be a positive integer strictly less than 65536.");
                haveGoodNumber = false;
            } finally {
                userInput.nextLine();
            }
        }
        return port;
    }

    // THESE METHODS PERFORM CHAT AFTER CONNECTION IS SET UP

    @SuppressWarnings("WeakerAccess")
    public void communicate() {
        try {
            communicate(
                    new BufferedReader(new InputStreamReader(System.in)),
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    System.out,
                    new PrintStream(socket.getOutputStream()));
        } catch (IOException ioException) {
            System.err.println("Failed to set up input/output streams: " + ioException);
            System.err.println("Terminating.");     // I'm pretty sure this is recoverable if the client is waiting on the server
            System.exit(1);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void communicate(BufferedReader localInput,
                             BufferedReader remoteInput,
                             PrintStream localOutput,
                             PrintStream remoteOutput) {
        System.out.println("Connection established. Host goes first.");
        String message = "";
        boolean myTurnToTalk = isHost;
        try {
            while (!message.equals("EXIT")) {
                if (myTurnToTalk) {
                    message = localInput.readLine();
                    remoteOutput.println(encipher(message));
                } else {
                    message = decipher(remoteInput.readLine());
                    localOutput.println(message);
                }
                myTurnToTalk = !myTurnToTalk;
            }
        } catch (IOException ioException) {
            System.err.println("Connection dropped: " + ioException);
            System.exit(1);
        }
    }

    private String encipher(String plaintext) {
//        String ciphertext = ...;
//        return ciphertext;
        return plaintext;
    }

    private String decipher(String ciphertext) {
//        String plaintext = ...;
//        return plaintext;
        return ciphertext;
    }

    public static void main(String[] args) {
        Chat chat = new Chat();
        chat.communicate();
        chat.disconnect();
    }
}
