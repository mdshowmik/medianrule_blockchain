import java.io.IOException;
import java.net.ServerSocket;
//median rule basic
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        int numServers = 5;
        int numClients = 3;
        int numCommandsPerClient = 3;
        int port = 5000;

        ServerManager serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        //serverManager.printAllStoredCommands();

        CheckConsensus checkConsensus = new CheckConsensus (serverManager);
        checkConsensus.checkAndPrintConsensusStatus();


        ClientManager clientManager = new ClientManager(numClients, serverManager);
        clientManager.startClients(numCommandsPerClient);

        Thread.sleep(1000); // Wait for some time to let all clients finish

        serverManager.printAllStoredCommands();
        //serverManager.printAllServersData();

        checkConsensus.checkAndPrintConsensusStatus();


        serverManager.stopAllServers();
    }
}
