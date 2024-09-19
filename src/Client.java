import java.io.*;
import java.net.*;

public class Client {
    private int clientId;
    private String serverAddress;

    public Client(int clientId, String serverAddress) {
        this.clientId = clientId;
        this.serverAddress = serverAddress;
    }

    public void sendCommand(String command, int serverNumber, int serverPort) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Client" + clientId + " sends " + command + " to Server" + serverNumber);
            out.println(command);

            String response = in.readLine();
            System.out.println("Server" + serverNumber + " received: " + command);
            System.out.println("Client" + clientId + " receives response from Server" + serverNumber);

        } catch (IOException e) {
            System.out.println("Failed to connect to Server" + serverNumber + ": " + e.getMessage());
        }
    }
}
