import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private int clientId;
    private String serverAddress;
    private ServerManager serverManager;
    private ConsensusManager consensusManager;
    //boolean allCommandSend;

    public Client(int clientId, String serverAddress, ServerManager serverManager, ConsensusManager consensusManager) {
        this.clientId = clientId;
        this.serverAddress = serverAddress;
        this.serverManager = serverManager;
        this.consensusManager = consensusManager;
    }

    //send command to servers
    public void sendCommand(String command) {
        //String formattedCommand = "Client" + clientId + "_" + command;
        //String formattedCommand = "Client" + clientId + "_Round" + consensusManager.getRoundForConsensus()+ "_" + command ;
        String formattedCommand = "Client" + clientId + "_" + command + "_Round" + consensusManager.getRoundForConsensus();
        Server server = serverManager.getRandomServer();

        if (server == null) {
            System.out.println("No available server to send the command.");
            return;
        }

        while (server.isBusy()) {
            System.out.println(server.getName() + " is currently busy. Client" + clientId + " is waiting to send the command.");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try (Socket socket = new Socket(serverAddress, server.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Client " + clientId + " sends " + formattedCommand + " to " + server.getName());
            out.println(formattedCommand);

            String response = in.readLine();
            System.out.println("Client " + clientId + " receives response from " + server.getName() + ": " + response);

        } catch (IOException e) {
            System.out.println("Client " + clientId + " failed to connect to " + server.getName() + ": " + e.getMessage());
        }

        //allCommandSend = true;
    }
}
