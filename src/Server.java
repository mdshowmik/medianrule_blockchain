import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
    private int port;
    private boolean running;
    private String name;
    private ServerSocket serverSocket;
    private int commandCount; // Track the number of commands received
    private List<String> commandsReceived; // Store the received commands

    // Constructor to initialize the server
    public Server(String name, int port) {
        this.name = name;
        this.port = port;
        this.running = false;
        this.commandCount = 0;
        this.commandsReceived = new ArrayList<>(); // Initialize the command list
    }

    public String getName() {
        return name;
    }

    public List<String> getCommandsReceived() {
        return new ArrayList<>(commandsReceived); // Return a copy to avoid external modification
    }

    public void setCommandsReceived(List<String> commands) {
        this.commandsReceived = new ArrayList<>(commands); // Set a copy of the list
    }

    // Method to start the server
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println(name + " started on port " + port);
        new Thread(this).start();
    }

    // Method to stop the server
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        System.out.println(name + " stopped.");
    }

    // Method to print the server's status (name, port, running state, command count, commands received)
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

    // Method to listen for commands
    @Override
    public void run() {
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String command = in.readLine();
                if (command != null) {
                    commandCount++; // Increment the command count when a command is received
                    commandsReceived.add(command); // Add the command to the list
                    System.out.println(name + " received command: " + command);
                    if ("STOP".equalsIgnoreCase(command)) {
                        stop();
                    } else {
                        out.println("Command received: " + command);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error in " + name + ": " + e.getMessage());
                }
            }
        }
    }


    // Add a new command if it doesn't already exist
    /*public void addCommand(String command) {
        if (!commandsReceived.contains(command)) {
            commandsReceived.add(command);
        }
    }*/
}
