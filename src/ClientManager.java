import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManager {
    private Client[] clients;
    private int numClients;
    private ServerManager serverManager;

    public ClientManager(int numClients, ServerManager serverManager) {
        this.numClients = numClients;
        this.serverManager = serverManager;
        clients = new Client[numClients];
        for (int i = 0; i < numClients; i++) {
            clients[i] = new Client(i + 1, "localhost", serverManager);
        }
    }

    //runs parallel clients
    public void startClients(int numCommandsPerClient) {
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        for (int i = 0; i < numClients; i++) {
            int clientId = i + 1;
            executor.submit(() -> {
                for (int j = 0; j < numCommandsPerClient; j++) {
                    clients[clientId - 1].sendCommand("Command" + (j + 1));
                }
            });
        }
        executor.shutdown();
    }
}
