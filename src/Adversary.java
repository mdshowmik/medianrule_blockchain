import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

public class Adversary {
    private ServerManager serverManager;
    private CopyOnWriteArraySet<Server> blockedServers;
    private Random random;

    public Adversary(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.blockedServers = new CopyOnWriteArraySet<>();
        this.random = new Random();
    }

    public void blockRandomServers(PrintStream out) {
        unblockAllServers(out);

        List<Server> servers = serverManager.getServers();
        int totalServers = servers.size();
        int numberOfMaximumServersToBlock = random.nextInt(totalServers / 10) + 1;
        int numberOfServersToBlock = Math.max(1, numberOfMaximumServersToBlock);

        out.println(numberOfServersToBlock + " Servers will be blocked in this round");
        for (int i = 0; i < numberOfServersToBlock; i++) {
            Server serverToBlock = servers.get(random.nextInt(totalServers));
            if (!serverToBlock.isBlocked()) {
                serverToBlock.setBlocked(true);
                blockedServers.add(serverToBlock);
                serverToBlock.setNull();
                out.println(serverToBlock.getCommandsStored());
                out.println(serverToBlock.getName() + " is blocked.");
            }
        }
    }

    public void unblockAllServers(PrintStream out) {
        for (Server server : blockedServers) {
            server.setBlocked(false);
            out.println(server.getName() + " is unblocked.");
        }
        blockedServers.clear();
        out.println(" ");
    }
}
