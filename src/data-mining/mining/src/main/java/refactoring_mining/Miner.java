package refactoring_mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import gr.uom.java.xmi.diff.AddAttributeAnnotationRefactoring;
import gr.uom.java.xmi.diff.AddAttributeModifierRefactoring;
import gr.uom.java.xmi.diff.AddClassAnnotationRefactoring;
import gr.uom.java.xmi.diff.AddClassModifierRefactoring;
import gr.uom.java.xmi.diff.AddMethodAnnotationRefactoring;
import gr.uom.java.xmi.diff.AddMethodModifierRefactoring;
import gr.uom.java.xmi.diff.AddParameterRefactoring;
import gr.uom.java.xmi.diff.AddThrownExceptionTypeRefactoring;
import gr.uom.java.xmi.diff.AddVariableAnnotationRefactoring;
import gr.uom.java.xmi.diff.AddVariableModifierRefactoring;
import gr.uom.java.xmi.diff.AssertThrowsRefactoring;
import gr.uom.java.xmi.diff.ChangeAttributeAccessModifierRefactoring;
import gr.uom.java.xmi.diff.ChangeAttributeTypeRefactoring;
import gr.uom.java.xmi.diff.ChangeClassAccessModifierRefactoring;
import gr.uom.java.xmi.diff.ChangeOperationAccessModifierRefactoring;
import gr.uom.java.xmi.diff.ChangeReturnTypeRefactoring;
import gr.uom.java.xmi.diff.ChangeThrownExceptionTypeRefactoring;
import gr.uom.java.xmi.diff.ChangeTypeDeclarationKindRefactoring;
import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.CollapseHierarchyRefactoring;
import gr.uom.java.xmi.diff.EncapsulateAttributeRefactoring;
import gr.uom.java.xmi.diff.ExtractAttributeRefactoring;
import gr.uom.java.xmi.diff.ExtractClassRefactoring;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.ExtractSuperclassRefactoring;
import gr.uom.java.xmi.diff.ExtractVariableRefactoring;
import gr.uom.java.xmi.diff.InlineAttributeRefactoring;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import gr.uom.java.xmi.diff.InlineVariableRefactoring;
import gr.uom.java.xmi.diff.InvertConditionRefactoring;
import gr.uom.java.xmi.diff.MergeAttributeRefactoring;
import gr.uom.java.xmi.diff.MergeCatchRefactoring;
import gr.uom.java.xmi.diff.MergeClassRefactoring;
import gr.uom.java.xmi.diff.MergeConditionalRefactoring;
import gr.uom.java.xmi.diff.MergeOperationRefactoring;
import gr.uom.java.xmi.diff.MergePackageRefactoring;
import gr.uom.java.xmi.diff.MergeVariableRefactoring;
import gr.uom.java.xmi.diff.ModifyAttributeAnnotationRefactoring;
import gr.uom.java.xmi.diff.ModifyClassAnnotationRefactoring;
import gr.uom.java.xmi.diff.ModifyMethodAnnotationRefactoring;
import gr.uom.java.xmi.diff.ModifyVariableAnnotationRefactoring;
import gr.uom.java.xmi.diff.MoveAndRenameAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveAndRenameClassRefactoring;
import gr.uom.java.xmi.diff.MoveAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveClassRefactoring;
import gr.uom.java.xmi.diff.MoveCodeRefactoring;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.ParameterizeTestRefactoring;
import gr.uom.java.xmi.diff.PullUpOperationRefactoring;
import gr.uom.java.xmi.diff.RemoveAttributeAnnotationRefactoring;
import gr.uom.java.xmi.diff.RemoveAttributeModifierRefactoring;
import gr.uom.java.xmi.diff.RemoveClassAnnotationRefactoring;
import gr.uom.java.xmi.diff.RemoveClassModifierRefactoring;
import gr.uom.java.xmi.diff.RemoveMethodAnnotationRefactoring;
import gr.uom.java.xmi.diff.RemoveMethodModifierRefactoring;
import gr.uom.java.xmi.diff.RemoveParameterRefactoring;
import gr.uom.java.xmi.diff.RemoveThrownExceptionTypeRefactoring;
import gr.uom.java.xmi.diff.RemoveVariableAnnotationRefactoring;
import gr.uom.java.xmi.diff.RemoveVariableModifierRefactoring;
import gr.uom.java.xmi.diff.RenameAttributeRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import gr.uom.java.xmi.diff.RenamePackageRefactoring;
import gr.uom.java.xmi.diff.RenameVariableRefactoring;
import gr.uom.java.xmi.diff.ReorderParameterRefactoring;
import gr.uom.java.xmi.diff.ReplaceAnonymousWithClassRefactoring;
import gr.uom.java.xmi.diff.ReplaceAnonymousWithLambdaRefactoring;
import gr.uom.java.xmi.diff.ReplaceAttributeRefactoring;
import gr.uom.java.xmi.diff.ReplaceLoopWithPipelineRefactoring;
import gr.uom.java.xmi.diff.ReplacePipelineWithLoopRefactoring;
import gr.uom.java.xmi.diff.SplitAttributeRefactoring;
import gr.uom.java.xmi.diff.SplitClassRefactoring;
import gr.uom.java.xmi.diff.SplitConditionalRefactoring;
import gr.uom.java.xmi.diff.SplitOperationRefactoring;
import gr.uom.java.xmi.diff.SplitPackageRefactoring;
import gr.uom.java.xmi.diff.SplitVariableRefactoring;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Parameter;
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
        } else if(ref instanceof InlineOperationRefactoring) { 
            InlineOperationRefactoring ex = (InlineOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getTargetOperationCodeRangeBeforeInline();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterInline();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof RenameOperationRefactoring) { 
            RenameOperationRefactoring ex = (RenameOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeRename();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterRename();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof MoveOperationRefactoring) { 
            MoveOperationRefactoring ex = (MoveOperationRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceOperationCodeRangeBeforeMove();
            CodeRange newCodeRange = ex.getTargetOperationCodeRangeAfterMove();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof MoveAttributeRefactoring) { 
            MoveAttributeRefactoring ex = (MoveAttributeRefactoring) ref;
            CodeRange parentCodeRange = ex.getSourceAttributeCodeRangeBeforeMove();
            CodeRange newCodeRange = ex.getTargetAttributeCodeRangeAfterMove();

            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // PullUpOperationRefactoring - does not perform single file refactoring (only rightSide method available)
        // PullUpAttributeRefactoring - does not perform single file refactoring (only rightSide method available)
        // PushDownOperationRefactoring - does not perform single file refactoring (only rightSide method available)
        // PushDownAttributeRefactoring - does not perform single file refactoring (only rightSide method available)
        else if(ref instanceof ExtractSuperclassRefactoring) { 
            ExtractSuperclassRefactoring ex = (ExtractSuperclassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            System.out.println("Left coderange size = " + left.size());
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        // ExtractInterfaceRefactoring - not found in the library
        else if( ref instanceof MoveClassRefactoring){
            MoveClassRefactoring ex = (MoveClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof RenameClassRefactoring) { 
            RenameClassRefactoring ex = (RenameClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        // ExtractAndMoveClassRefactoring - not found in the library
        else if(ref instanceof RenamePackageRefactoring) { 
            RenamePackageRefactoring ex = (RenamePackageRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof MoveAndRenameClassRefactoring) { 
            MoveAndRenameClassRefactoring ex = (MoveAndRenameClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if (ref instanceof ExtractClassRefactoring){
            ExtractClassRefactoring ex = (ExtractClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // ExtractSubclassRefactoring - not found in the library
        else if ( ref instanceof ExtractVariableRefactoring){
            ExtractVariableRefactoring ex = (ExtractVariableRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof InlineVariableRefactoring) { 
            InlineVariableRefactoring ex = (InlineVariableRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // ParameterizeVariableRefactoring - not found in the library
        else if(ref instanceof RenameVariableRefactoring) { 
            RenameVariableRefactoring ex = (RenameVariableRefactoring) ref;
            // TODO try using getInvolvedClassesBeforeRefactoring & getInvolvedClassesAfterRefactoring functions
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // RenameParameterRefactoring - not found in the library
        else if(ref instanceof RenameAttributeRefactoring) { 
            RenameAttributeRefactoring ex = (RenameAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof MoveAndRenameAttributeRefactoring) { 
            MoveAndRenameAttributeRefactoring ex = (MoveAndRenameAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // ReplaceVariableWithAttributeRefactoring - not found in the library
        else if(ref instanceof ReplaceAttributeRefactoring) { 
            ReplaceAttributeRefactoring ex = (ReplaceAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof MergeVariableRefactoring) { 
            MergeVariableRefactoring ex = (MergeVariableRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // MergeParameterRefactoring - not found in the library
        else if(ref instanceof MergeOperationRefactoring) {
            MergeOperationRefactoring ex = (MergeOperationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof MergeAttributeRefactoring) { 
            MergeAttributeRefactoring ex = (MergeAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof SplitVariableRefactoring) { 
            SplitVariableRefactoring ex = (SplitVariableRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // SplitParameterRefactoring - not found in the library
        else if(ref instanceof SplitOperationRefactoring) { 
            SplitOperationRefactoring ex = (SplitOperationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof SplitAttributeRefactoring) { 
            SplitAttributeRefactoring ex = (SplitAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof ChangeVariableTypeRefactoring) { 
            ChangeVariableTypeRefactoring ex = (ChangeVariableTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // ChangeParameterTypeRefactoring - not found in the library
        else if(ref instanceof ChangeReturnTypeRefactoring) { 
            ChangeReturnTypeRefactoring ex = (ChangeReturnTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof ChangeAttributeTypeRefactoring) { 
            ChangeAttributeTypeRefactoring ex = (ChangeAttributeTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof ExtractAttributeRefactoring) { 
            ExtractAttributeRefactoring ex = (ExtractAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // MoveAndRenameOperation - not found in the library
        // MoveAndInlineOperation - not found in the library
        else if(ref instanceof AddMethodAnnotationRefactoring) { 
            AddMethodAnnotationRefactoring ex = (AddMethodAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        }
        else if(ref instanceof RemoveMethodAnnotationRefactoring) { 
            RemoveMethodAnnotationRefactoring ex = (RemoveMethodAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof ModifyMethodAnnotationRefactoring) { 
            ModifyMethodAnnotationRefactoring ex = (ModifyMethodAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if(ref instanceof AddAttributeAnnotationRefactoring) { 
            AddAttributeAnnotationRefactoring ex = (AddAttributeAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveAttributeAnnotationRefactoring){
            RemoveAttributeAnnotationRefactoring ex = (RemoveAttributeAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ModifyAttributeAnnotationRefactoring){
            ModifyAttributeAnnotationRefactoring ex = (ModifyAttributeAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AddClassAnnotationRefactoring){
            AddClassAnnotationRefactoring ex = (AddClassAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveClassAnnotationRefactoring){
            RemoveClassAnnotationRefactoring ex = (RemoveClassAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ModifyClassAnnotationRefactoring){
            ModifyClassAnnotationRefactoring ex = (ModifyClassAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // AddParameterAnnotationRefactoring - not found in the library
        // RemoveParameterAnnotationRefactoring - not found in the library
        // ModifyParameterAnnotationRefactoring - not found in the library
        else if( ref instanceof AddVariableAnnotationRefactoring){
            AddVariableAnnotationRefactoring ex = (AddVariableAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveVariableAnnotationRefactoring){
            RemoveVariableAnnotationRefactoring ex = (RemoveVariableAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ModifyVariableAnnotationRefactoring){
            ModifyVariableAnnotationRefactoring ex = (ModifyVariableAnnotationRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AddParameterRefactoring){
            AddParameterRefactoring ex = (AddParameterRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveParameterRefactoring){
            RemoveParameterRefactoring ex = (RemoveParameterRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ReorderParameterRefactoring){
            ReorderParameterRefactoring ex = (ReorderParameterRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AddThrownExceptionTypeRefactoring){
            AddThrownExceptionTypeRefactoring ex = (AddThrownExceptionTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveThrownExceptionTypeRefactoring){
            RemoveThrownExceptionTypeRefactoring ex = (RemoveThrownExceptionTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ChangeThrownExceptionTypeRefactoring){
            ChangeThrownExceptionTypeRefactoring ex = (ChangeThrownExceptionTypeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ChangeOperationAccessModifierRefactoring){
            ChangeOperationAccessModifierRefactoring ex = (ChangeOperationAccessModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ChangeAttributeAccessModifierRefactoring){
            ChangeAttributeAccessModifierRefactoring ex = (ChangeAttributeAccessModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof EncapsulateAttributeRefactoring){
            EncapsulateAttributeRefactoring ex = (EncapsulateAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // ParameterizeAttributeRefactoring - not found in the library
        // ReplaceAttributeWithVariableRefactoring - not found in the library
        else if( ref instanceof AddMethodModifierRefactoring){
            AddMethodModifierRefactoring ex = (AddMethodModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveMethodModifierRefactoring){
            RemoveMethodModifierRefactoring ex = (RemoveMethodModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AddAttributeModifierRefactoring){
            AddAttributeModifierRefactoring ex = (AddAttributeModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveAttributeModifierRefactoring){
            RemoveAttributeModifierRefactoring ex = (RemoveAttributeModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AddVariableModifierRefactoring){
            AddVariableModifierRefactoring ex = (AddVariableModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // RemoveParameterModifierRefactoring - not found in the library
        // AddParameterModifierRefactoring - not found in the library
        else if( ref instanceof RemoveVariableModifierRefactoring){
            RemoveVariableModifierRefactoring ex = (RemoveVariableModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ChangeClassAccessModifierRefactoring){
            ChangeClassAccessModifierRefactoring ex = (ChangeClassAccessModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AddClassModifierRefactoring){
            AddClassModifierRefactoring ex = (AddClassModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof RemoveClassModifierRefactoring){
            RemoveClassModifierRefactoring ex = (RemoveClassModifierRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // MovePackageRefactoring - not found in the library
        else if( ref instanceof SplitPackageRefactoring){
            SplitPackageRefactoring ex = (SplitPackageRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof MergePackageRefactoring){
            MergePackageRefactoring ex = (MergePackageRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // LocalizeVariableRefactoring - not found in the library
        else if( ref instanceof ChangeTypeDeclarationKindRefactoring){
            ChangeTypeDeclarationKindRefactoring ex = (ChangeTypeDeclarationKindRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof CollapseHierarchyRefactoring){
            CollapseHierarchyRefactoring ex = (CollapseHierarchyRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ReplaceLoopWithPipelineRefactoring){
            ReplaceLoopWithPipelineRefactoring ex = (ReplaceLoopWithPipelineRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ReplaceAnonymousWithLambdaRefactoring){
            ReplaceAnonymousWithLambdaRefactoring ex = (ReplaceAnonymousWithLambdaRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof MergeClassRefactoring){
            MergeClassRefactoring ex = (MergeClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof InlineAttributeRefactoring){
            InlineAttributeRefactoring ex = (InlineAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ReplacePipelineWithLoopRefactoring){
            InlineAttributeRefactoring ex = (InlineAttributeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof SplitClassRefactoring){
            SplitClassRefactoring ex = (SplitClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof SplitConditionalRefactoring){
            SplitConditionalRefactoring ex = (SplitConditionalRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof InvertConditionRefactoring){
            InvertConditionRefactoring ex = (InvertConditionRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof MergeConditionalRefactoring){
            MergeConditionalRefactoring ex = (MergeConditionalRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof MergeCatchRefactoring){
            MergeCatchRefactoring ex = (MergeCatchRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } 
        // MergeMethodRefactoring - not found in the library
        // SplitMethodRefactoring - not found in the library
        else if( ref instanceof MoveCodeRefactoring){
            MoveCodeRefactoring ex = (MoveCodeRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0);
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0);
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ReplaceAnonymousWithClassRefactoring){
            ReplaceAnonymousWithClassRefactoring ex = (ReplaceAnonymousWithClassRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0);
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0);
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof ParameterizeTestRefactoring){
            ParameterizeTestRefactoring ex = (ParameterizeTestRefactoring) ref;
            List<CodeRange> left = ex.leftSide(); 
            CodeRange parentCodeRange = left.get(0); // There might be multiple codeRanges!!!
            List<CodeRange> right = ex.rightSide();
            CodeRange newCodeRange = right.get(0); // There might be multiple codeRanges!!!
            json = createJSONForRefactoring(ref, refId, parentCodeRange, newCodeRange, folderPath);
        } else if( ref instanceof AssertThrowsRefactoring){
            AssertThrowsRefactoring ex = (AssertThrowsRefactoring) ref;
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
    private Boolean buildPromptContext(Refactoring ref, String refId, String folderPath, Map<String,Integer> filePaths, Boolean onlySingleFile, int maxRefactorings){
        JSONObject json = getRefactoringData(ref, refId, folderPath);
        String folder = "refactoring-data/" + folderPath.substring(4);
        if( !Files.exists(Paths.get(folder))){
            try {
                Files.createDirectories(Paths.get(folder));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Get the number of refactoring jsons in the folder
        long refactoringCount = HelperTools.getNumberOfFilesInFolder(folder);
        if( refactoringCount >= maxRefactorings){
            System.out.println("Reached the maximum number of refactorings for " + folderPath);
            return false;
        }

        // Check if the file refactoring json is not already saved for this commit
        String changedFilePath = HelperTools.getChangedFilePath(json);
        if(filePaths.containsKey(changedFilePath)){
            System.out.println("Refactoring for " + changedFilePath + " already saved for this commit");
            return false;
        }

        if( !onlySingleFile || HelperTools.isSingleFileRefactoring(json)){
            String filePath = "refactoring-data/" + folderPath.substring(4) + "/" + refId + ".json";

            try (FileWriter fileWriter = new FileWriter(filePath)){
                fileWriter.write(json.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            filePaths.put(changedFilePath, 1);
            return true;
        }
        return false;
    }

    public void generateJsonForAllSingleFileRefactorings(String folderPath){
        try {
            miner.detectAll(this.repo, this.branch, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    System.out.println("Refactorings at " + commitId);
                    int id = 0;
                    Map<String, Integer> filePaths = new HashMap<>();
                    for (Refactoring ref : refactorings) {
                        System.out.println(ref.toString());
                        String refId = commitId + "-" + id;
                        buildPromptContext(ref, refId, folderPath, filePaths, true, 100);
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
    public void populateJsonsWithFileContentOnCommits(String repoFolderPath) {
        System.out.println("Populating JSONs with file content in the folder " + repoFolderPath);
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
                            
                            // Checkout the parent commit and populate the json with beforeRefactoring file content
                            String parentCommit = getParentCommitId(commitId);
                            gitService.checkout(this.repo, parentCommit);
                            populateJsonWithFileContent(JSONFilePath, repoFolderPath, false);

                            // // Only get the LLM Refactoring for the Single file refactorings
                            // if (HelperTools.isSingleFileRefactoring(JSONFilePath.toString())){ 
                            //     HelperTools.getLLMRefactoring(JSONFilePath.toString());
                            // }
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
     * Run the LLM prompts script on all the single file refactorings in the folder.
     * The python script will decide which refactorings are performed in a single file.
     */
    public void getLLMRefactorings(String folderPath){
        System.out.println("Getting LLM Refactorings for the folder " + folderPath);
        String refactoringFolderPath = "refactoring-data/" + folderPath.substring(4);
        HelperTools.getLLMRefactoring(refactoringFolderPath);
    }

    /**
	 * Traverse the jsons and get the evaluations for all single file refactorings.
     * @param repoFolderPath The path of the repository with the codebase.
	 */
    public void evaluateSingleFileRefactorings(String repoFolderPath){
        System.out.println("Evaluating single file refactorings in the folder " + repoFolderPath);
        String refactoringFolderPath = "refactoring-data/" + repoFolderPath.substring(4);
        RepositoryEvaluator evaluator = new RepositoryEvaluator(repoFolderPath);
        
        try (Stream<Path> JSONFiles = Files.list(Paths.get(refactoringFolderPath))) {   
            JSONFiles.forEach(JSONFilePath -> {
                try { 
                    String file = JSONFilePath.getFileName().toString();
                    if(file.length() > 5 && file.substring(file.length()-5).equals(".json") && 
                        HelperTools.isSingleFileRefactoring(JSONFilePath.toString())){

                        String changedFilePath = HelperTools.getChangedFilePath(JSONFilePath.toString());
                        String LLMRefactoring = HelperTools.preprocessLLMRefactoring(HelperTools.getFileFromJSON(JSONFilePath.toString(), "LLM_simple"));
                        String beforeRefactoring = HelperTools.getFileFromJSON(JSONFilePath.toString(), "before");
                        String afterRefactoring = HelperTools.getFileFromJSON(JSONFilePath.toString(), "after");
                        String evaluation = HelperTools.getFileFromJSON(JSONFilePath.toString(), "isEvaluated");

                        // Only evaluate the refactorings that have not been evaluated and has all required data
                        if( evaluation == null && ( changedFilePath != null || LLMRefactoring != null || beforeRefactoring != null || afterRefactoring != null ) ){
                            String commitId = file.substring(0, 40);
                            
                            // Checkout the parent commit and perform the evaluation
                            String parentCommit = getParentCommitId(commitId);
                            HelperTools.checkout(this.repo, this.branch, parentCommit);
                            String baseline = evaluator.evaluateFile(changedFilePath);
                            
                            // Evaluate the file using the LLM refactoring
                            HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, LLMRefactoring);
                            String LLM = evaluator.evaluateFile(changedFilePath);

                            // Evaluate the file using the developer refactoring
                            HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, afterRefactoring);
                            String developer = evaluator.evaluateFile(changedFilePath);

                            // Replace the file with the original content
                            HelperTools.replaceFile(repoFolderPath+"/"+changedFilePath, beforeRefactoring);

                            // Update the json file with the evaluation results
                            if(baseline != null || LLM != null || developer != null){
                                evaluator.processEvaluationResults(baseline, LLM, developer, JSONFilePath.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error during evaluating json " + JSONFilePath.toString());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("Error getting jsons in repository " + repoFolderPath);
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
