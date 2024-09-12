import java.io.IOException;

public class main {
    public static void main(String[] args) throws InterruptedException, IOException {
        // Create the ServerManager to manage 15 servers
        ServerManager manager = new ServerManager();

        // Start all servers
        manager.startAllServers();

        // Send random commands to the servers
        CommandSender commandSender = new CommandSender(manager);
        commandSender.sendRandomCommands(1);  // Send 10 random commands

        // Create an adversary to block servers
        Adversary adversary = new Adversary();

        // Create a MedianRule instance to synchronize the servers
        MedianRule medianRule = new MedianRule(manager, adversary);

        // Run the consensus algorithm
        medianRule.runConsensus();

        // Stop all servers after reaching consensus
        manager.stopAllServers();
    }
}

