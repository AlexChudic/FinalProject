package refactoring_mining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class RepositoryEvaluator {
    private String repositoryPath;
    private String sonarToken;
    private String sonarUser;
    private String sonarPassword;
    private String sonarProjectName;
    private String basicAuth;

    public RepositoryEvaluator(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        String pathToEnvFile = "/Users/alexc/Desktop/UofG/Final_Project/FinalProject/src/.env";
        Dotenv dotenv = Dotenv.configure().directory(pathToEnvFile).load();
        this.sonarToken = dotenv.get("SONAR_TOKEN");
        this.sonarUser = dotenv.get("SONAR_USER");
        this.sonarPassword = dotenv.get("SONAR_PASSWORD");
        this.sonarProjectName = dotenv.get("SONAR_PROJECT_NAME");
        String credentials = sonarUser + ":" + sonarPassword;
        this.basicAuth = "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes()));
    }

    public String evaluateRepository() {
        // Run the Bash script
        String analysisUrl = runBashScript();

        // Make a GET request to SonarQube server
        String sonarQubeUrl = "http://localhost:9000/api/measures/component?component=" + sonarProjectName + "&metricKeys=code_smells%2Cnew_code_smells%2Clines%2Cnew_lines%2Cncloc%2Cbugs%2Cnew_bugs%2Cvulnerabilities%2Cnew_vulnerabilities%2Cnew_maintainability_rating%2Csqale_index%2Ccomplexity%2Ccognitive_complexity%2Ccomment_lines";
        String sonarQubeResult = makeGetRequest(sonarQubeUrl);

        // Print the result
        System.out.println("SonarQube Result:\n" + sonarQubeResult);
        return sonarQubeResult;
    }

    public String evaluateFile(String filePath) {
        // Run the Bash script
        String analysisUrl = runBashScript();

        // Wait for the analysis to finish
        waitForTaskToFinish(analysisUrl);
        
        // Make a GET request to SonarQube server
        String fileUrl = "%3A" + filePath.replace("/", "%2F");
        String sonarQubeUrl = "http://localhost:9000/api/measures/component?component=" + sonarProjectName + fileUrl + "&metricKeys=code_smells%2Cnew_code_smells%2Clines%2Cnew_lines%2Cncloc%2Cbugs%2Cnew_bugs%2Cvulnerabilities%2Cnew_vulnerabilities%2Cnew_maintainability_rating%2Csqale_index%2Ccomplexity%2Ccognitive_complexity%2Ccomment_lines";
        String sonarQubeResult = makeGetRequest(sonarQubeUrl);

        // Print the result
        System.out.println("SonarQube Result:\n" + sonarQubeResult);
        return sonarQubeResult;
        
    }

    private void waitForTaskToFinish(String analysisUrl){
        String analysisTaskDetails = makeGetRequest(analysisUrl);
        JSONObject analysisTaskDetailsJson = new JSONObject(analysisTaskDetails);
        String analysisState = analysisTaskDetailsJson.getJSONObject("task").getString("status");
        while(analysisState.equals("PENDING") || analysisState.equals("IN_PROGRESS")){
            System.out.println("Waiting; Analysis State: " + analysisState);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            analysisTaskDetails = makeGetRequest(analysisUrl);
            analysisTaskDetailsJson = new JSONObject(analysisTaskDetails);
            analysisState = analysisTaskDetailsJson.getJSONObject("task").getString("status");
        }
        System.out.println("Analysis State: " + analysisState);
    }

    public String runBashScript(){
        try { 
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "tmp/evaluate-repository.sh " + repositoryPath + " " + sonarToken);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            String analysisUrl = ""; // get the analysis url from the bash script
            try (InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if( line.contains("ANALYSIS_URL") ){
                        analysisUrl = line.substring(14);
                    }
                    System.out.println(line);
                }
            }

            // Check the exit code
            if (exitCode != 0) {
                throw new RuntimeException("Bash script execution failed with exit code: " + exitCode);
            } else {
                System.out.println("Bash script executed successfully");
                return analysisUrl;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String makeGetRequest(String strUrl) {
        try {
            URI uri = new URI(strUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method and headers
            connection.setRequestProperty("Authorization", basicAuth);          
            connection.setRequestMethod("GET");

            // Get the response
            connection.getResponseCode();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
            
        } catch (URISyntaxException | IOException e) {
            System.out.println("Error making GET request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // LLM refactoring evaluation - baseline eval
    // Developer refactoring eval - baseline eval
    public void processEvaluationResults(String baseline, String LLM, String developer, String JSONFilePath){
        JSONObject baslineJson = new JSONObject(baseline);
        JSONObject LLMJson = new JSONObject(LLM);
        JSONObject developerJson = new JSONObject(developer);

        try {
            // Save the eval values into the json
            String jsonString = new String(Files.readAllBytes(Paths.get(JSONFilePath)));
            JSONObject json = new JSONObject(jsonString);
            JSONObject evaluation = new JSONObject();
            evaluation.put("baseline", baslineJson);
            evaluation.put("LLM", LLMJson);
            evaluation.put("developer", developerJson);
            json.put("evaluation", evaluation);

            try (FileWriter fileWriter = new FileWriter(JSONFilePath)){
                fileWriter.write(json.toString());
                System.out.println("Successfully updated the JSON file with evaluation data.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}