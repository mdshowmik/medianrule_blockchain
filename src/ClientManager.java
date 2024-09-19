public class ClientManager {
    private int numClients;
    private CommandSender commandSender;
    private static int globalCommandCounter = 1;

    public ClientManager(int numClients, CommandSender commandSender) {
        this.numClients = numClients;
        this.commandSender = commandSender;
    }

    public void sendCommandsFromClients(int commandsPerClient) {
        for (int i = 1; i <= numClients; i++) {
            Client client = new Client(i, "localhost");
            for (int j = 0; j < commandsPerClient; j++) {
                int commandNumber = getNextCommandNumber();
                String command = "Command " + commandNumber;
                commandSender.sendCommand(client, command);
            }
        }
    }

    private synchronized int getNextCommandNumber() {
        return globalCommandCounter++;
    }
}
