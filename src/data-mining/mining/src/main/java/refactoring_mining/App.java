package refactoring_mining;
import refactoring_mining.Miner;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        Miner miner = new Miner("tmp/refactoring-example", 
                                "https://github.com/guydunton/Refactoring-example-cpp.git", 
                                "master");
        miner.fetchRevWalk();
    }
}
