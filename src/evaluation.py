import os
import json
import sys
from tabulate import tabulate
from dotenv import load_dotenv


def evaluateCommit( jsonPath, generateLog=False ):
    with open(jsonPath) as f:
        data = json.load(f)
    
    developer_measures = data["evaluation"]["developer"]["component"]["measures"]
    baseline_measures = data["evaluation"]["baseline"]["component"]["measures"]
    LLM_measures = data["evaluation"]["LLM"]["component"]["measures"]
    
    # Compute evaluation metrics for developer_refactoring and LLM_refactoring
    baseline = {measure["metric"]: int(measure["value"]) for measure in baseline_measures if "value" in measure }
    developer_metrics = {measure["metric"]: int(measure["value"]) for measure in developer_measures if "value" in measure}
    developer_refactoring = {metric: developer_metrics[metric] - baseline.get(metric, 0) for metric in developer_metrics }
    LLM_metrics = {measure["metric"]: int(measure["value"]) for measure in LLM_measures if "value" in measure}
    LLM_refactoring = {metric: LLM_metrics[metric] - baseline.get(metric, 0) for metric in LLM_metrics}

    # Display metrics in table format
    headers = ["Metric", "Developer Refactoring", "LLM Refactoring"]
    table = []
    for metric in developer_refactoring:
        table.append([metric, developer_refactoring[metric], LLM_refactoring[metric]])
    
    table.sort(key=lambda x: x[0])
    if generateLog:
        # Write the data to a log file
        logPath = os.path.dirname(jsonPath) + "/eval/" + os.path.basename(jsonPath)[:-5] + "-eval.txt"
        with open(logPath, "w") as log_file:
            log_file.write(tabulate(table, headers=headers, tablefmt="grid"))
            log_file.write("\n\n")

            log_file.write("CODE BEFORE REFACTORING:\n")
            log_file.write("\n".join(data["beforeRefactoring"]["file"]))
            log_file.write("\n\n")

            log_file.write("CODE AFTER DEVELOPER REFACTORING:\n")
            log_file.write("\n".join(data["afterRefactoring"]["file"]))
            log_file.write("\n\n")

            log_file.write("CODE AFTER LLM REFACTORING:\n")
            ref = data["LLMRefactoring"]["simplePrompt"]
            log_file.write(ref[8: len(ref)-3])
        print("Evaluation results have been written to:", logPath)
    else:
        print(tabulate(table, headers=headers, tablefmt="grid"))
    

def evaluateRepository( repositoryPath ):
    for filename in os.listdir(repositoryPath):
        if filename[-4:] == "json":
            if hasEvaluationMetrics(repositoryPath + "/" + filename):
                evaluateCommit(repositoryPath + "/" + filename, True)


def hasEvaluationMetrics(jsonPath):
    with open(jsonPath) as f:
        data = json.load(f)
    return True if "evaluation" in data else False
        

if __name__ == "__main__":
    load_dotenv(override=True)
    if len(sys.argv) > 1:
        JSON_path = sys.argv[1]
        evaluateRepository(JSON_path)
    else:
        evaluateCommit("refactoring-data/refactoring-toy-example/f35b2c8eb8c320f173237e44d04eefb4634649a2-0.json", True)