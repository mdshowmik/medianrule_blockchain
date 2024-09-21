import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

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

}

