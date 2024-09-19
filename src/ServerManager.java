import java.io.IOException;
import java.util.List;

public class ServerManager {
    private Server[] servers;

    public ServerManager(int startingPort, int numServers) {
        this.servers = new Server[numServers];

        // Initialize servers with dynamic ports
        for (int i = 0; i < numServers; i++) {
            servers[i] = new Server("Server" + (i + 1), startingPort + i);
        }
    }

    public Server[] getServers() {
        return servers;
    }

    public void startAllServers() throws IOException {
        for (Server server : servers) {
            server.start();
        }
        System.out.println(" ");
    }

    public void stopAllServers() throws IOException {
        //System.out.println(" ");
        for (Server server : servers) {
            server.stop();
        }
    }

    public void printAllServersData() {
        System.out.println(" ");
        for (Server server : servers) {
            server.printStatus();
        }
    }

    public void printAllStoredCommands() {
        System.out.println(" ");
        for (Server server : servers) {
            System.out.print(server.getName() + ": ");
            List<String> commands = server.getCommandsStored();
            System.out.println(String.join(", ", commands));
        }
        System.out.println(" ");
    }

}
