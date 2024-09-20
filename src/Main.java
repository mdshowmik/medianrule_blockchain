import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        int startingPort = 5000;
        int numServers = 10;
        int numClients = 5;
        int numCommandsPerClient = 5;

        ServerManager serverManager = new ServerManager(startingPort, numServers);
        serverManager.startAllServers();

        ClientManager clientManager = new ClientManager(numClients, serverManager);
        clientManager.startClients(numCommandsPerClient);

        Thread.sleep(1000); // Wait for some time to let all clients finish

        serverManager.printAllStoredCommands();
        serverManager.printAllServersData();
        serverManager.stopAllServers();
    }
}
