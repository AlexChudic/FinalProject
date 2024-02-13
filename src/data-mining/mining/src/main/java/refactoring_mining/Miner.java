package refactoring_mining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import gr.uom.java.xmi.diff.AddClassAnnotationRefactoring;
import gr.uom.java.xmi.diff.AddParameterRefactoring;
import gr.uom.java.xmi.diff.ChangeClassAccessModifierRefactoring;
import gr.uom.java.xmi.diff.ChangeOperationAccessModifierRefactoring;
import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.ExtractAttributeRefactoring;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import gr.uom.java.xmi.diff.MoveAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveClassRefactoring;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import gr.uom.java.xmi.diff.RenameVariableRefactoring;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    /**
	 * Get the parent commit id of a commit.
	 * @param commitId The child commit id
     * @return The parent commit id
	 */
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

    /**
	 * Get the RevCommit object from a commit id
	 * @param commitId The commit id
     * @return The RevCommit object
	 */
    private RevCommit getCommitById(String commitId) throws IOException {
        return repoWalk.parseCommit(repo.resolve(commitId));
    }

     /**
	 * Indicate commits that should be ignored.
	 * You may override this method to implement custom logic.
	 *  
	 * @param ref The refactoring object.
     * @param refId The refactoring id.
     * @param parentCodeRange The code range of the parent file.
     * @param newCodeRange The code range of the new file.
     * @param folderPath The folder path of the repository.
     * @return The JSON object of the refactoring.
	 */
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

    /**
	 * Find the refactoring type and create a JSON object for the refactoring.
	 * @param ref The refactoring object.
     * @param refId The refactoring id.
     * @param folderPath The folder path of the repository.
     * @return The JSON object of the refactoring.
	 */
    private JSONObject getRefactoringData(Refactoring ref, String refId, String folderPath){
        JSONObject json = new JSONObject();
        if(ref instanceof ExtractOperationRefactoring) { 
            ExtractOperationRefactoring ex = (ExtractOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeExtraction();
            CodeRange newCodeRange = ex.getSourceOperationCodeRangeAfterExtraction();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if(ref instanceof ExtractAttributeRefactoring) { 
            ExtractAttributeRefactoring ex = (ExtractAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
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
        else if(ref instanceof RenameVariableRefactoring) { 
            RenameVariableRefactoring ex = (RenameVariableRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            // TODO try using getInvolvedClassesBeforeRefactoring & getInvolvedClassesAfterRefactoring functions
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
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
        else if( ref instanceof MoveClassRefactoring){
            MoveClassRefactoring ex = (MoveClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if( ref instanceof ChangeVariableTypeRefactoring){
            ChangeVariableTypeRefactoring ex = (ChangeVariableTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if( ref instanceof AddParameterRefactoring){
            AddParameterRefactoring ex = (AddParameterRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if( ref instanceof ChangeClassAccessModifierRefactoring){
            ChangeClassAccessModifierRefactoring ex = (ChangeClassAccessModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if( ref instanceof ChangeOperationAccessModifierRefactoring){
            ChangeOperationAccessModifierRefactoring ex = (ChangeOperationAccessModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if( ref instanceof AddClassAnnotationRefactoring){
            AddClassAnnotationRefactoring ex = (AddClassAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }

        return json;
    }

    /**
	 * Get the data for the refactoring and save it to a JSON file.
	 * @param ref The refactoring object.
     * @param refId The refactoring id.
     * @param folderPath The folder path of the repository.
     * @param onlySingleFile A boolean to indicate if ONLY single file refactorings should be saved.
	 */
    private void buildPromptContext(Refactoring ref, String refId, String folderPath, Boolean onlySingleFile){
        JSONObject json = getRefactoringData(ref, refId, folderPath);
        String folder = "refactoring-data/" + folderPath.substring(4);
        if( !Files.exists(Paths.get(folder))){
            try {
                Files.createDirectories(Paths.get(folder));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if( !onlySingleFile || HelperTools.isSingleFileRefactoring(json)){
            String filePath = "refactoring-data/" + folderPath.substring(4) + "/" + refId + ".json";

            try (FileWriter fileWriter = new FileWriter(filePath)){
                fileWriter.write(json.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateJsonForAllSingleFileRefactorings(String folderPath){
        try {
            miner.detectAll(this.repo, this.branch, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    System.out.println("Refactorings at " + commitId);
                    int id = 0;
                    for (Refactoring ref : refactorings) {
                        System.out.println(ref.toString());
                        String refId = commitId + "-" + id;
                        buildPromptContext(ref, refId, folderPath, true);
                        id++;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Add the file content to the JSON object.
	 * @param JSONFilePath The path of the JSON file.
     * @param repoFolderPath The path of the repository with the codebase.
     * @param after A boolean to indicate if the content of the file after or before the refactoring should be added.
	 */
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

    /**
	 * Add the file content before and after the refactoring to the JSON object and get the LLM refactoring.
     * @param repoFolderPath The path of the repository with the codebase.
	 */
    public void populateFileContentOnCommitsAndGetLLMRefactorings(String repoFolderPath) {
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

                            // Only get the LLM Refactoring for the Single file refactorings
                            if (HelperTools.isSingleFileRefactoring(JSONFilePath.toString())){ 
                                HelperTools.getLLMRefactoring(JSONFilePath.toString());
                            }
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

    /**
	 * Traverse the jsons and get the evaluations for all single file refactorings.
     * @param repoFolderPath The path of the repository with the codebase.
	 */
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
                                
                                String baseline = evaluator.evaluateFile(changedFilePath);
                                HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, LLMRefactoring);
                                String LLM = evaluator.evaluateFile(changedFilePath);
                                HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, afterRefactoring);
                                String developer = evaluator.evaluateFile(changedFilePath);
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

    /**
	 * Set repository of the miner object.
     * @param folder The local path where to clone repo.    
     * @param url The url of the Gitrepository.
	 */
    public void setRepo(String folder, String url) {
        try {
            this.repo = gitService.cloneIfNotExists(folder, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * @return The repository of the miner object.
	 */
    public Repository getRepo() {
        return this.repo;
    }
}
