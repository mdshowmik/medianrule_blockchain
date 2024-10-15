import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable {
    private int port;
    private boolean running;
    private String name;
    private ServerSocket serverSocket;
    private int commandCount;
    private List<String> commandsReceived;
    //private List<String> commandsStored;
    private boolean busy;
    private boolean consensus;
    private boolean isready;
    private boolean blocked = false;
    private CopyOnWriteArrayList<String> commandsStored = new CopyOnWriteArrayList<>();


    public Server(String name, int port) throws IOException {
        this.name = name;
        this.port = port;
        this.running = false;
        this.commandCount = 0;
        this.commandsReceived = new ArrayList<>();
        //this.commandsStored = new ArrayList<>();
        this.busy = false;
        bindToPort(port);
        this.consensus = false;
        this.isready = true;
    }

    //assigns port
    private void bindToPort(int port) throws IOException {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                this.serverSocket = new ServerSocket(port);
                //System.out.println(name + " started on port " + port);
                this.port = port;
                break;
            } catch (IOException e) {
                System.out.println("Port " + port + " is unavailable. Trying next port...");
                port++;
            }
        }
        if (this.serverSocket == null) {
            throw new IOException("Failed to bind to a port after " + maxRetries + " attempts.");
        }
    }

    public String getName() {
        return name;
    }

    /*public List<String> getCommandsReceived() {
        return new ArrayList<>(commandsReceived);
    }*/

    //returns servers`s data
    public List<String> getCommandsStored() {
        return new ArrayList<>(commandsStored);
    }

    public void start() throws IOException {
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

    /*@Override
    public void run() {
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                busy = true;
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
    }*/

    //handles requests-responses of servers
    @Override
    public void run() {
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                busy = true;

                String command = in.readLine();

                if (command != null) {

                    if (command.startsWith("Request")) {
                        String response = processRequest(command);
                        out.println(response);  // Send the server's data back to the requester

                        if(!response.isEmpty()){
                            //System.out.println(name + " sent response: " + response);
                        }
                        else{
                            //System.out.println(name + " sent response: ⊥");
                        }

                    } else {
                        //System.out.println(name + " received: " + command);
                        commandsStored.add(command);
                        commandCount++;
                        commandsReceived.add(command);

                        out.println(command + " Received");
                        //System.out.println(name + " stored command: " + command);
                    }
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
    public String getData() {
        if (!commandsStored.isEmpty()) {
            return commandsStored.get(commandsStored.size() - 1);
        }
        return null;
    }

    //return stored data
    public String processRequest(String requestData) {
        //System.out.println(name + " received a request: " + requestData);
        return String.join(", ", commandsStored);
    }


    //add returned data from other servers
    /*public void addCommand(String command) {
        if(command != null){
            String[] commandFromServer = command.split(",");

            for (String breakingCommandList:commandFromServer) {
                breakingCommandList = breakingCommandList.replaceAll("\\s","");

                if(!breakingCommandList.isEmpty() && !commandsStored.contains(breakingCommandList)){
                    commandsStored.add(breakingCommandList);
                    System.out.println(name + " added command: " + breakingCommandList);
                }
            }
        }
    }*/

    public void addCommand(String command) {
        commandsStored.clear();  // Clear all previous commands
        commandsStored.add(command);  // Add the new command
        //System.out.println(name + " added command: " + command);
    }


    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBlocked() {
        return blocked;
    }

}

