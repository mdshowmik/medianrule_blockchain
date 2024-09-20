import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        int numServers = 50;
        int numClients = 2;
        int numCommandsPerClient = 2;
        int port = 5000;

        ServerManager serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        ClientManager clientManager = new ClientManager(numClients, serverManager);
        clientManager.startClients(numCommandsPerClient);

        Thread.sleep(1000);

        serverManager.printAllStoredCommands();
        serverManager.printAllServersData();
        serverManager.stopAllServers();
    }
}
