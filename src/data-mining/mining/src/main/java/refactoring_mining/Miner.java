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
import java.util.stream.Stream;

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
import gr.uom.java.xmi.diff.AddParameterRefactoring;
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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Miner {
    private GitService gitService = new GitServiceImpl();
    private GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    private Repository repo;
    private String branch;
    private RevWalk repoWalk;

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
            this.repoWalk = this.gitService.createAllRevsWalk(repo, branch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getParentCommitId(String commitId) {
        try{
            RevCommit commit = getCommitById(commitId);

            if (commit != null) {
                RevCommit[] parents = commit.getParents();
                return parents[0].getId().getName();
            } else {
                return new String() ;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new String();
        }
    }

    private RevCommit getCommitById(String commitId) throws IOException {
        return repoWalk.parseCommit(repo.resolve(commitId));
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

    private JSONObject createJSONForRefactoring(Refactoring ref, String refId, CodeRange parentCodeRange, CodeRange newCodeRange, String folderPath){
        JSONObject json = new JSONObject();
        json.put("refactoringType", ref.getRefactoringType().toString());
        json.put("description", ref.toString());
        json.put("path", folderPath);
        json.put("commitId", refId);
        try {
            JSONObject parentFile = new JSONObject();
            JSONObject newFile = new JSONObject();

            parentFile.put("startLine", String.valueOf(parentCodeRange.getStartLine()));
            parentFile.put("endLine", String.valueOf(parentCodeRange.getEndLine()));
            parentFile.put("filePath", parentCodeRange.getFilePath());
            
            // this is redundant, as the right commit is not checked out
            if( Files.exists(Paths.get(folderPath + "/" + parentCodeRange.getFilePath()))){
                ArrayList<String> parentFileCode = new ArrayList<String>();
                for( String line : Files.readAllLines(Paths.get(folderPath + "/" + parentCodeRange.getFilePath()))){
                    parentFileCode.add(line);
                }
                parentFile.put("file", new JSONArray(parentFileCode));
            }

            newFile.put("startLine", String.valueOf(newCodeRange.getStartLine()));
            newFile.put("endLine", String.valueOf(newCodeRange.getEndLine()));
            newFile.put("filePath", newCodeRange.getFilePath());

            // this is redundant, as the right commit is not checked out
            if ( Files.exists(Paths.get(folderPath + "/" + newCodeRange.getFilePath())) ){
                ArrayList<String> newFileCode = new ArrayList<String>();
                for( String line : Files.readAllLines(Paths.get(folderPath + "/" + newCodeRange.getFilePath()))){
                    newFileCode.add(line);
                }
                newFile.put("file", new JSONArray(newFileCode));
            }
            json.put("beforeRefactoring", parentFile);
            json.put("afterRefactoring", newFile);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return json;
    }

    private JSONObject getRefactoringData(Refactoring ref, String refId, String folderPath){
        JSONObject json = new JSONObject();
        if(ref instanceof ExtractOperationRefactoring) { 
            ExtractOperationRefactoring ex = (ExtractOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeExtraction();
            CodeRange newCodeRange = ex.getSourceOperationCodeRangeAfterExtraction();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        else if(ref instanceof InlineOperationRefactoring) { 
            InlineOperationRefactoring ex = (InlineOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getTargetOperationCodeRangeBeforeInline();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterInline();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if(ref instanceof RenameOperationRefactoring) { 
            RenameOperationRefactoring ex = (RenameOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeRename();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterRename();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if(ref instanceof MoveOperationRefactoring) { 
            MoveOperationRefactoring ex = (MoveOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeMove();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterMove();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if(ref instanceof MoveAttributeRefactoring) { 
            MoveAttributeRefactoring ex = (MoveAttributeRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceAttributeCodeRangeBeforeMove();
            CodeRange newCodeRange = ex.getTargetAttributeCodeRangeAfterMove();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        if( ref instanceof AddParameterRefactoring){
            AddParameterRefactoring ex = (AddParameterRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }

        return json;
    }

    private void buildPromptContext(Refactoring ref, String refId, String folderPath){
        JSONObject json = getRefactoringData(ref, refId, folderPath);
        String filePath = "refactoring-data/" + folderPath.substring(4) + "/" + refId + ".json";

        try (FileWriter fileWriter = new FileWriter(filePath)){
            fileWriter.write(json.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateJsonForAllRefactorings(String folderPath){
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

    private static void populateJsonWithFileContent(Path JSONFilePath, String repoFolderPath, Boolean after) {
        try{
            String jsonString = new String(Files.readAllBytes(JSONFilePath));
            JSONObject json = new JSONObject(jsonString);
            String refString = "beforeRefactoring";
            if(after){
                refString = "afterRefactoring";
            }
            if (json.has(refString)) {
                JSONObject refactoringObj = json.getJSONObject(refString);
        
                if (refactoringObj.has("filePath")) {
                    // Get the file content
                    String filePath = refactoringObj.getString("filePath");
                    if( Files.exists(Paths.get(repoFolderPath + "/" + filePath)) ){
                        ArrayList<String> parentFileCode = new ArrayList<String>();
                        for( String line : Files.readAllLines(Paths.get(repoFolderPath + "/" + filePath))){
                            parentFileCode.add(line);
                        }
                        refactoringObj.put("file", new JSONArray(parentFileCode));
                    }

                    // Update the json file
                    json.put(refString, refactoringObj);
                    try (FileWriter fileWriter = new FileWriter(JSONFilePath.toString())){
                        fileWriter.write(json.toString());
            
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Added " + refString + " file content to json");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
    }

    public void populateFileContentOnCommits(String repoFolderPath) {
        String refactoringFolderPath = "refactoring-data/" + repoFolderPath.substring(4);
        try {
            try (Stream<Path> JSONFiles = Files.list(Paths.get(refactoringFolderPath))) {
                                
                JSONFiles.forEach(JSONFilePath -> {
                    String file = JSONFilePath.getFileName().toString();
                    if(file.length() > 5 && file.substring(file.length()-5).equals(".json")){
                        String commitId = file.substring(0, 40);
                        try{ 
                            // Checkout the commit and populate the json with afterReafactoring file content
                            gitService.checkout(this.repo, commitId);
                            populateJsonWithFileContent(JSONFilePath, repoFolderPath, true);
                            
                            // Checkout the parent commit and populate the json with beforeReafactoring file content
                            String parentCommit = getParentCommitId(commitId);
                            gitService.checkout(this.repo, parentCommit);
                            populateJsonWithFileContent(JSONFilePath, repoFolderPath, false);

                            HelperTools.getLLMRefactoring(JSONFilePath.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluateSingleFileRefactorings(String repoFolderPath){
        String refactoringFolderPath = "refactoring-data/" + repoFolderPath.substring(4);
        RepositoryEvaluator evaluator = new RepositoryEvaluator(repoFolderPath);
        try {
            try (Stream<Path> JSONFiles = Files.list(Paths.get(refactoringFolderPath))) {
                                
                JSONFiles.forEach(JSONFilePath -> {
                    String file = JSONFilePath.getFileName().toString();
                    if(file.length() > 5 && file.substring(file.length()-5).equals(".json") && 
                        HelperTools.isSingleFileRefactoring(JSONFilePath.toString())){

                        String changedFilePath = HelperTools.getChangedFilePath(JSONFilePath.toString());
                        String LLMRefactoring = HelperTools.preprocessLLMRefactoring(HelperTools.getFileFromJSON(JSONFilePath.toString(), "LLM_simple"));
                        String beforeRefactoring = HelperTools.getFileFromJSON(JSONFilePath.toString(), "before");
                        String afterRefactoring = HelperTools.getFileFromJSON(JSONFilePath.toString(), "after");
                        if( changedFilePath != null || LLMRefactoring != null || beforeRefactoring != null || afterRefactoring != null){
                            String commitId = file.substring(0, 40);
                            try{ 
                                // Checkout the parent commit and perform the evaluation
                                String parentCommit = getParentCommitId(commitId);
                                HelperTools.checkout(this.repo, this.branch, parentCommit);
                                
                                String baseline = evaluator.evaluateRepository();
                                HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, LLMRefactoring);
                                String LLM = evaluator.evaluateRepository();
                                HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, afterRefactoring);
                                String developer = evaluator.evaluateRepository();
                                HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, beforeRefactoring);

                                if(baseline != null || LLM != null || developer != null){
                                    evaluator.processEvaluationResults(baseline, LLM, developer, JSONFilePath.toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    
                    }
                });
            }

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
