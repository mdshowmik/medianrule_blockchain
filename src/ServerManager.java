import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ServerManager {
    //private Server[] servers;
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

//    public Server getRandomServer() {
//        Random random = new Random();
//        if (!servers.isEmpty()) {
//            Server currentServer = servers.get(random.nextInt(servers.size()));
//            if(curr)
////            return servers.get(random.nextInt(servers.size()));
//        }
//        else {
//            System.out.println("No servers available.");
//            return null;
//        }
//    }

    public Server getRandomServer() {
        Random random = new Random();
        if (servers.isEmpty()) {
            System.out.println("No servers available.");
            return null;
        } else {
            int totalServers = servers.size();
            for (int i = 0; i < totalServers; i++) {
                Server currentServer = servers.get(random.nextInt(totalServers));
                if (!currentServer.isBusy() && !currentServer.isBlocked()) {
                    return currentServer;
                }
            }
            System.out.println("All servers are currently busy or blocked.");
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

    /*public void printAllStoredCommands() {   //uncomment
        System.out.println(" ");
        for (Server server : servers) {
            System.out.print(server.getName() + ": ");
            System.out.println(String.join(", ", server.getCommandsStored()));
        }
        System.out.println(" ");
    }*/

    public void printAllStoredCommands(PrintStream out) {

        out.println("Current Command"+ClientManager.totalCommandsSent);
        out.println("Consensus Status"+ConsensusManager.concensusComplete);
        out.println(" ");
        for (Server server : servers) {
            out.print(server.getName() + ": ");
            out.println(String.join(", ", server.getCommandsStored()));
        }
        out.println(" ");


    }

    public void storedInCSVFile(String filename){
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (Server server : servers) {
                // Collect all commands for the server, joined by commas
                String commands = String.join(",", server.getCommandsStored());
                out.println(commands);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // check command validity
    public boolean checkValidity(String commandToCheck) {

        List<String> allCommands = new ArrayList<>();
        for (Server server : servers) {
            allCommands.addAll(server.getCommandsStored());
        }

        String[] commandReceivedFromOtherServer = commandToCheck.split(",");

        for (String breakingCommandList : commandReceivedFromOtherServer) {
            breakingCommandList = breakingCommandList.replaceAll("\\s", "");

            //System.out.println("checking " + breakingCommandList + " against " + allCommands);

            if (allCommands.contains(breakingCommandList)) {
                //System.out.println("yes");
                return true;
            }
        }
        return false;
    }

    public double valueofT(int n){
        // Compute the base-10 logarithm of n
        double logBase10 = Math.log(n) / Math.log(10);
        int tau = 2;
        double T =  tau * logBase10;
        System.out.println("tau = " + T);

        return T;
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
