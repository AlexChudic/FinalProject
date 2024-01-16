package refactoring_mining;
import refactoring_mining.Miner;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }
    // sqp_3879eda2af9477bdba56f5a4424c1a2878fca91a
    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        //https://github.com/guydunton/Refactoring-example-cpp.git
        // Miner miner = new Miner("tmp/code-smells-refactoring-training-java", 
        //                         "https://github.com/Codesai/code-smells-refactoring-training-java.git", 
        //                         "master");
        String folderPath = "tmp/refactoring-toy-example";
        Miner miner = new Miner(folderPath, 
                                "https://github.com/danilofes/refactoring-toy-example.git", 
                                "master");
        // String folderPath = "tmp/retrofit";
        // Miner miner = new Miner(folderPath, 
        //                         "https://github.com/square/retrofit.git", 
        //                         "master");
        
        // miner.detectAllCommits(folderPath);
        RepositoryEvaluator evaluator = new RepositoryEvaluator(folderPath);
        evaluator.evaluateRepository();
    }
}
