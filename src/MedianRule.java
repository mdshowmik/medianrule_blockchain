import java.io.IOException;  // Import IOException
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianRule {
    private ServerManager serverManager;
    private Adversary adversary;
    private int roundCount;

    // Constructor for MedianRule
    public MedianRule(ServerManager serverManager, Adversary adversary) {
        this.serverManager = serverManager;
        this.adversary = adversary;
        this.roundCount = 0;
    }

    // Function to run the consensus algorithm using the median rule
    public void runConsensus() {
        boolean allServersInSync = false;

        while (!allServersInSync) {
            roundCount++;
            System.out.println("\nRound " + roundCount + ": Synchronizing servers...");

            // Get blocked servers for this round
            List<Server> blockedServers = adversary.getBlockedServers(serverManager.getServers());

            // Loop through all servers
            for (Server server : serverManager.getServers()) {
                // Skip blocked servers
                if (blockedServers.contains(server)) {
                    continue;
                }

                // Get 6 random servers to request data from
                List<Server> selectedServers = getRandomServers(serverManager.getServers(), server, 6, blockedServers);
                List<List<String>> responses = new ArrayList<>();

                // Collect responses from selected servers
                for (Server selectedServer : selectedServers) {
                    responses.add(new ArrayList<>(selectedServer.getCommandsReceived()));
                }

                // Print commands before applying the median rule
                System.out.println(server.getName() + " commands before median rule: " + server.getCommandsReceived());

                // Randomly pick 3 responses and apply the median rule
                Collections.shuffle(responses);
                List<List<String>> selectedResponses = responses.subList(0, 3);

                // Print responses from other servers before applying median rule
                System.out.println("Responses for " + server.getName() + ": " + selectedResponses);

                // Apply the median rule to the selected responses and append unique commands
                appendUniqueCommands(server, selectedResponses);

                // Print commands after applying the median rule
                System.out.println(server.getName() + " commands after median rule: " + server.getCommandsReceived());
            }

            // Check if all servers have the same state
            allServersInSync = checkIfAllServersInSync();
            adversary.prepareNextRound(); // Prepare for the next round by selecting new blocked servers
        }

        System.out.println("\nConsensus reached in " + roundCount + " rounds.");
        printFinalServerStates();  // Print the final state of each server after consensus
    }

    // Function to get random servers for the current server
    private List<Server> getRandomServers(Server[] servers, Server currentServer, int count, List<Server> blockedServers) {
        List<Server> availableServers = new ArrayList<>();
        for (Server server : servers) {
            if (!server.equals(currentServer) && !blockedServers.contains(server)) {
                availableServers.add(server);
            }
        }
        Collections.shuffle(availableServers);
        return availableServers.subList(0, Math.min(count, availableServers.size()));
    }

    // Append unique commands from the selected responses to the server's command list
    private void appendUniqueCommands(Server server, List<List<String>> selectedResponses) {
        List<String> currentCommands = server.getCommandsReceived();
        for (List<String> response : selectedResponses) {
            for (String command : response) {
                if (!currentCommands.contains(command)) {
                    currentCommands.add(command); // Append only if the command is unique
                }
            }
        }
        server.setCommandsReceived(currentCommands); // Update the server with new commands
    }

    // Check if all servers have the same state
    private boolean checkIfAllServersInSync() {
        List<String> referenceCommands = serverManager.getServers()[0].getCommandsReceived();
        for (Server server : serverManager.getServers()) {
            if (!server.getCommandsReceived().equals(referenceCommands)) {
                return false;
            }
        }
        return true;
    }

    // Print the final state of each server after reaching consensus
    private void printFinalServerStates() {
        System.out.println("\nFinal state of each server after consensus:");
        for (Server server : serverManager.getServers()) {
            System.out.println(server.getName() + " final commands: " + server.getCommandsReceived());
        }
    }
}