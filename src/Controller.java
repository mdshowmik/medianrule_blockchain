import java.io.IOException;

public class Controller {

    private ServerManager serverManager;
    private ClientManager clientManager;
    private ConsensusManager consensusManager;

    public void startApplication() throws InterruptedException, IOException {
        int numServers = 100;
        int numClients = 5;
        int numCommandsPerClient = 20;
        int port = 5000;

        serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        serverManager.assignRandomDataToServers();

        //clientManager = new ClientManager(numClients, serverManager);
        //clientManager.startClients(numCommandsPerClient);

        Thread.sleep(1000);

        serverManager.printAllStoredCommands();

        consensusManager = new ConsensusManager(serverManager);
        consensusManager.makeRequestsAndComputeMedian();

        serverManager.printAllStoredCommands();

        serverManager.stopAllServers();
    }
}
