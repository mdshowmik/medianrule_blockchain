public class CheckConsensus {

    private ServerManager serverManager;
    private ApplyConsensus applyConsensus;

    public CheckConsensus(ServerManager serverManager) {
        this.serverManager = serverManager;
        //this.applyConsensus = applyConsensus;
        this.applyConsensus = new ApplyConsensus(serverManager);
    }
    // Method to check if all servers are in consensus mode
    public boolean areAllServersInConsensus() {
        return serverManager.areAllServersInConsensus();
    }

    public boolean serversEmpty() {
        return serverManager.serversEmpty();
    }

    // Method to handle the entire process and print the result
    public void checkAndPrintConsensusStatus() {
        //enableConsensusModeForAllServers();
        boolean serversEmpty = serversEmpty();

        if (serversEmpty == true){
            System.out.println("All servers are empty.");
        }
        else{
            //System.out.println("All servers are empty.");
            boolean allInSMRMode = areAllServersInConsensus();
            System.out.println("Are all servers in Consensus? " + allInSMRMode);
            applyConsensus.checkAndInitiateConsensus();
        }
    }
}
