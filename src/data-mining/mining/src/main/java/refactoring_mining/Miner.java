package refactoring_mining;

import java.io.File;
import java.util.ArrayList;
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
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.decomposition.AbstractCodeMapping;
import gr.uom.java.xmi.decomposition.CompositeStatementObject;
import gr.uom.java.xmi.decomposition.StatementObject;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import gr.uom.java.xmi.diff.MoveAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    // TESTING other refactoring miner functionality
    private void getRefactoringLines(Refactoring ref){
        if(ref instanceof ExtractOperationRefactoring) { //TODO do this for all refactoring types
            ExtractOperationRefactoring ex = (ExtractOperationRefactoring) ref;
            UMLOperationBodyMapper bodyMapper = ex.getBodyMapper();
            UMLOperationBodyMapper parentMapper = bodyMapper.getParentMapper();
            Set<AbstractCodeMapping> mappings = bodyMapper.getMappings();
            Set<AbstractCodeFragment> codeFragment = ex.getExtractedCodeFragmentsFromSourceOperation();
            Set<AbstractCodeFragment> codeFragment2 = ex.getExtractedCodeFragmentsToExtractedOperation();

            System.out.println( "BEFORE" );
            CodeRange newCodeRange = ex.getSourceOperationCodeRangeAfterExtraction();
            System.out.println( newCodeRange.toString() );
            for (AbstractCodeFragment abstractCodeFragment : codeFragment2) {
                System.out.println( abstractCodeFragment.getParent() );
                System.out.println( abstractCodeFragment.getArgumentizedString() );
            }

            System.out.println( "AFTER" );
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeExtraction();
            System.out.println( parentCodeRange.toString() );

            for (AbstractCodeFragment abstractCodeFragment : codeFragment) {
                System.out.println( abstractCodeFragment.getArgumentizedString() );
            }
            
        }
    }

    private JSONObject createJSONForRefactoring(Refactoring ref, CodeRange parentCodeRange, CodeRange newCodeRange, String folderPath){
        JSONObject json = new JSONObject();
        json.put("refactoringType", ref.getRefactoringType().toString());
        json.put("description", ref.toString());
        try {
            JSONObject parentFile = new JSONObject();
            JSONObject newFile = new JSONObject();

            parentFile.put("startLine", String.valueOf(parentCodeRange.getStartLine()));
            parentFile.put("endLine", String.valueOf(parentCodeRange.getEndLine()));
            ArrayList<String> parentFileCode = new ArrayList<String>();
            for( String line : Files.readAllLines(Paths.get(folderPath + "/" + parentCodeRange.getFilePath()))){
                parentFileCode.add(line);
            }
            parentFile.put("file", new JSONArray(parentFileCode));

            newFile.put("startLine", String.valueOf(newCodeRange.getStartLine()));
            newFile.put("endLine", String.valueOf(newCodeRange.getEndLine()));
            ArrayList<String> newFileCode = new ArrayList<String>();
            for( String line : Files.readAllLines(Paths.get(folderPath + "/" + newCodeRange.getFilePath()))){
                newFileCode.add(line);
            }
            newFile.put("file", new JSONArray(newFileCode));

            json.put("beforeRefactoring", parentFile);
            json.put("afterRefactoring", newFile);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return json;
    }

    private JSONObject getRefactoringData(Refactoring ref, String folderPath){
        JSONObject json = new JSONObject();
        if(ref instanceof ExtractOperationRefactoring) { 
            ExtractOperationRefactoring ex = (ExtractOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeExtraction();
            CodeRange newCodeRange = ex.getSourceOperationCodeRangeAfterExtraction();

            json = createJSONForRefactoring(ref, parentCodeRange, newCodeRange, folderPath);
        }
        if(ref instanceof InlineOperationRefactoring) { 
            InlineOperationRefactoring ex = (InlineOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getTargetOperationCodeRangeBeforeInline();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterInline();

            json = createJSONForRefactoring(ref, parentCodeRange, newCodeRange, folderPath);
        }
        if(ref instanceof RenameOperationRefactoring) { 
            RenameOperationRefactoring ex = (RenameOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeRename();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterRename();

            json = createJSONForRefactoring(ref, parentCodeRange, newCodeRange, folderPath);
        }
        if(ref instanceof MoveOperationRefactoring) { 
            MoveOperationRefactoring ex = (MoveOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeMove();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterMove();

            json = createJSONForRefactoring(ref, parentCodeRange, newCodeRange, folderPath);
        }
        if(ref instanceof MoveAttributeRefactoring) { 
            MoveAttributeRefactoring ex = (MoveAttributeRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceAttributeCodeRangeBeforeMove();
            CodeRange newCodeRange = ex.getTargetAttributeCodeRangeAfterMove();

            json = createJSONForRefactoring(ref, parentCodeRange, newCodeRange, folderPath);
        }

        return json;
    }

    private void buildPromptContext(Refactoring ref, String refId, String folderPath){
        JSONObject json = getRefactoringData(ref, folderPath);
        String filePath = "refactoring-data/" + folderPath.substring(4) + "/" + refId + ".json";

        try (FileWriter fileWriter = new FileWriter(filePath)){
            fileWriter.write(json.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detectAllCommits(String folderPath){
        try {
            miner.detectAll(this.repo, this.branch, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    System.out.println("Refactorings at " + commitId);
                    int id = 0;
                    for (Refactoring ref : refactorings) {
                        System.out.println(ref.toString());
                        String refId = commitId + "-" + id;
                        buildPromptContext(ref, refId, folderPath);
                        id++;
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
