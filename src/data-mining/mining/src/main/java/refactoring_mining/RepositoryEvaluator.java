package refactoring_mining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import io.github.cdimascio.dotenv.Dotenv;

public class RepositoryEvaluator {
    private String repositoryPath;
    private String sonarToken;

    public RepositoryEvaluator(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        String pathToEnvFile = "/Users/alexc/Desktop/UofG/Final_Project/FinalProject/src/.env";
        Dotenv dotenv = Dotenv.configure().directory(pathToEnvFile).load();
        this.sonarToken = dotenv.get("SONAR_TOKEN");;
    }

    public void evaluateRepository() {
        try {
            // Run the Bash script
            runBashScript();

            // Make a GET request to SonarQube server
            String sonarQubeUrl = "http://localhost:9000/api/qualitygates/project_status?projectKey=RefactorAssessment";
            String sonarQubeResult = makeGetRequest(sonarQubeUrl);

            // Print the result
            System.out.println("SonarQube Result:\n" + sonarQubeResult);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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

    private String makeGetRequest(String strUrl) throws IOException {
        try {
            URI uri = new URI(strUrl);
            URL url = uri.toURL(); // TODO: Handle MalformedURLException
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Set SonarQube token header
            connection.setRequestProperty("Authorization", "Bearer " + sonarToken);

            // Get the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } finally {
                connection.disconnect();
            }
        } catch (URISyntaxException e) {
            throw new IOException("Malformed URL: " + strUrl, e);
        }
    }
}