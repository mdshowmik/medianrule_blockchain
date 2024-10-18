import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ServerManager {

    private List<Server> servers;

    public ServerManager(int numberOfServers, int startingPort) {
        servers = new ArrayList<>();
        for (int i = 0; i < numberOfServers; i++) {
            int port = startingPort + new Random().nextInt(1000);
            try {
                Server server = new Server("Server" + (i + 1), port);
                servers.add(server);
                new Thread(server).start();
            } catch (IOException e) {
                System.err.println("Failed to start Server" + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public Server getRandomServer() {
        Random random = new Random();
        if (!servers.isEmpty()) {
            return servers.get(random.nextInt(servers.size()));
        } else {
            System.out.println("No servers available.");
            return null;
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public void startAllServers() throws IOException {
        for (Server server : servers) {
            server.start();
        }
        System.out.println(" ");
    }

    public void stopAllServers() throws IOException {
        for (Server server : servers) {
            server.stop();
        }
    }

    public void printAllStoredCommands() {
        System.out.println(" ");
        for (Server server : servers) {
            System.out.print(server.getName() + ": ");
            System.out.println(String.join(", ", server.getCommandsStored()));
        }
        System.out.println(" ");
    }

    /*public void printAllServersData() {
        System.out.println(" ");
        for (Server server : servers) {
            server.printStatus();
        }
    }*/

   /*public boolean areAllServersInConsensus() {
        List<String> firstServerCommands = servers.get(0).getCommandsStored();
        for (Server server : servers) {
            if (!server.getCommandsStored().equals(firstServerCommands)) {
                return false;
            }
        }
        return true;
    }*/
   /*public void assignRandomDataToOneServer() {

       for (Server server : servers) {
           Server randomServer = servers.get(new Random().nextInt(servers.size()));
           int randomNumber = new Random().nextInt(10); // Generate a random number between 0 and 99
           randomServer.getCommandsStored().add(String.valueOf(randomNumber)); // Assign it to the randomly selected server
           System.out.println("Random data assigned to " + randomServer.getName() + ": " + randomNumber);
       }
   }*/

    public void assignRandomDataToServers() {
        Random random = new Random();
        int numberOfServers = servers.size();
        int serversToInitialize = (int) Math.ceil(numberOfServers * 0.80); // Calculate 80% of the servers, rounding up if necessary.

        // Create a list of server indices and shuffle it.
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < numberOfServers; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);

        // Select the first 80% of the shuffled index list to assign data.
        for (int i = 0; i < serversToInitialize; i++) {
            Server server = servers.get(indices.get(i));
            int randomNumber = random.nextInt(10); // Generate a random number between 0 and 9
            String randomNumberAsString = String.valueOf(randomNumber);
            server.addCommand(randomNumberAsString); // Assuming addCommand method handles the data correctly
        }
    }

}

