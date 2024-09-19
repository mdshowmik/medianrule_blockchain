public class CommandSender {
    private ServerManager serverManager;

    public CommandSender(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public void sendCommand(Client client, String command) {
        int randomServerIndex = (int) (Math.random() * serverManager.getServers().length);
        Server server = serverManager.getServers()[randomServerIndex];
        client.sendCommand(command, randomServerIndex + 1, server.getPort());
    }
}
