import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        int startingPort = 6000;
        int numServers = 5;
        int numClients = 2;
        int commandsPerClient = 5;

        ServerManager serverManager = new ServerManager(startingPort, numServers);
        serverManager.startAllServers();
        serverManager.printAllStoredCommands();

        CommandSender commandSender = new CommandSender(serverManager);
        ClientManager clientManager = new ClientManager(numClients, commandSender);
        clientManager.sendCommandsFromClients(commandsPerClient);

        //serverManager.printAllServersData();
        serverManager.printAllStoredCommands();
        serverManager.stopAllServers();
    }
}
