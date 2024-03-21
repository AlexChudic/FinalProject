package refactoring_mining;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Repository;
import org.json.JSONArray;
import org.json.JSONObject;

public class HelperTools {
    
    public static void replaceFile(String filePath, String newContent){
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(newContent);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String preprocessLLMRefactoring(String LLMRefactoring){
        return LLMRefactoring.substring(8,LLMRefactoring.length()-3);
    }

    public static void checkout(Repository repository, String branch, String commitId) throws Exception {
        System.out.println("Checking out " + repository.getDirectory().getParent().toString() + " " + commitId + " ...");
        try (Git git = new Git(repository)) {
            git.reset()
                .setRef("refs/remotes/origin/" + branch)
                .setMode(ResetType.HARD)
                .call();
            CheckoutCommand checkout = git.checkout().setName(commitId).setForced(true);
            checkout.call();
        }
    }

    public static void getLLMRefactoring(String JSONPath){
        String[] command = {"python", "src/useGPT.py", JSONPath};
        System.out.println("Executing command: " + String.join(" ", command));
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            System.out.println("Process started");
            int exitCode = process.waitFor();
            System.out.println("Process finished");        

            if (exitCode != 0) {
                System.out.println("Python script execution failed with exit code: " + exitCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            } else {
                System.out.println("Python script executed successfully");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("Python script executed successfully");
            }
                    
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getFileFromJSON(String JSONPath, String file){
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(JSONPath)));
            JSONObject json = new JSONObject(jsonString);
            // Get the value of LLMRefactoring.simplePrompt
            if (file == "LLM_simple"){
                if (json.has("LLMRefactoring") && json.getJSONObject("LLMRefactoring").has("simplePrompt")) {
                    return json.getJSONObject("LLMRefactoring").getString("simplePrompt");
                } else {
                    System.out.println(JSONPath+ " LLMRefactoring.simplePrompt not found in the JSON.");
                }
            } else if(file == "before"){
                if (json.has("beforeRefactoring") && json.getJSONObject("beforeRefactoring").has("file")) {
                    return convertArrayOfStringsToSingleString(json.getJSONObject("beforeRefactoring").getJSONArray("file"));
                } else {
                    System.out.println(JSONPath+ " beforeRefactoring.file not found in the JSON.");
                }
            } else if(file == "after"){
                if (json.has("afterRefactoring") && json.getJSONObject("afterRefactoring").has("file")) {
                    return convertArrayOfStringsToSingleString(json.getJSONObject("afterRefactoring").getJSONArray("file"));
                } else {
                    System.out.println(JSONPath+ " afterRefactoring.file not found in the JSON.");
                }
            } else if (file == "isEvaluated"){
                if (json.has("evaluation")) {
                    return "Evaluated";
                } else {
                    return null;
                }
            } else if (file == "simplePromptTooLong"){
                if (json.has("LLMRefactoring") && json.getJSONObject("LLMRefactoring").has("simplePromptTooLong")) {
                    return "simplePromptTooLong";
                } else {
                    return null;
                }
            } else {
                System.out.println("Invalid file type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getChangedFilePath(String JSONPath){
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(JSONPath)));
            JSONObject json = new JSONObject(jsonString);
            return getChangedFilePath(json);
        } catch (Exception e) {
            System.out.println("Error reading JSON file: " + JSONPath);
            e.printStackTrace();
        }
        return null;
    }

    public static String getChangedFilePath(JSONObject json){
        if (json.has("beforeRefactoring") && json.getJSONObject("beforeRefactoring").has("filePath")) {
            return json.getJSONObject("beforeRefactoring").getString("filePath");
        } else {
            System.out.println("Changed file path not found in the JSON.");
        }
        return null;
    }

    public static boolean isSingleFileRefactoring(String JSONPath){
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(JSONPath)));
            JSONObject json = new JSONObject(jsonString);
            // Get the value of before
            if (json.has("beforeRefactoring") && json.getJSONObject("beforeRefactoring").has("filePath") &&
                json.has("afterRefactoring") && json.getJSONObject("afterRefactoring").has("filePath") &&
                json.getJSONObject("beforeRefactoring").getString("filePath").equals(json.getJSONObject("afterRefactoring").getString("filePath"))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error reading JSON file: " + JSONPath);
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSingleFileRefactoring(JSONObject json){
        if (json.has("beforeRefactoring") && json.getJSONObject("beforeRefactoring").has("filePath") &&
            json.has("afterRefactoring") && json.getJSONObject("afterRefactoring").has("filePath") &&
            json.getJSONObject("beforeRefactoring").getString("filePath").equals(json.getJSONObject("afterRefactoring").getString("filePath"))) {
            return true;
        } else {
            return false;
        }
    }

    public static String convertArrayOfStringsToSingleString(JSONArray array){
        String result = "";
        for (int i = 0; i < array.length(); i++) {
            String string = array.getString(i);
            result += string + "\n";
        }
        return result;
    }

    public static long getNumberOfFilesInFolder(String folderPath){
        try (Stream<Path> files = Files.list(Paths.get(folderPath))) {
            return files.count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void createFolder(String folderPath){
        try {
            if(Files.exists(Paths.get(folderPath)) && Files.isDirectory(Paths.get(folderPath))){
                return;
            } else {
                Files.createDirectories(Paths.get(folderPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
