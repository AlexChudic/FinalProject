## Meeting 1 - 19.9.2023
### Minutes
* Should think about
  * What LLM to use 
  * Which repo to use
  * What are the determining metrics for assessing refactorings (suggestion: SonarQube, M. Fowler book)
  * How to do prompt engineering
  
### Project Workflow
1. Select LLM
2. Understand their training data
3. Setup a baseline on how to evaluate code refactoring
4. Mine GitHub repos to see the developer code refactorings


## Meeting 2 - 29.9.2023
### Minutes
* Talked about some interesting papers for research - https://arxiv.org/pdf/2305.04764.pdf
* Should look at the codebase or related papers
* Setup Zotero and share GitHub repo


## Meeting 3 - 13.10.2023
### Minutes
* Discussion about LLMs - decided to use CodeLlama as ChatGPT 3.5 is paid
* Next we discussed the overlap between the LLM training dataset and the evaluation set of the research.
  * It shouldn't overlap as it would decrease the value of the research
  * Should research how it's done in related literature (e.g. https://arxiv.org/abs/2305.01210 )
* Talked about refactorings
  * Refactoring Miner - useful tool, should play around with it
  * It is advised to first start with the simple refactorings and make sure the pipeline works and later build on that
  * Think about the type of refactorings I want to test first
    * For refactorings that affect functionality outside the scope the call graph might be useful
    * https://github.com/gousiosg/java-callgraph
  * Look at the book by M.Folwer as it might provide some guidance

###  Next steps
  1. Think about how to curate the dataset, which consists of open-source software projects
     *  Which open-source repo to use? (GitLab, GitHub, BitBucket)
     *  Play with RefactoringMiner, and see how it works
  2. Think about how one should give input the code into Llma so that Llma can refactor it 
