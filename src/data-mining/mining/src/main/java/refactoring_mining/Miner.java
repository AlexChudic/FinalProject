package refactoring_mining;

import java.util.List;

import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.eclipse.jgit.lib.Repository;

public class Miner {

    public void runMiner(){
        try {
            GitService gitService = new GitServiceImpl();
            GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

            Repository repo = gitService.cloneIfNotExists(
                "tmp/refactoring-toy-example",
                "https://github.com/danilofes/refactoring-toy-example.git");

            miner.detectAll(repo, "master", new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    System.out.println("Refactorings at " + commitId);
                    for (Refactoring ref : refactorings) {
                        System.out.println(ref.toString());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
