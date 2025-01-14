/*import java.io.IOException;

public class Controller {

    private ServerManager serverManager;
    private ClientManager clientManager;
    private ConsensusManager consensusManager;

    public void startApplication() throws InterruptedException, IOException {
        int numServers = 15;
        int numClients = 1;
        int numCommandsPerClient = 100;
        int port = 5000;

        serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        //serverManager.assignRandomDataToServers();

        clientManager = new ClientManager(numClients, serverManager, consensusManager);
        clientManager.startClients(numCommandsPerClient);

        Thread.sleep(1000);

        serverManager.printAllStoredCommands();

        consensusManager = new ConsensusManager(serverManager, this);
        consensusManager.makeRequestsAndComputeMedian();

        serverManager.printAllStoredCommands();

        serverManager.stopAllServers();
    }
}*/

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.nio.file.Files;

public class Controller {

    private ServerManager serverManager;
    private ClientManager clientManager;
    private ConsensusManager consensusManager;
    public volatile boolean clientsActive = true;
    int numClients = 5;
    int numCommandsPerClient = 100;
    private static final Path directoryPath;
    static {
        // Format the current time as a string for the directory name
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dirName = LocalDateTime.now().format(dtf);

        // Initialize the directory path
        directoryPath = Paths.get("Calculate_Tau/"+dirName);

        // Create the directory
        try {
            Files.createDirectories(directoryPath);
            System.out.println("Directory created: " + directoryPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + e.getMessage());
            throw new RuntimeException("Failed to initialize application directory", e);
        }
    }

    public static Path getDirectoryPath() {
        return directoryPath;
    }

    public int getTotalCommands(){
        return numClients*numCommandsPerClient;
    }

    public void startApplication(PrintStream out) throws InterruptedException, IOException {
        int numServers = 80;
        int port = 5000;

        serverManager = new ServerManager(numServers, port);
        serverManager.startAllServers();

        consensusManager = new ConsensusManager(serverManager, this);  // Adjust if ConsensusManager needs a reference to Controller
        clientManager = new ClientManager(numClients, serverManager, consensusManager);

        // Client thread: manages client tasks
        Thread clientThread = new Thread(() -> {
            try {
                clientManager.startClients(numCommandsPerClient);
            } catch (Exception e) {
                System.err.println("Error in ClientManager: " + e.getMessage());
            } finally {
                clientsActive = false;  // Signal that client tasks are done
            }
        });

        Thread consensusThread = new Thread(() -> {
            while (true) {
                if (!clientsActive && consensusManager.isConsensusReached(out)) {
                    break;
                }
                try {
                    // Log the current status of the consensus
//                    System.out.println("Attempting consensus; clients active: " + clientsActive +
//                            ", consensus reached: " + consensusManager.isConsensusReached());

                    // Make requests and compute median
                    consensusManager.makeRequestsAndComputeMedian(out);

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Consensus thread was interrupted: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    System.err.println("Error in ConsensusManager: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Consensus process completed. Clients active: " + clientsActive +
                    ", consensus reached: " + consensusManager.isConsensusReached(out));
        });


        // Monitoring thread, reports the state of client and consensus threads
        Thread monitorThread = new Thread(() -> {
            try {
                while (clientThread.isAlive() || consensusThread.isAlive()) {
                    System.out.println("Client Thread State: " + clientThread.getState());
                    System.out.println("Consensus Thread State: " + consensusThread.getState());
                    Thread.sleep(1000);  // Check every second
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Monitor Thread interrupted.");
            }
        }, "Monitor-Thread");

        // Start both threads
        clientThread.start();
        consensusThread.start();
        monitorThread.start();

        // Wait for both threads to complete
        clientThread.join();
        consensusThread.join();
        monitorThread.join();

        Thread.sleep(2000);  // Consider the necessity and implications of this sleep

        // Print commands after all clients and consensus operations have completed
//        serverManager.printAllStoredCommands(out);

        System.out.println("Consensus Reached in " + consensusManager.getRoundForConsensus() + " rounds");

        serverManager.stopAllServers();

        serverManager.valueofT(numServers);
    }
}
