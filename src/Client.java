import java.io.*;
import java.net.*;

public class Client {
    private String serverAddress;
    private int port;

    // Constructor to initialize the client with server address and port
    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    // Method to send a command to the server
    public void sendCommand(String command) {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);
            String response = in.readLine();
            System.out.println("Response from server: " + response);

        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }
}
