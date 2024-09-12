import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class Adversary {
    private List<Server> previouslyBlockedServers = new ArrayList<>();

    // Get the list of servers to block for the current round
    public List<Server> getBlockedServers(Server[] servers) {
        int numberOfServersToBlock = Math.max(1, servers.length / 10); // Block at least 1/10th or at least 1 server
        List<Server> availableServers = new ArrayList<>(Arrays.asList(servers));

        // Remove previously blocked servers to avoid blocking them again
        availableServers.removeAll(previouslyBlockedServers);

        // If not enough servers remain, unblock previously blocked ones and reshuffle
        if (availableServers.size() < numberOfServersToBlock) {
            availableServers = new ArrayList<>(Arrays.asList(servers));
        }

        // Shuffle the available servers and select new servers to block
        Collections.shuffle(availableServers);
        List<Server> blockedServers = availableServers.subList(0, numberOfServersToBlock);

        // Update the previously blocked servers
        previouslyBlockedServers = new ArrayList<>(blockedServers);

        // Print blocked servers for this round
        System.out.println("Blocked servers this round: " +
                blockedServers.stream()
                        .map(Server::getName)
                        .collect(Collectors.toList()) // Replace toList() with collect(Collectors.toList())
        );

        return blockedServers;
    }

    // Prepare for the next round by resetting blocked servers (if needed)
    public void prepareNextRound() {
        // Logic for any preparation needed before the next round, if necessary
    }
}