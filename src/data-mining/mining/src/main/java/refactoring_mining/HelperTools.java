package refactoring_mining;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    public static void getLLMRefactoring(String JSONPath){
        // String[] command = {"python3", "src/useGPT.py", JSONPath};
        String[] command = {"/runModel/python", "src/useGPT.py", JSONPath}; // TODO: make this work
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
                    
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            } else {
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

}
