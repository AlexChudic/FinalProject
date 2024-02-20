package refactoring_mining;
import java.util.concurrent.TimeUnit;

import refactoring_mining.Miner;

public final class App {
    private App() {
    }

    /**
     * The main method of the program.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        // String folderPath = "tmp/refactoring-toy-example";
        // Miner miner = new Miner(folderPath, 
        //                         "https://github.com/danilofes/refactoring-toy-example.git", 
        //                         "master");
        // String folderPath = "tmp/java-design-patterns";
        // Miner miner = new Miner(folderPath, 
        //                         "https://github.com/iluwatar/java-design-patterns.git", 
        //                         "master");
        // String folderPath = "tmp/weexteam_hackernews-App-powered-by-Apache-Weex";
        // Miner miner = new Miner(folderPath, 
        //                         "https://github.com/weexteam/hackernews-App-powered-by-Apache-Weex", 
        //                         "master");
        String folderPath = "tmp/traex_ExpandableLayout";
        Miner miner = new Miner(folderPath, 
                                "https://github.com/traex/ExpandableLayout", 
                                "master");
        
        miner.generateJsonForAllSingleFileRefactorings(folderPath);
        miner.populateJsonsWithFileContentOnCommits(folderPath);
        miner.getLLMRefactorings(folderPath);
        // miner.evaluateSingleFileRefactorings(folderPath);

        // HelperTools.getLLMRefactoring("refactoring-data/weexteam_hackernews-App-powered-by-Apache-Weex/92e61bf9d56aa9b7f8d6abb3c54cbc44d63b9d08-91.json");
    
    }
}
