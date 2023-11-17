package refactoring_mining;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Miner {
    private GitService gitService = new GitServiceImpl();
    private GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private Repository repo;
    private String branch;

    /**
	 * Indicate commits that should be ignored.
	 * You may override this method to implement custom logic.
	 *  
	 * @param folder The local path where to clone repo.
     * @param url The url of the Gitrepository.
     * @param branch The branch of the repository.
	 */
    public Miner(String folder, String url, String branch) {
        try {
            this.branch = branch;
            setRepo(folder, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchRevWalk(){
        try {
            RevWalk walk = gitService.createAllRevsWalk(this.repo, this.branch);
            Iterator<RevCommit> it = walk.iterator();
            while(it.hasNext()){
                RevCommit currentCommit = it.next();
                Set<String> filePathsBefore = new LinkedHashSet<String>();
                Set<String> filePathsCurrent = new LinkedHashSet<String>();
                Map<String, String> renamedFilesHint = new HashMap<String, String>();
                gitService.fileTreeDiff(this.repo, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
                
                Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
                Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
                Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
                Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
                
                if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() > 0) {
                    RevCommit parentCommit = currentCommit.getParent(0);
                    GitHistoryRefactoringMinerImpl.populateFileContents(this.repo, parentCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                    GitHistoryRefactoringMinerImpl.populateFileContents(this.repo, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
                    List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings = GitHistoryRefactoringMinerImpl.processIdenticalFiles(fileContentsBefore, fileContentsCurrent, renamedFilesHint, false); 
                    UMLModel parentUMLModel = GitHistoryRefactoringMinerImpl.createModel(fileContentsBefore, repositoryDirectoriesBefore);
                    UMLModel currentUMLModel = GitHistoryRefactoringMinerImpl.createModel(fileContentsCurrent, repositoryDirectoriesCurrent);
                    //UMLModelDiff modelDiff = parentUMLModel.diff(currentUMLModel);

				
                }
                //System.out.println(commit.getFullMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void detectAllCommits(){
        try {
            miner.detectAll(this.repo, this.branch, new RefactoringHandler() {
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

    public void setRepo(String folder, String url) {
        try {
            this.repo = gitService.cloneIfNotExists(folder, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
