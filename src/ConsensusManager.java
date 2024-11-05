import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
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
        adversary.unblockAllServers();
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
                            //Check validity
                            if(serverManager.checkValidity(medianResponse) == true){
                                System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse);
                                sourceServer.addCommand(medianResponse);
                            }
                            else{
                                System.out.println(sourceServer.getName() + " can't accept median response: " + medianResponse + " as it seems adversarial");
                            }
                            //System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse);
                            //sourceServer.addCommand(medianResponse);
                        } else {
                            System.out.println(sourceServer.getName() + " can`t accept ⊥");
                        }
                    }

                });
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            //adversary.unblockAllServers();
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
        /*//Collections.sort(responses);
        System.out.println("3 responses" + responses);
        //int medianIndex = responses.size() / 2;

        String firstResponse = responses.get(0);
        String middleResponse = responses.get(1);
        String lastResponse = responses.get(2);

        System.out.println("separated respomses: "+firstResponse+middleResponse+lastResponse);

        //String mergedString = responses.get(0) +"," + responses.get(1) + "," + responses.get(0);

        String mergedString = String.join(",",responses);

        List<String> merged = new ArrayList<>(Arrays.asList(mergedString.split(",")));

        Set<String> uniqueData = new HashSet<>(merged);
        uniqueData.remove("");
        uniqueData.remove(null);

        String finalMedianList = String.join(", ", uniqueData);

        System.out.println("mergered responses: " + finalMedianList);
        //List<String> resultList = new ArrayList<>(uniqueData);

        //System.out.println("first to last"+firstResponse+middleResponse+lastResponse);

        return finalMedianList;*/

        Set<String> uniqueData = new HashSet<>(responses);
        uniqueData.remove("");
        uniqueData.remove(null);

        String finalMedianList = String.join(", ", uniqueData);

        //System.out.println("merged responses: " + finalMedianList);

        return finalMedianList;

    }

    public boolean isConsensusReached() {
        Adversary adversary = new Adversary(serverManager);
        for (int i = 0; i < serverManager.getServers().size(); i++){
            if(!serverManager.getServers().get(i).isBlocked()){
                List<String> referenceCommand = serverManager.getServers().get(i).getCommandsStored();
                if(referenceCommand.isEmpty()){
                    System.out.println("reference: " + referenceCommand);
                    System.out.println(" ");
                    roundForConsensus++;
                    return false;
                }
                else{
                    Collections.sort(referenceCommand);
                    for (int j = i+1; j < serverManager.getServers().size(); j++) {
                        if(!serverManager.getServers().get(j).isBlocked()){
                            List<String> commands = serverManager.getServers().get(j).getCommandsStored();
                            Collections.sort(commands);
                            System.out.println("reference: " + referenceCommand + " from " + serverManager.getServers().get(i).getName());
                            System.out.println("compare: " + commands + " from " + serverManager.getServers().get(j).getName());
                            //System.out.println("compare: " + commands);
                            System.out.println(commands.equals(referenceCommand));
                            System.out.println(" ");
                            if (commands.isEmpty() || !commands.equals(referenceCommand)) {
                                roundForConsensus++;
                                adversary.unblockAllServers();
                                serverManager.printAllStoredCommands();
                                return false;
                            }
                        }
                    }
                }
            }
            break;
        }
        adversary.unblockAllServers();
        System.out.println("Consensus Reached in "+ roundForConsensus + " round");
        serverManager.printAllStoredCommands();
        return true;
    }

    public boolean checkValidity(Server sourceServer, String command) {
        List<Server> servers = serverManager.getServers();
        for (Server server : servers) {
            if (server != sourceServer && server.isCommandStored(command)) {  // Skip the source server
                System.out.println(command + " is already stored on " + server.getName());
                return true; //valid data
            }
        }
        System.out.println(command + " is not stored on any server except " + sourceServer.getName() + ".");
        return false; //invalid data
    }
}
