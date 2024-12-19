import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsensusManager {
    private ServerManager serverManager;
    private Controller controller;
    //private AtomicInteger roundForConsensus = new AtomicInteger(0);
    public int roundForConsensus = 0;


    public ConsensusManager(ServerManager serverManager, Controller controller) {
        this.serverManager = serverManager;
        this.controller = controller;
    }

    public int getRoundForConsensus() {
        //System.out.print(roundForConsensus);
        return roundForConsensus;
    }

    /*public void incrementRound() {
        roundForConsensus.incrementAndGet();
    }*/

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

    public void makeRequestsAndComputeMedian(PrintStream out) {
        Adversary adversary = new Adversary(serverManager);
        adversary.unblockAllServers();
        while (!isConsensusReached(out)) {
            System.out.println("Number of Rounds for Consensus: " + roundForConsensus);
            System.out.println(" ");
            out.println("Number of Rounds for Consensus: " + roundForConsensus);

            adversary.blockRandomServers();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Server sourceServer : serverManager.getServers()) {
                if (sourceServer.isBlocked()) {
                    //System.out.println(sourceServer.getName() + " is blocked and cannot receive any key in this round."); uncomment
                    //System.out.println(sourceServer.getName() + " set to ⊥"); uncomment
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
                            //System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": " + response); uncomment
                        } else {
                            //System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": ⊥"); uncomment
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
                                //System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse); uncomment
                                sourceServer.addCommand(medianResponse);
                            }
                            else{
                                //System.out.println(sourceServer.getName() + " can't accept median response: " + medianResponse + " as it seems adversarial"); uncomment
                            }
                            //System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse);
                            //sourceServer.addCommand(medianResponse);
                        } else {
                            //System.out.println(sourceServer.getName() + " can`t accept ⊥"); uncomment
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

            //System.out.println(sourceServer.getName() + " is requesting data from " + targetServer.getName()); uncomment
            out.println(requestData);

            response = in.readLine();
            //System.out.println(sourceServer.getName() + " received response: " + response);

        } catch (IOException e) {
            System.err.println("Error during communication with " + targetServer.getName() + ": " + e.getMessage());
            response = targetServer.getName() + " is busy or unavailable.";
        }
        return response;
    }

    private List<String> cleanAndSplit(String response) {
        // Split the string on commas and trim each value
        List<String> cleanedList = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            String[] splitArray = response.split(",");
            for (String value : splitArray) {
                String trimmedValue = value.trim(); // Remove all extra spaces
                if (!trimmedValue.isEmpty()) { // Ignore empty entries after trimming
                    cleanedList.add(trimmedValue);
                }
            }
        }
        return cleanedList;
    }
    public String computeMedianResponse(List<String> responses) {

        String firstResponse = responses.get(0);
        String secondResponse = responses.get(1);
        String thirdreslist = responses.get(2);


        List<String> firstResponseList = cleanAndSplit(firstResponse);
        List<String> secondResponseList = cleanAndSplit(secondResponse);
        List<String> thirdResponseList = cleanAndSplit(thirdreslist);


        String combinedStringofFirstResponse = String.join("", firstResponseList);
        double numericValueFirstresponse = Double.parseDouble("0." + combinedStringofFirstResponse);
        System.out.println("Value of First Response: " + numericValueFirstresponse);


        String combinedStringofSecondResponse = String.join("", secondResponseList);
        double numericValueSecondResponse = Double.parseDouble("0." + combinedStringofSecondResponse);
        System.out.println("Value of Second Response: " + numericValueSecondResponse);

        String combinedStringofthirdResponse = String.join("", thirdResponseList);
        double numericValueThirdResponse = Double.parseDouble("0." + combinedStringofthirdResponse);
        System.out.println("Value of Third Response: " + numericValueThirdResponse);

        Set<String> medianListSubset = new LinkedHashSet<>();

        if((numericValueFirstresponse >= numericValueSecondResponse && numericValueFirstresponse <= numericValueThirdResponse) || (numericValueFirstresponse <= numericValueSecondResponse && numericValueFirstresponse >= numericValueThirdResponse)){
            medianListSubset.addAll(firstResponseList);

            medianListSubset.addAll(secondResponseList);
            medianListSubset.addAll(thirdResponseList);

        }
        else if((numericValueSecondResponse >= numericValueFirstresponse && numericValueSecondResponse <= numericValueThirdResponse) || (numericValueSecondResponse <= numericValueFirstresponse && numericValueSecondResponse >= numericValueThirdResponse)){
            medianListSubset.addAll(secondResponseList);

            medianListSubset.addAll(firstResponseList);
            medianListSubset.addAll(thirdResponseList);
        }
        else if((numericValueThirdResponse >= numericValueFirstresponse && numericValueThirdResponse <= numericValueSecondResponse) || (numericValueThirdResponse <= numericValueFirstresponse && numericValueThirdResponse >= numericValueSecondResponse)){
            medianListSubset.addAll(thirdResponseList);

            medianListSubset.addAll(firstResponseList);
            medianListSubset.addAll(secondResponseList);
        }
        else{
            medianListSubset.addAll(firstResponseList);
            medianListSubset.addAll(secondResponseList);
            medianListSubset.addAll(thirdResponseList);
        }

        // Remove empty and null values
        medianListSubset.remove("");
        medianListSubset.remove(null);


        String finalMedianList = String.join(", ", medianListSubset);

        System.out.println("1st response - " + firstResponse);
        System.out.println("2nd response - " + secondResponse);
        System.out.println("3rd response - " + thirdreslist);

        //System.out.println("subset - " + subset);
        System.out.println("median response - " + finalMedianList);

        return finalMedianList;
    }

    /*public String computeMedianResponse(List<String> responses) {

        String firstResponse = responses.get(0);
        String middleResponse = responses.get(1);
        String lastResponse = responses.get(2);

        System.out.println("1st response - " + firstResponse);
        System.out.println("2nd response - " + middleResponse);
        System.out.println("3rd response - " + lastResponse);

        Set<String> uniqueData = new HashSet<>(responses);
        uniqueData.remove("");
        uniqueData.remove(null);

        String finalMedianList = String.join(", ", uniqueData);

        System.out.println("median response - " + finalMedianList);

        return finalMedianList;
    }*/

//    public boolean isConsensusReached() {
//        Adversary adversary = new Adversary(serverManager);
//        for (int i = 0; i < serverManager.getServers().size(); i++){
//            if(!serverManager.getServers().get(i).isBlocked()){
//                List<String> referenceCommand = serverManager.getServers().get(i).getCommandsStored();
//                if(referenceCommand.isEmpty()){
//                    System.out.println("reference: " + referenceCommand);
//                    System.out.println(" ");
//                    roundForConsensus++;
//                    return false;
//                }
//                else{
//                    Collections.sort(referenceCommand);
//                    for (int j = i+1; j < serverManager.getServers().size(); j++) {
//                        if(!serverManager.getServers().get(j).isBlocked()){
//                            List<String> commands = serverManager.getServers().get(j).getCommandsStored();
//                            Collections.sort(commands);
//                            System.out.println("reference: " + referenceCommand + " from " + serverManager.getServers().get(i).getName());
//                            System.out.println(serverManager.getServers().get(j).getName());
//                            System.out.println("compare: " + commands + " from " + serverManager.getServers().get(j).getName());
//                            //System.out.println("compare: " + commands);
//                            System.out.println(commands.equals(referenceCommand));
//                            System.out.println(" ");
//                            if (commands.isEmpty() || !commands.equals(referenceCommand)) {
//                                roundForConsensus++;
//                                adversary.unblockAllServers();
//                                serverManager.printAllStoredCommands();
//                                return false;
//                            }
//                        }
//                    }
//                }
//            }
//            break;
//        }
//        adversary.unblockAllServers();
//        System.out.println("Consensus Reached in "+ roundForConsensus + " round");
//        serverManager.printAllStoredCommands();
//        return true;
//    }


//    public boolean isConsensusReached() {
//        Adversary adversary = new Adversary(serverManager);
//        List<Server> servers = serverManager.getServers();
//        List<List<String>> commandsFromAllServers = new ArrayList<>();
//
//        // Prepare and collect commands from all non-blocked servers
//        for (Server server : servers) {
//            if (!server.isBlocked()) {
//                List<String> storedCommands = new ArrayList<>(server.getCommandsStored());
//                Collections.sort(storedCommands);
//                commandsFromAllServers.add(storedCommands);
//            }
//        }
//
//        if (commandsFromAllServers.isEmpty()) {
//            System.out.println("No active servers to check for consensus.");
//            return false;
//        }
//
//        // Check if all non-blocked servers have identical command lists
//        List<String> referenceCommands = commandsFromAllServers.get(0);
//        for (int i = 1; i < commandsFromAllServers.size(); i++) {
//            System.out.println("Reference commands from first active server: " + referenceCommands);
//            System.out.println("Comparing with commands from server " + (i+1) + ": " + commandsFromAllServers.get(i));
//
//            if (!commandsFromAllServers.get(i).equals(referenceCommands)) {
//                System.out.println("Consensus not reached. Discrepancy found between server 1 and server " + (i+1) + ".");
//                roundForConsensus++;
//                adversary.unblockAllServers();
//                serverManager.printAllStoredCommands();
//                return false;
//            }
//        }
//
//        // If all lists are identical
//        adversary.unblockAllServers();
//        System.out.println("Consensus Reached in " + roundForConsensus + " rounds");
//        serverManager.printAllStoredCommands();
//        return true;
//    }


    /*public boolean isConsensusReached() { //uncomment
        Adversary adversary = new Adversary(serverManager);
        List<Server> servers = serverManager.getServers();
        List<List<String>> commandsFromAllServers = new ArrayList<>();

        // Prepare and collect commands from all non-blocked servers
        for (Server server : servers) {
            if (!server.isBlocked()) {
                List<String> storedCommands = new ArrayList<>(server.getCommandsStored());
                if (storedCommands.isEmpty()) {
                    System.out.println(server.getName() + " has no commands stored.");
                    adversary.unblockAllServers();
                    roundForConsensus++;
                    //incrementRound();
                    return false; // Return false immediately if any server has an empty command list
                }
                Collections.sort(storedCommands);
                commandsFromAllServers.add(storedCommands);
            }
        }

        if (commandsFromAllServers.isEmpty()) {
            System.out.println("No active servers to check for consensus.");
            return false;
        }

        // Check if all non-blocked servers have identical command lists
        List<String> referenceCommands = commandsFromAllServers.get(0);
        for (List<String> commands : commandsFromAllServers) {
            if (!commands.equals(referenceCommands)) {
                System.out.println("Consensus not reached. Discrepancy found between servers.");
                adversary.unblockAllServers();
                roundForConsensus++;
                //incrementRound();
                serverManager.printAllStoredCommands();
                return false;
            }
        }

        // If all lists are identical and non-empty
        adversary.unblockAllServers();
        System.out.println("Consensus Reached in " + roundForConsensus + " rounds");
        serverManager.printAllStoredCommands();
        return true;
    }*/

    public boolean isConsensusReached(PrintStream out) {
        Adversary adversary = new Adversary(serverManager);
        List<Server> servers = serverManager.getServers();
        List<List<String>> commandsFromAllServers = new ArrayList<>();

        // Prepare and collect commands from all non-blocked servers
        for (Server server : servers) {
            if (!server.isBlocked()) {
                List<String> storedCommands = new ArrayList<>(server.getCommandsStored());
                if (storedCommands.isEmpty()) {
                    out.println(server.getName() + " has no commands stored.");
                    adversary.unblockAllServers();
                    roundForConsensus++;
                    //incrementRound();
                    return false; // Return false immediately if any server has an empty command list
                }
                Collections.sort(storedCommands);
                commandsFromAllServers.add(storedCommands);
            }
        }

        if (commandsFromAllServers.isEmpty()) {
            out.println("No active servers to check for consensus.");
            return false;
        }

        // Check if all non-blocked servers have identical command lists
        List<String> referenceCommands = commandsFromAllServers.get(0);
        for (List<String> commands : commandsFromAllServers) {
            if (!commands.equals(referenceCommands)) {
                out.println("Consensus not reached. Discrepancy found between servers.");
                adversary.unblockAllServers();
                roundForConsensus++;
                //incrementRound();
                serverManager.printAllStoredCommands(out);
                return false;
            }
        }

        // If all lists are identical
        adversary.unblockAllServers();
        out.println("Consensus Reached in " + roundForConsensus + " rounds");
        serverManager.printAllStoredCommands(out);
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
