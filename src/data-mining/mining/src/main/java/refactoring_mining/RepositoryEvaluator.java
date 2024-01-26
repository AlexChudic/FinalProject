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

import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class RepositoryEvaluator {
    private String repositoryPath;
    private String sonarToken;
    private String sonarUser;
    private String sonarPassword;
    private String sonarProjectName;

    public RepositoryEvaluator(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        String pathToEnvFile = "/Users/alexc/Desktop/UofG/Final_Project/FinalProject/src/.env";
        Dotenv dotenv = Dotenv.configure().directory(pathToEnvFile).load();
        this.sonarToken = dotenv.get("SONAR_TOKEN");
        this.sonarUser = dotenv.get("SONAR_USER");
        this.sonarPassword = dotenv.get("SONAR_PASSWORD");
        this.sonarProjectName = dotenv.get("SONAR_PROJECT_NAME");
    }

    public String evaluateRepository() {
        try {
            // Run the Bash script
            runBashScript();

            // Make a GET request to SonarQube server
            String sonarQubeUrl = "http://localhost:9000/api/measures/component?component=" + sonarProjectName + "&metricKeys=code_smells%2Cnew_code_smells%2Clines%2Cnew_lines%2Cbugs%2Cnew_bugs%2Cvulnerabilities%2Cnew_vulnerabilities%2Cnew_maintainability_rating%2Csqale_index%2Ccomplexity%2Ccognitive_complexity%2Ccomment_lines";
            String sonarQubeResult = makeGetRequest(sonarQubeUrl);

            // Print the result
            System.out.println("SonarQube Result:\n" + sonarQubeResult);
            return sonarQubeResult;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void runBashScript() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "tmp/evaluate-repository.sh " + repositoryPath + " " + sonarToken);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        try (InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Check the exit code
        if (exitCode != 0) {
            throw new RuntimeException("Bash script execution failed with exit code: " + exitCode);
        }
    }

    public String makeGetRequest(String strUrl) {
        try {
            URI uri = new URI(strUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method and headers
            String credentials = sonarUser + ":" + sonarPassword;
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes()));
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