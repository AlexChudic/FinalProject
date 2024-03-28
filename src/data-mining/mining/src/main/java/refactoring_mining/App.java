package refactoring_mining;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import refactoring_mining.Miner;

public final class App {
    private App() {
    }

    /**
     * The main method of the program.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        processSampledRepositories();

        // String folderPath = "tmp/incubator-paimon";
        // Miner miner = new Miner(folderPath, 
        //                         "https://github.com/apache/incubator-paimon.git", 
        //                         "master");

        // miner.generateJsonForAllSingleFileRefactorings(folderPath);
        // miner.populateJsonsWithFileContentOnCommits(folderPath);
        // miner.getLLMRefactorings(folderPath);
        // miner.evaluateSingleFileRefactorings(folderPath);

        // RepositoryEvaluator repositoryEvaluator = new RepositoryEvaluator("tmp/incubator-paimon");
        // repositoryEvaluator.evaluateRepository();
    }

    public static void processSampledRepositories() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get("data/evaluate_repositories.json")));
            JSONObject json = new JSONObject(jsonString);

            for (String repoid : json.keySet()) {
                // Get the value associated with the key
                JSONObject repo = json.getJSONObject(repoid);
                String repoName = repo.getString("name");
                String repoURL = repo.getString("url");
                String branch = repo.getString("main_branch");

                // if( Files.exists(Paths.get("refactoring-data/"+repoName))){
                //     continue;
                // }

                String folderPath = "tmp/" + repoName;
                Miner miner = new Miner(folderPath, repoURL, branch);


                String refactoringDataFolder = "refactoring-data/"+repoName;
                HelperTools.createFolder(refactoringDataFolder);
                if(HelperTools.getNumberOfFilesInFolder(refactoringDataFolder) < 1) {
                    System.out.println("Creating JSON for all single file refactorings. Existing: " + HelperTools.getNumberOfFilesInFolder(refactoringDataFolder));
                    miner.generateJsonForAllSingleFileRefactorings(folderPath);
                }
                miner.populateJsonsWithFileContentOnCommits(folderPath);
                miner.getLLMRefactorings(folderPath);
                miner.evaluateSingleFileRefactorings(folderPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
