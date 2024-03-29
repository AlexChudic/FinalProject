# Assessing LLMs’ Code Refactoring Skills
### Supervisor: Handan Gul Calikli

### Description:

This project aims to investigate LLMs’ refactoring skills compared to (human) developers through mining GitHub repositories and conducting experiments with novice and experienced developers.
Through mining GitHub repositories, the goal is to understand to what extent zero-shot LLMs can refactor medium-large code bases. Should we guide zero-shot LLMs on which parts of the code to refactor? For instance, can zero-shot LLMs perform refactoring if we give them the code base and the location in the code base to be refactored? How maintainable and readable is the resulting code compared to code refactored by (human) developers? How should one enter the prompt to get more effective results from zero-shot LLMs? Besides working with zero-shot LLMs, how about developing an LLM-based refactoring assistant by improving LLMs’ performance through further training and prompt engineering?
We will conduct experiments with (novice and experienced) developers to gain insights about novice/experienced developers’ interaction with zero-shot LLMs. If the project achieves developing an LLM-based refactoring assistant, we will also conduct experiments to assess the effectiveness and efficiency of the LLM-based refactoring assistant.

##### References:
[1] Martin Fowler, Improving the Design of Existing Code. Addison-Wesley, 2019.

## Dissertation
The link to read the dissertation: https://www.overleaf.com/read/vnkqzdphxfmv#afd5e7

## Setup and Compilation
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


### Running the modules
It is possible to run submodules on its own from the terminal. Make sure these commands are executed from the root repository folder.
- Use `python src/evaluation.py refactoring-data/<repositoryName>` for generating the evaluation metrics for the generated refactoring jsons
- Use `python src/useGPT.py refactoring-data/<refactoringJsonPath>` for prompting the GPT3.5 Turbo about how to refactor the code 
