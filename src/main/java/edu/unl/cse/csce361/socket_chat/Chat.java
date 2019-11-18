package edu.unl.cse.csce361.socket_chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class Chat {
    private static final int MAXIMUM_CONNECTION_ATTEMPTS = 10;
    private Cipher cipherStrategy;
    private Socket socket;
    private boolean isHost;
    private ResourceBundle bundle;
    private Set<String> keywords;


    public Chat () {
        this.cipherStrategy = CipherFactory.createCipher();
        setLocale(Locale.getDefault());
        socket = connect(new Scanner(System.in));
    }

    public static void main (String[] args) {
        Chat chat = new Chat();
        chat.communicate();
        chat.disconnect();
    }

    /*
     *  THESE METHODS SET UP AND TEAR DOWN CONNECTION
     */

    private String encipher (String plaintext) {
        String ciphertext = cipherStrategy.encipher(plaintext);
        return ciphertext;
    }

    private String decipher (String ciphertext) {
        String plaintext = cipherStrategy.decipher(ciphertext);
        return plaintext;
    }

    private void setLocale (Locale locale) {
        bundle = ResourceBundle.getBundle("socketchat", locale);
        Set<String> culledKeySet = bundle.keySet().stream()
                .filter(entry -> entry.startsWith("communicate.keyword."))
                .collect(Collectors.toSet());
        keywords = new HashSet<>(culledKeySet.size());
        culledKeySet.forEach(key -> keywords.add(bundle.getString(key)));
    }

    @SuppressWarnings("WeakerAccess")
    public Socket connect (Scanner userInput) {
        String yes = bundle.getString("connection.response.yes");
        String no = bundle.getString("connection.response.no");
        // "Are you the chat host? [Y] "
        System.out.print(bundle.getString("connection.prompt.isHost") + " [" + yes + "] ");
        String answerString = userInput.nextLine().toUpperCase();
        String answer = answerString.length() > 0 ? answerString.substring(0, no.length()) : yes;
        isHost = (!answer.equals(no));
        Socket socket = null;
        try {
            socket = isHost ? connectAsServer(userInput) : connectAsClient(userInput);
        } catch (IOException ioException) {
            // "Connection failed: ..."
            System.err.println(bundle.getString("connection.error.generalConnectionFailure") + ": " + ioException);
            System.exit(1);
        }
        return socket;
    }

    @SuppressWarnings("WeakerAccess")
    public void disconnect () {
        try {
            socket.close();
        } catch (IOException ioException) {
            // "Error while closing socket: ..."
            System.err.println(bundle.getString("connection.error.closingError") + ": " + ioException);
            // We're terminating anyway, note the error and continue normal termination
        }
    }

    private Socket connectAsServer (Scanner userInput) throws IOException {
        byte[] address = InetAddress.getLocalHost().getAddress();
        // "Host address: ..."
        System.out.println(MessageFormat.format(bundle.getString("connection.info.hostAddress.0.1.2.3"),
                address[0], address[1], address[2], address[3]));
        int port;
        ServerSocket serverSocket;
        do {
            // "Select port number"
            port = getPort(bundle.getString("connection.prompt.selectPortNumber"), userInput);
            try {
                serverSocket = new ServerSocket(port);
            } catch (BindException ignored) {
                // "Port ... is already in use."
                System.out.println(MessageFormat.format(bundle.getString("connection.error.portInUse.0"), port));
                serverSocket = null;
            }
        } while (serverSocket == null);
        System.out.println(bundle.getString("connection.info.waiting"));
        return serverSocket.accept();
    }

    /*
y
     *  THESE METHODS PERFORM CHAT AFTER CONNECTION IS SET UP
     */

    private Socket connectAsClient (Scanner userInput) throws IOException {
        byte[] address = getRemoteHostAddress(userInput);
        // "Enter port host is opening at ..."
        String prompt = MessageFormat.format(bundle.getString("connection.prompt.getHostPort.0.1.2.3"),
                address[0], address[1], address[2], address[3]);
        int port = getPort(prompt, userInput);
        Socket socket = null;
        int attemptCount = 0;
        do {
            try {
                sleep(1000 * attemptCount++);
            } catch (InterruptedException ignored) {
            }
            try {
                socket = new Socket(InetAddress.getByAddress(address), port);
            } catch (ConnectException ignored) {
                // "Attempt ...: Chat server is not yet ready at ..."
                System.out.println(MessageFormat.format(
                        bundle.getString("connection.error.hostNotReady.0.1.2.3.4.5"),
                        attemptCount, address[0], address[1], address[2], address[3], port));
                if (attemptCount < MAXIMUM_CONNECTION_ATTEMPTS) {
                    // "Will attempt to connect again in ... seconds."
                    System.out.println(MessageFormat.format(bundle.getString("connection.info.reAttempt"),
                            attemptCount));
                    socket = null;
                } else {
                    // "Exceeded maximum number of connection attempts. Terminating."
                    System.err.println(bundle.getString("connection.error.tooManyAttempts"));
                    System.exit(1);
                }
            }
        } while (socket == null);
        return socket;
    }

    private byte[] getRemoteHostAddress (Scanner userInput) {
        // This assumes IPv4. Probably a good assumption.
        boolean haveGoodAddress = false;
        byte[] address = new byte[4];
        while (!haveGoodAddress) {
            // "Enter IP address of host <##.##.##.##>:"
            System.out.print(bundle.getString("connection.prompt.getHostAddress") + " ");
            try {
                String addressString = userInput.nextLine();
                String[] tokens = addressString.split("\\.");
                if (tokens.length == 4) {
                    for (int i = 0; i < 4; i++) {
                        address[i] = Byte.parseByte(tokens[i]);
                    }
                    haveGoodAddress = true;
                } else {
                    // "The IP address should be four dot-separated numbers; ... does not conform."
                    System.out.println(MessageFormat.format(bundle.getString("connection.error.malformedAddress"),
                            addressString));
                    haveGoodAddress = false;
                }
            } catch (NumberFormatException nfException) {
                // "The IP address should be exactly as reported to the host user."
                System.out.println(bundle.getString("connection.error.badNumberInAddress"));
                String message = nfException.getMessage();
                // "Value out of range. Value"
                if (message.startsWith(bundle.getString("exception.numberFormat.startOfValueOutOfRangeMessage"))) {
                    String[] messageTokens = message.split("\"");
                    long value = Long.parseLong(messageTokens[1]);   // this may break if message format changes
                    if ((127 < value) && (value < 256)) {
                        // "Note that Java does not have unsigned integers, so subtract 256 from values greater than..."
                        System.out.println(MessageFormat.format(bundle.getString(
                                "connection.error.userAttemptedUnsignedByte.0.1"), value, value - 256));
                    }
                }
                haveGoodAddress = false;
            }
        }
        return address;
    }

    private int getPort (String prompt, Scanner userInput) {
        boolean haveGoodNumber = false;
        int port = 0;
        while (!haveGoodNumber) {
            System.out.print(prompt + ": ");
            try {
                port = userInput.nextInt();
                if (port <= 0) {
                    // "Expected positive value, got ..."
                    throw new InputMismatchException(MessageFormat.format(
                            bundle.getString("connection.error.portNumberTooLow"), port));
                }
                if (port >= 2 * (Short.MAX_VALUE + 1)) {
                    // "Expected value less than 65536, got ..."
                    throw new InputMismatchException(MessageFormat.format(
                            bundle.getString("connection.error.portNumberTooHigh"), port));
                }
                haveGoodNumber = true;
            } catch (InputMismatchException ignored) {
                // "The port number must be a positive integer strictly less than 65536."
                System.out.println(bundle.getString("connection.error.badPortNumber"));
                haveGoodNumber = false;
            } finally {
                userInput.nextLine();
            }
        }
        return port;
    }

    @SuppressWarnings("WeakerAccess")
    public void communicate () {
        try {
            communicate(
                    new BufferedReader(new InputStreamReader(System.in)),
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    System.out,
                    new PrintStream(socket.getOutputStream()));
        } catch (IOException ioException) {
            // "Failed to set up input/output streams: ..."
            // "Terminating."
            System.err.println(bundle.getString("communicate.error.cannotSetUpStreams") + ": " + ioException);
            System.err.println(bundle.getString("communicate.info.terminating"));
            System.exit(1);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void communicate (BufferedReader localInput,
                              BufferedReader remoteInput,
                              PrintStream localOutput,
                              PrintStream remoteOutput) {
        // "Connection established. Host goes first."
        System.out.println(bundle.getString("connection.info.ready"));
        String message = "";
        boolean keepTalking = true;
        boolean myTurnToTalk = isHost;
        try {
            while (keepTalking) {
                try {
                    if (myTurnToTalk) {
                        message = localInput.readLine();
                        remoteOutput.println(encipher(message));
                    } else {
                        String encipheredMessage = remoteInput.readLine();
                        if (encipheredMessage != null) {
                            message = decipher(encipheredMessage);
                            localOutput.println(message);
                        } else {
                            // "Received null message: lost connection to remote chatter. Terminating."
                            localOutput.println(bundle.getString("communicate.error.nullMessageFromRemote"));
                            keepTalking = false;
                        }
                    }
                } catch (SocketException ignored) {
                    // "Unable to exchange message: lost connection to remote chatter. Terminating."
                    localOutput.println(bundle.getString("communicate.error.cannotSendMessage"));
                    keepTalking = false;
                }
                if (keepTalking && keywords.contains(message)) {
                    keepTalking = handleKeyword(message, myTurnToTalk, localInput, localOutput);
                }
                myTurnToTalk = !myTurnToTalk;
            }
        } catch (IOException ioException) {
            System.err.println("Connection dropped: " + ioException);
            System.exit(1);
        }
    }

    private boolean handleKeyword (String keyword, boolean localMessage, BufferedReader input, PrintStream output) throws IOException {
        if (keyword.equals(bundle.getString("communicate.keyword.exit"))) {
            return false;

        } else if (keyword.equals(bundle.getString("communicate.keyword.setLocale"))) {
            if (localMessage) {
                output.println("Please enter a choice: (1) 中文, (2) English: ");
                String userChoice = input.readLine();
                while (!"中文".equalsIgnoreCase(userChoice) && !"English".equalsIgnoreCase(userChoice)) {
                    output.println(bundle.getString("communicate.error.unrecognizedKeyword") + ": " + keyword);
                    userChoice = input.readLine();
                }
                if ("中文".equals(userChoice)) {
                    setLocale(Locale.CHINESE);
                } else if ("English".equals(userChoice)) {
                    setLocale(Locale.getDefault());
                }
                output.println(bundle.getString("connection.info.changeSuccess") + ": " + userChoice + " mode");
            } else {
                output.println(bundle.getString("connection.info.changing")); // replace with i18n property
            }
        } else if (keyword.equals(bundle.getString("communicate.keyword.setCipher"))) {
            String userChoice = null;
            String[] keys = new String[1];
            if (localMessage) {
                output.println(bundle.getString("connection.info.cipher"));
                userChoice = input.readLine();
                while (!"Base64".equalsIgnoreCase(userChoice) && !"XOR".equalsIgnoreCase(userChoice)) {
                    output.println(bundle.getString("communicate.error.unrecognizedKeyword") + ": " + keyword);
                    userChoice = input.readLine();
                }
                if ("XOR".equalsIgnoreCase(userChoice)) {
                    output.println(bundle.getString("connection.prompt.inputString"));
                    keys[0] = input.readLine();
                }
                output.println(bundle.getString("connection.info.changeSuccess") + ": " + userChoice + " Cipher Algorithm");
                if (keys[0] == null) {
                    output.println(bundle.getString("connection.info.changeCipher") + userChoice + " Cipher Algorithm");
                } else {
                    output.println(bundle.getString("connection.info.changeCipher") + userChoice + " Cipher Algorithm " +
                            "key: " + keys[0]);
                }
                this.cipherStrategy = CipherFactory.createCipher(userChoice, keys);
            } else {
                output.println(bundle.getString("connection.info.changing"));
                if (keys[0] == null) {
                    output.println(bundle.getString("connection.info.changeCipher") + "bASE64 Cipher Algorithm");
                } else{
                    output.println(bundle.getString("connection.info.changeCipher") + "XOR Cipher Algorithm " +
                            "key: " + keys[0]);
                }
            }


        } else {
            output.println(bundle.getString("communicate.error.unrecognizedKeyword") + ": " + keyword);
        }
        return true;
    }
}
