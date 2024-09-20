import java.io.IOException;
import java.util.Random;

public class ServerManager {
    private Server[] servers;
    private Random random;

    public ServerManager(int startingPort, int numServers) {
        this.servers = new Server[numServers];
        this.random = new Random();

        for (int i = 0; i < numServers; i++) {
            servers[i] = new Server("Server " + (i + 1), startingPort + i);
        }
    }

    public Server getRandomServer() {
        return servers[random.nextInt(servers.length)];
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

