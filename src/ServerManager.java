import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerManager {
    private List<Server> servers;

    public ServerManager(int numberOfServers, int startingPort) {
        servers = new ArrayList<>();
        for (int i = 0; i < numberOfServers; i++) {
            int port = startingPort + new Random().nextInt(1000);  // Random port offset within a range
            try {
                Server server = new Server("Server" + (i + 1), port);
                servers.add(server);
                new Thread(server).start();  // Start the server thread
            } catch (IOException e) {
                System.err.println("Failed to start Server" + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public Server getRandomServer() {
        Random random = new Random();
        if (!servers.isEmpty()) {
            return servers.get(random.nextInt(servers.size()));
        } else {
            System.out.println("No servers available.");
            return null;
        }
    }

    public void startAllServers() throws IOException {
        for (Server server : servers) {
            server.start();
        }
        System.out.println(" ");
    }

    public void stopAllServers() throws IOException {
        for (Server server : servers) {
            server.stop();
        }
    }

    public void printAllStoredCommands() {
        System.out.println(" ");
        for (Server server : servers) {
            System.out.print(server.getName() + ": ");
            System.out.println(String.join(", ", server.getCommandsStored()));
        }
    }

    public void printAllServersData() {
        System.out.println(" ");
        for (Server server : servers) {
            server.printStatus();
        }
    }
}

