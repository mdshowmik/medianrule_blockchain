import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Adversary {
    private ServerManager serverManager;
    private Set<Server> blockedServers;
    private Random random;

    public Adversary(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.blockedServers = new HashSet<>();
        this.random = new Random();
    }

    public void blockRandomServers() {
        unblockAllServers();

        List<Server> servers = serverManager.getServers();
        int totalServers = servers.size();
        int numberOfMaximumServersToBlock = random.nextInt(totalServers / 10) + 1;
        int numberOfServersToBlock = Math.max(1, numberOfMaximumServersToBlock);

        System.out.println(numberOfServersToBlock + " Servers will be blocked in this round");
        for (int i = 0; i < numberOfServersToBlock; i++) {
            Server serverToBlock = servers.get(random.nextInt(totalServers));
            if (!serverToBlock.isBlocked()){
                serverToBlock.setBlocked(true);
                blockedServers.add(serverToBlock);
                //serverToBlock.setNull();
                System.out.println(serverToBlock.getName() + " is blocked.");
            }
        }
    }

    public void unblockAllServers() {
        for (Server server : blockedServers) {
            server.setBlocked(false);
            System.out.println(server.getName() + " is unblocked.");
        }
        System.out.println(" ");
        blockedServers.clear();
    }
}
