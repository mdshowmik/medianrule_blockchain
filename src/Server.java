import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private int port;
    private boolean running;
    private String name;
    private ServerSocket serverSocket;
    private int commandCount;
    private List<String> commandsReceived;
    private List<String> commandsStored;
    private boolean busy;
    private boolean consensus;
    private boolean isready;


    public Server(String name, int port) throws IOException {
        this.name = name;
        this.port = port;
        this.running = false;
        this.commandCount = 0;
        this.commandsReceived = new ArrayList<>();
        this.commandsStored = new ArrayList<>();
        this.busy = false;
        bindToPort(port);
        this.consensus = false;
        this.isready = true; // Initially ready
    }

    private void bindToPort(int port) throws IOException {
        int maxRetries = 10;  // Maximum number of retries to find an available port
        for (int i = 0; i < maxRetries; i++) {
            try {
                this.serverSocket = new ServerSocket(port);
                //System.out.println(name + " started on port " + port);
                this.port = port;  // Save the port in case it was changed
                break;
            } catch (IOException e) {
                System.out.println("Port " + port + " is unavailable. Trying next port...");
                port++;  // Try the next port
            }
        }
        if (this.serverSocket == null) {
            throw new IOException("Failed to bind to a port after " + maxRetries + " attempts.");
        }
    }

    public boolean isInConsensus() {
        return consensus;
    }

    public void setConsensus(boolean consensus) {
        this.consensus = consensus;
    }

    public String getName() {
        return name;
    }

    public boolean isReady() {
        return isready;
    }

    // Method to set server readiness (e.g., if it is processing a request, it might not be ready)
    public void setReady(boolean isready) {
        this.isready = isready;
    }

    public List<String> getCommandsReceived() {
        return new ArrayList<>(commandsReceived);
    }

    public List<String> getCommandsStored() {
        return new ArrayList<>(commandsStored);
    }

    public void setCommandsReceived(List<String> commands) {
        this.commandsReceived = new ArrayList<>(commands);
    }

    public void start() throws IOException {
        //bindToPort(port);
        running = true;
        System.out.println(name + " started on port " + port);
        new Thread(this).start();
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        System.out.println(name + " stopped.");
    }

    public void printStatus() {
        System.out.println("Server: " + name);
        System.out.println("Port: " + port);
        System.out.println("Running: " + running);
        System.out.println("Commands received: " + commandCount);

        if (commandsReceived.isEmpty()) {
            System.out.println("No commands have been received yet.");
        } else {
            System.out.println("List of commands received:");
            for (String command : commandsReceived) {
                System.out.println(" - " + command);
            }
        }
        System.out.println("-----------------------------");
    }

    @Override
    public void run() {
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                busy = true;  // Mark server as busy
                String command = in.readLine();
                if (command != null) {
                    commandsStored.add(command);
                    commandCount++;
                    commandsReceived.add(command);
                    out.println(command);
                }
                busy = false;
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error in " + name + ": " + e.getMessage());
                }
                busy = false;
            }
        }
    }

    public int getPort() {
        return port;
    }

    public boolean isBusy() {
        return busy;
    }

    // Method to send requests to a specified number of other servers
    public List<String> sendRequestToServers(List<Server> allServers, int numRequests) {
        List<String> flattenedResponses = new ArrayList<>();  // Store all commands in a single list
        Set<Server> contactedServers = new HashSet<>();
        Random random = new Random();

        // Use ExecutorService to manage parallel tasks
        ExecutorService executor = Executors.newFixedThreadPool(numRequests);
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();

        while (contactedServers.size() < numRequests && contactedServers.size() < allServers.size() - 1) {
            // Filter available servers that are ready and haven't been contacted
            List<Server> availableServers = new ArrayList<>();
            for (Server server : allServers) {
                if (server != this && server.isReady() && !contactedServers.contains(server)) {
                    availableServers.add(server);
                }
            }

            // If no servers are available, wait and recheck
            while (availableServers.isEmpty()) {
                System.out.println(this.name + " could not find any available servers to send a request. Waiting...");
                try {
                    Thread.sleep(100); // Wait for 100 milliseconds before rechecking
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                    System.out.println(this.name + " was interrupted while waiting.");
                    return flattenedResponses; // Exit if interrupted
                }

                // Recheck available servers after waiting
                availableServers.clear(); // Clear the list before rechecking
                for (Server server : allServers) {
                    if (server != this && server.isReady() && !contactedServers.contains(server)) {
                        availableServers.add(server);
                    }
                }
            }

            // Randomly select a server to send a request to
            Server targetServer = availableServers.get(random.nextInt(availableServers.size()));
            contactedServers.add(targetServer);

            // Launch a parallel task for each request
            CompletableFuture<List<String>> futureResponse = CompletableFuture.supplyAsync(() -> {
                synchronized (targetServer) {  // Ensure thread safety
                    targetServer.setReady(false);  // Set to false when processing starts
                    List<String> commands = targetServer.getCommandsStored(); // Get the list of commands from the target server
                    targetServer.setReady(true);   // Set to true when processing ends
                    if (commands.isEmpty()) {
                        commands.add("");  // Add an empty string if the server has no commands
                    }
                    System.out.println(this.name + " sent a request to " + targetServer.getName() + " and received commands: " + commands);
                    return commands;  // Return the list of commands as the response
                }
            }, executor);

            futures.add(futureResponse);
        }

        // Collecting all responses asynchronously
        for (CompletableFuture<List<String>> future : futures) {
            try {
                List<String> commandList = future.get();
                flattenedResponses.addAll(commandList); // Flatten the responses into a single list
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown(); // Properly shut down the executor service

        // Print all responses
        //System.out.println("All commands received by " + this.name + ": " + flattenedResponses);

        // Keep the first 3 responses and discard the last 3
        List<String> keptResponses = flattenedResponses.subList(0, Math.min(3, flattenedResponses.size()));

        // Print the kept responses
        System.out.println("First 3 commands kept by " + this.name + ": " + keptResponses);

        String medianCommand = keptResponses.get(1);
        System.out.println("Median Command " + this.name + ": " + medianCommand);

        // Add the median command to the server's list of stored commands
        List<String> currentCommands = this.getCommandsStored();
        currentCommands.add(medianCommand);

        // Assuming commandsStored is a field that you can directly modify
        this.commandsStored = currentCommands;

        // Print the updated list of commands stored in the server
        System.out.println(this.name + "'s stored commands after adding median: " + this.getCommandsStored());

        return keptResponses;  // Return only the first 3 responses
    }





    // In the Server class
    public String provideData(String request) {
        // Generate some data in response to the request
        String responseData = "Data from " + this.name + ": " + Math.random();
        // Here, the data could be anything that the server is supposed to provide.
        // The response could include computed values, stored data, etc.
        return responseData;
    }



    // Method to receive a request and generate a response
    public String receiveRequest(String senderName) {
        String response = "Response from " + this.name + " to " + senderName;
        // Optionally, you can add logic to process the request or update state here
        return response;
    }

    // Existing methods...

}

