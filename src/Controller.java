/*import java.io.IOException;

public class Controller {

    private ServerManager serverManager;
    private ClientManager clientManager;
    private ConsensusManager consensusManager;

    public void startApplication() throws InterruptedException, IOException {
        int numServers = 10;
        int numClients = 1;
        int numCommandsPerClient = 100;
        int port = 5000;

        serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        //serverManager.assignRandomDataToServers();

        clientManager = new ClientManager(numClients, serverManager);
        clientManager.startClients(numCommandsPerClient);

        Thread.sleep(1000);

        serverManager.printAllStoredCommands();

        consensusManager = new ConsensusManager(serverManager);
        consensusManager.makeRequestsAndComputeMedian();

        serverManager.printAllStoredCommands();

        serverManager.stopAllServers();
    }
}*/

import java.io.IOException;

public class Controller {

    private ServerManager serverManager;
    private ClientManager clientManager;
    private ConsensusManager consensusManager;

    public void startApplication() throws InterruptedException, IOException {
        int numServers = 50;
        int numClients = 1;
        int numCommandsPerClient = 100;
        int port = 5000;

        serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        //serverManager.assignRandomDataToServers();

        clientManager = new ClientManager(numClients, serverManager);
        consensusManager = new ConsensusManager(serverManager);

        // Create threads for ClientManager and ConsensusManager
        Thread clientThread = new Thread(() -> {
            try {
                clientManager.startClients(numCommandsPerClient);
            } catch (Exception e) {
                System.err.println("Error in ClientManager: " + e.getMessage());
            }
        });

        Thread consensusThread = new Thread(() -> {
            try {
                consensusManager.makeRequestsAndComputeMedian();
            } catch (Exception e) {
                System.err.println("Error in ConsensusManager: " + e.getMessage());
            }
        });

        // Start both threads
        clientThread.start();
        consensusThread.start();

        // Wait for both threads to complete
        clientThread.join();
        consensusThread.join();

        Thread.sleep(2000);

        // Print commands after all clients and consensus operations have completed
        serverManager.printAllStoredCommands();

        serverManager.stopAllServers();
    }
}
