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
    public static boolean concensusComplete = false;


    public ConsensusManager(ServerManager serverManager, Controller controller) {
        this.serverManager = serverManager;
        this.controller = controller;
    }

    public int getRoundForConsensus() {
        //System.out.print(roundForConsensus);
        return roundForConsensus;
    }

    public void makeRequestsAndComputeMedian(PrintStream out) {
        Adversary adversary = new Adversary(serverManager);
        adversary.unblockAllServers(out);
        while (!isConsensusReached(out)) {
            System.out.println("Number of Rounds for Consensus: " + roundForConsensus);
            System.out.println(" ");
            out.println("Number of Rounds for Consensus: " + roundForConsensus);

            adversary.blockRandomServers(out);

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Server sourceServer : serverManager.getServers()) {
                if (sourceServer.isBlocked()) {
                    sourceServer.setNull();
                    continue;
                }

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    CopyOnWriteArrayList<String> responses = new CopyOnWriteArrayList<>();
                    for (int i = 0; i < 6; i++) {  // Send 6 requests
                        Server targetServer;
                        do {
                            targetServer = serverManager.getRandomServer();
                        } while (targetServer == sourceServer || targetServer.isBlocked());

                        String requestData = "Request from " + sourceServer.getName();
                        String response = requestFromServerToServer(sourceServer, targetServer, requestData);
                        if (!response.isEmpty()) {
                            responses.add(response);
                        }
                    }

                    // Check for enough responses before attempting to compute median
                    if (responses.size() < 3) {
                        System.out.println("Not enough responses available");
                        sourceServer.setNull();
                    } else {
                        // Ensure safe access to responses
                        List<String> selectedResponses = new ArrayList<>(responses.subList(0, Math.min(3, responses.size())));
                        Collections.shuffle(selectedResponses);

                        String medianResponse = computeMedianResponse(selectedResponses);
                        System.out.println("Check for median response");
                        System.out.println(medianResponse);
                        if (!medianResponse.isEmpty()) {
                            if (serverManager.checkValidity(medianResponse)) {
                                sourceServer.addCommand(medianResponse);
                                sourceServer.addLeafToMerkleTree(medianResponse);
                            }
                        }
                    }
                });
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    public boolean areMerkleRootsConsistent() {
        String referenceRoot = serverManager.getServers().get(0).getMerkleRoot();
        for (Server server : serverManager.getServers()) {
            if (!referenceRoot.equals(server.getMerkleRoot())) {
                return false;
            }
        }
        return true;
    }
    public void visualizeMerkleTreesAfterConsensus() {

        for (Server server : serverManager.getServers()) {
            if (!server.isBlocked()) {
                try {
                    MerkleTree visualizer = new MerkleTree();
                    String fileName = "merkle_tree_" + server.getName() + ".png";
                    System.out.println("Printing the tree"+fileName);
                    visualizer.visualizeTreeAsImage(server.getMerkleTreeLeaves(), "MerkelTree/"+fileName);
                    System.out.println("Merkle tree for " + server.getName() + " saved as: " + fileName);
                } catch (IOException e) {
                    System.err.println("Error visualizing Merkle tree for " + server.getName() + ": " + e.getMessage());
                }
            }
        }
    }




//    public void makeRequestsAndComputeMedian(PrintStream out) {
//        Adversary adversary = new Adversary(serverManager);
//        adversary.unblockAllServers(out);
//        while (!isConsensusReached(out)) {
//            System.out.println("Number of Rounds for Consensus: " + roundForConsensus);
//            System.out.println(" ");
//            out.println("Number of Rounds for Consensus: " + roundForConsensus);
//
//            adversary.blockRandomServers(out);
//
//            List<CompletableFuture<Void>> futures = new ArrayList<>();
//
//            for (Server sourceServer : serverManager.getServers()) {
//                if (sourceServer.isBlocked()) {
//                    //System.out.println(sourceServer.getName() + " is blocked and cannot receive any key in this round."); uncomment
//                    //System.out.println(sourceServer.getName() + " set to ⊥"); uncomment
//                    sourceServer.setNull();
//                    continue;
//                }
//
//                CopyOnWriteArrayList<String> responses = new CopyOnWriteArrayList<>();
//                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                    for (int i = 0; i < 6; i++) {  // Send 6 requests
//                        Server targetServer;
//                        do {
//                            targetServer = serverManager.getRandomServer();
//                        } while (targetServer == sourceServer || targetServer.isBlocked());
//
//                        String requestData = "Request from " + sourceServer.getName();
//                        String response = requestFromServerToServer(sourceServer, targetServer, requestData);
//                        responses.add(response);
//                        if (!response.isEmpty()) {
//                            //System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": " + response); uncomment
//                        } else {
//                            //System.out.println(sourceServer.getName() + " received response from " + targetServer.getName() + ": ⊥"); uncomment
//                        }
//                    }
//
//                    // Randomly select 3 responses to compute the median
//                    Collections.shuffle(responses);
//                    List<String> selectedResponses = responses.subList(0, 3);
//
//                    if(selectedResponses.size() < 3) {
//                        System.out.println("Not enough responses available");
//                        sourceServer.setNull();
//                    }
//                    else{
//                        String medianResponse = computeMedianResponse(selectedResponses);
//                        if (!medianResponse.isEmpty()) {
//                            //Check validity
//                            if(serverManager.checkValidity(medianResponse) == true){
//                                //System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse); uncomment
//                                sourceServer.addCommand(medianResponse);
//                            }
//                            else{
//                                //System.out.println(sourceServer.getName() + " can't accept median response: " + medianResponse + " as it seems adversarial"); uncomment
//                            }
//                            //System.out.println(sourceServer.getName() + " accepted median response: " + medianResponse);
//                            //sourceServer.addCommand(medianResponse);
//                        } else {
//                            //System.out.println(sourceServer.getName() + " can`t accept ⊥"); uncomment
//                        }
//                    }
//
//                });
//                futures.add(future);
//            }
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//            //adversary.unblockAllServers(out);
//        }
//    }


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
    public double calculateTheValueForMedian(List<String> responses) {
        if(responses.size()>0){
            String returnString="0.";
            for(String response : responses){

                String[] parts = response.split("_");
                String lastPart = parts[parts.length - 1];
                returnString += lastPart;
            }
            try {
                return Double.parseDouble(returnString); // Convert the constructed string to a double
            } catch (NumberFormatException e) {
                System.out.println("Error in converting to double: " + returnString);
                return 0.0; // Return 0.0 in case of conversion error
            }
        }
        return 0.0;
    }

    public String computeMedianResponse(List<String> responses) {

        String firstResponse = responses.get(0);
        String secondResponse = responses.get(1);
        String thirdResponse = responses.get(2);

        List<String> firstResponseList = cleanAndSplit(firstResponse);
        List<String> secondResponseList = cleanAndSplit(secondResponse);
        List<String> thirdResponseList = cleanAndSplit(thirdResponse);


        Double numericValueFirstresponse = calculateTheValueForMedian(firstResponseList);
        System.out.println("Value of First Response: " + numericValueFirstresponse);

        Double numericValueSecondResponse = calculateTheValueForMedian(secondResponseList);
        System.out.println("Value of Second Response: " + numericValueSecondResponse);

        Double numericValueThirdResponse = calculateTheValueForMedian(thirdResponseList);
        System.out.println("Value of Third Response: " + numericValueThirdResponse);

        if (numericValueFirstresponse.equals(0.0) && numericValueSecondResponse.equals(0.0) && numericValueThirdResponse.equals("0.0")) {
            System.out.println("All responses are 0.0, skipping median calculation.");
            return ""; // Or return a special value that indicates this condition
        }


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
        System.out.println("3rd response - " + thirdResponse);

        //System.out.println("subset - " + subset);
        System.out.println("median response - " + finalMedianList);

        return finalMedianList;
    }



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
                    //adversary.unblockAllServers(out);
                    roundForConsensus++;
                    if(roundForConsensus>4){checkCommitted();}
                    //incrementRound();
                    return false; // Return false immediately if any server has an empty command list
                }
//                Collections.sort(storedCommands);
                commandsFromAllServers.add(storedCommands);
            }
        }

        if (commandsFromAllServers.isEmpty()) {
            out.println("No active servers to check for consensus.");
            roundForConsensus++;
            return false;
        }
        else{
            List<String> referenceCommands = commandsFromAllServers.get(0);

            for (List<String> commands : commandsFromAllServers) {
                if (!areCommandsPositionWiseEqual(referenceCommands, commands)) {
                    out.println("Consensus not reached. Discrepancy found between servers.");
                    String currentCSVFilename = Controller.getDirectoryPath().resolve(roundForConsensus+".csv").toString();
                    System.out.println(currentCSVFilename);
                    serverManager.storedInCSVFile(currentCSVFilename);
                    serverManager.printAllStoredCommands(out);
                    roundForConsensus++;
                    if(roundForConsensus>4){checkCommitted();}
                    return false;
                }
            }

            System.out.println("Current command"+ClientManager.totalCommandsSent);
            if(controller.getTotalCommands()!= ClientManager.totalCommandsSent){
                System.out.println("Consensus Reached but Client command still pending");
                out.println("Consensus Reached but Client command still pending");
                String currentCSVFilename = Controller.getDirectoryPath().resolve(roundForConsensus+".csv").toString();
                System.out.println(currentCSVFilename);
                serverManager.storedInCSVFile(currentCSVFilename);
                roundForConsensus++;
                if(roundForConsensus>4){checkCommitted();}

                return false;
            }
            else{
                // If all lists are identical
                adversary.unblockAllServers(out);
                out.println("Consensus Reached in " + roundForConsensus + " rounds");
                System.out.println();
                concensusComplete= true;
                String currentCSVFilename = Controller.getDirectoryPath().resolve(roundForConsensus+".csv").toString();
                System.out.println(currentCSVFilename);
                serverManager.storedInCSVFile(currentCSVFilename);
                serverManager.printAllStoredCommands(out);

                return true;
            }
        }
    }

    public void checkCommitted() {
        System.out.println("Checking if commands are committed based on their rounds...");

        int currentConsensusRound = getRoundForConsensus();
        List<Server> servers = serverManager.getServers();

        for (Server server : servers) {
            List<String> commands = new ArrayList<>(server.getCommandsStored());
            List<String> filteredCommands = new ArrayList<>();

            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                // Extract the round number from the command
                String[] parts = command.split("_");
                if (parts.length <= 1) {
                    // Log warning or skip processing if the command is not in the expected format
                    System.out.println("Skipping command due to unexpected format: " + command);
                    continue;
                }
                String secondPart= parts[1];

                String lastChar = String.valueOf(secondPart.charAt(secondPart.length() - 1));
                int commandRound = Integer.parseInt(lastChar);
                if(currentConsensusRound-commandRound>13){

                    if(commands.size()-i>3){
                        System.out.println("##############");
                        System.out.println("The command is injected in " + commandRound + " round");
                        System.out.println("Current Consensus Round " + currentConsensusRound);
                        System.out.println(commands);
                        System.out.println(command);
                        System.out.println("Total commands in the server " + commands.size());
                        commands.subList(0, i + 1).clear();
                        String remainingCommandsAsString = String.join(",", commands);
                        System.out.println(remainingCommandsAsString);
                        server.addCommand(remainingCommandsAsString);
                        System.out.println(server.getCommandsStored());
                    }

                }

            }

        }
    }



    private boolean areCommandsPositionWiseEqual(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false; // If the lengths are different, they are not identical
        }

        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false; // If any position differs, consensus is not reached
            }
        }
        System.out.println("It is true");
        System.out.println(list1);
        System.out.println(list2);
        return true; // All positions match
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
