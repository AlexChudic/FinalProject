# User manual 

To be able to run all of the Python scripts and Jupyter notebooks, make sure to download all required packages. This can be done by running this command in the root folder of the repository: `pip install -r requirements.txt`

### Repository sampling
The process of retrieving the population of the repositories and performing the sampling is documented in file [src/get_samples.ipynb](https://github.com/AlexChudic/FinalProject/blob/main/src/get_samples.ipynb)

The list with sampled repositories used for the assessment of the refactoring skill in our evaluation is stored in file [data/evaluate_repositories.json](https://github.com/AlexChudic/FinalProject/blob/main/data/evaluate_repositories.json)

### Sonarqube and SonarScanner setup
1. [Download](https://docs.sonarsource.com/sonarqube/latest/try-out-sonarqube/#installing-a-local-instance-of-sonarqube) and install a local instance of Sonarqube
2. Run Sonarqube in terminal - `<PATH_TO_SONARQUBE>/bin` 
3. Log in, and create a new local project
4. Choose to analyse the project "Locally", generate a token
5. Add the token, project name, and login details to the project's `.env` file
6. [Download](https://docs.sonarsource.com/sonarcloud/advanced-setup/ci-based-analysis/sonarscanner-cli/) and install SonarScanner locally
7. Set the SonarQube envoronment Variable - add these lines to the `~/.zshrc` file
   - `export SONAR_HOME=<PATH_TO_SONNARSCANNER>/{version}/libexec`
   - `export SONAR=$SONAR_HOME/bin export PATH=$SONAR:$PATH`

For Mac, it's possible to use [Homebrew](https://techblost.com/how-to-setup-sonarqube-locally-on-mac/) for easier installation process (steps 1-6 are sufficient)

### Running the pipeline
To run the pipeline, you will need to follow these steps: 
1. Follow the instructions on setting up Sonarqube and SonarScanner above
2. Make sure the correct version of Java is installed.
3. Populate the `src/.env` file with the correct API keys and credentials
4. Now you can run the pipeline by running the main method in [App.java](https://github.com/AlexChudic/FinalProject/blob/main/src/data-mining/mining/src/main/java/refactoring_mining/App.java)
     - The pipeline automatically analyses the repositories in the file [data/evaluate_repositories.json](https://github.com/AlexChudic/FinalProject/blob/main/data/evaluate_repositories.json)


### Running the sub-modules
It is possible to run submodules on its own from the terminal. Make sure these commands are executed from the root repository folde
- Use `python src/evaluation.py` to generate the evaluation metrics for all of the repositories
    - Use `python src/evaluation.py refactoring-data/<repositoryName>` for generating the evaluation metrics for the generated refactoring jsons
- Use `python src/useGPT.py` for prompting all of the generated JSON refactorings using the GPT3.5 Turbo
    - Note: make sure to populate `src/.env` file with your OPENAI API key
    - Optionally use `python src/useGPT.py refactoring-data/<refactoringJsonPath>` for prompting a single refactoring JSON the GPT3.5 Turbo
  
