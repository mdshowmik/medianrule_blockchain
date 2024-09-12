import java.io.IOException;
import java.util.Random;

public class CommandSender {
    private ServerManager serverManager;
    private Random random;

    // Constructor to initialize with the ServerManager
    public CommandSender(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.random = new Random();
    }

    // Method to send n random commands to servers
    public void sendRandomCommands(int numberOfCommands) throws IOException, InterruptedException {
        for (int i = 1; i <= numberOfCommands; i++) {
            // Generate a random server index (0 to 4, corresponding to Server1 to Server5)
            int serverIndex = random.nextInt(5);

            // Construct the command as "Command number X"
            String command = "Command number " + i;

            // Create a client for the randomly chosen server
            int serverPort = 5000 + serverIndex; // Server ports are 5000 to 5004
            Client client = new Client("localhost", serverPort);

            // Send the command to the server
            System.out.println("Sending " + command + " to Server" + (serverIndex + 1));
            client.sendCommand(command);

            // Optional: Sleep for 1 second between commands to simulate real-time delay
            Thread.sleep(1000);
        }
    }
}
