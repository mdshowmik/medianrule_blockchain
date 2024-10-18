import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsensusManager {
    private ServerManager serverManager;
    int roundForConsensus = 0;

    public ConsensusManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    //initiates adversary and server to server request
    /*public void makeRequestsAndComputeMedian() {
        Adversary adversary = new Adversary(serverManager);
        while (!isConsensusReached()) {
            System.out.println("Number of Rounds for Consensus: " + roundForConsensus);
            System.out.println(" ");

            adversary.blockRandomServers();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Server sourceServer : serverManager.getServers()) {
                if (sourceServer.isBlocked()) {
                    System.out.println(sourceServer.getName() + " is blocked and cannot send requests this round.");
                    continue;
                }

                CopyOnWriteArrayList<String> responses = new CopyOnWriteArrayList<>();
                Set<Server> requestedServers = new HashSet<>();

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (int i = 0; i < 3; i++) {
                        Server targetServer;
                        do {
                            targetServer = serverManager.getRandomServer();
                        } while (targetServer == sourceServer || requestedServers.contains(targetServer) || targetServer.isBlocked());

                        requestedServers.add(targetServer);

                        String requestData = "Request from " + sourceServer.getName();
                        String response = requestFromServerToServer(sourceServer, targetServer, requestData);
                        responses.add(response);
                        if(!response.isEmpty()){
                            System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": " + response);
                        }
                        else {
                            System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": No Data");
                        }
                    }

                    String medianResponse = computeMedianResponse(responses);
                    if(!medianResponse.isEmpty()){
                        System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse);
                    }
                    else{
                        System.out.println(sourceServer.getName() + " accepted median response: No Data");
                    }


                    sourceServer.addCommand(medianResponse);
                });
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            adversary.unblockAllServers();
        }
    }*/

    public void makeRequestsAndComputeMedian() {
        Adversary adversary = new Adversary(serverManager);
        while (!isConsensusReached()) {
            System.out.println("Number of Rounds for Consensus: " + roundForConsensus);
            System.out.println(" ");

            adversary.blockRandomServers();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Server sourceServer : serverManager.getServers()) {
                if (sourceServer.isBlocked()) {
                    System.out.println(sourceServer.getName() + " is blocked and cannot receive any key in this round.");
                    System.out.println(sourceServer.getName() + " set to ⊥");
                    //sourceServer.setNull();
                    continue;
                }

                CopyOnWriteArrayList<String> responses = new CopyOnWriteArrayList<>();
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (int i = 0; i < 6; i++) {  // Send 6 requests
                        Server targetServer;
                        do {
                            targetServer = serverManager.getRandomServer();
                        } while (targetServer == sourceServer || targetServer.isBlocked());

                        String requestData = "Request from " + sourceServer.getName();
                        String response = requestFromServerToServer(sourceServer, targetServer, requestData);
                        responses.add(response);
                        if (!response.isEmpty()) {
                            System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": " + response);
                        } else {
                            System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": ⊥");
                        }
                    }

                    // Randomly select 3 responses to compute the median
                    Collections.shuffle(responses);
                    List<String> selectedResponses = responses.subList(0, 3);

                    if(selectedResponses.size() < 3) {
                        System.out.println("Not enough responses available");
                        sourceServer.setNull();
                    }
                    else{
                        String medianResponse = computeMedianResponse(selectedResponses);
                        if (!medianResponse.isEmpty()) {
                            System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse);
                            sourceServer.addCommand(medianResponse);
                        } else {
                            System.out.println(sourceServer.getName() + " can`t accept ⊥");
                        }
                    }

                });
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            adversary.unblockAllServers();
        }
    }


    public String requestFromServerToServer(Server sourceServer, Server targetServer, String requestData) {
        String response;
        try (Socket socket = new Socket("localhost", targetServer.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println(sourceServer.getName() + " is requesting data from " + targetServer.getName());
            out.println(requestData);

            response = in.readLine();
            //System.out.println(sourceServer.getName() + " received response: " + response);

        } catch (IOException e) {
            System.err.println("Error during communication with " + targetServer.getName() + ": " + e.getMessage());
            response = targetServer.getName() + " is busy or unavailable.";
        }
        return response;
    }



    public String computeMedianResponse(List<String> responses) {
        //Collections.sort(responses);
        int medianIndex = responses.size() / 2;
        return responses.get(medianIndex);
    }

    public boolean isConsensusReached() {
        List<String> referenceCommand = serverManager.getServers().get(0).getCommandsStored();
        if(referenceCommand.isEmpty()){
            System.out.println("reference: " + referenceCommand);
            System.out.println(" ");
            roundForConsensus++;
            return false;
        }
        else{
            Collections.sort(referenceCommand);

            for (int i = 1; i < serverManager.getServers().size(); i++) {
                List<String> commands = serverManager.getServers().get(i).getCommandsStored();
                Collections.sort(commands);
                System.out.println("reference: " + referenceCommand);
                System.out.println("compare: " + commands);
                System.out.println(commands.equals(referenceCommand));
                System.out.println(" ");
                if (commands.isEmpty() || !commands.equals(referenceCommand)) {
                    roundForConsensus++;
                    serverManager.printAllStoredCommands();
                    return false;

                }
            }
        }
        System.out.println("Consensus Reached in "+ roundForConsensus + " round");
        serverManager.printAllStoredCommands();
        return true;
    }
}
