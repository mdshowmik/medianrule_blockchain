import java.io.IOException;

public class ServerManager {
    private Server[] servers = new Server[40];  // Change from 5 to 15 servers

    public ServerManager() {
        // Initialize the 15 servers with different ports
        for (int i = 0; i < 40; i++) {
            servers[i] = new Server("Server" + (i + 1), 5000 + i);
        }
    }


    public Server[] getServers() {
        return servers; // Return the array of servers
    }

    // Start all servers
    public void startAllServers() throws IOException {
        for (Server server : servers) {
            server.start();
        }
    }

    // Stop all servers
    public void stopAllServers() throws IOException {
        for (Server server : servers) {
            server.stop();
        }
    }

    // Start a specific server by index
    public void startServer(int index) throws IOException {
        if (index >= 0 && index < servers.length) {
            servers[index].start();
        } else {
            System.out.println("Invalid server index.");
        }
    }

    // Stop a specific server by index
    public void stopServer(int index) throws IOException {
        if (index >= 0 && index < servers.length) {
            servers[index].stop();
        } else {
            System.out.println("Invalid server index.");
        }
    }

    // Method to print data of all servers
    public void printAllServersData() {
        for (Server server : servers) {
            server.printStatus(); // Calls the printStatus() method of each server
        }
    }
}
