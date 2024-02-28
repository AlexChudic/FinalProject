import os
import json
import sys
import matplotlib.pyplot as plt
from tabulate import tabulate
from dotenv import load_dotenv
from matplotlib.table import Table
import numpy as np


def evaluateCommit( jsonPath, generateLog=False, generateChart=False ):
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

    evalPath = os.path.dirname(jsonPath) + "/eval/"
    if not os.path.exists(evalPath):
        os.makedirs(evalPath)

    evalPath = logPath = os.path.dirname(jsonPath) + "/eval/" + os.path.basename(jsonPath)[:-5]
    if generateChart:
        generateBarChart(table, savePath=evalPath + "-barChart.png")

    if generateLog:
        # Write the data to a log file
        logPath = evalPath + "-eval.txt"
        generateEvalLog(table, logPath, True, data)
        
    else:
        print(tabulate(table, headers=headers, tablefmt="grid"))
    
    return table
    
def evaluateRepository( repositoryPath ):
    aggregate_table = []
    table_count = 0

    evalPath = repositoryPath + "/eval/"
    if not os.path.exists(evalPath):
        os.makedirs(evalPath)

    for filename in os.listdir(repositoryPath):
        if filename[-4:] == "json":
            if hasEvaluationMetrics(repositoryPath + "/" + filename):
                singleRefactoringMetrics = evaluateCommit(repositoryPath + "/" + filename, True, True)
                aggregate_table = addToTable(aggregate_table, singleRefactoringMetrics)
                table_count += 1

    headers = ["Metric", "Developer Refactoring", "LLM Refactoring"]
    average_table = [[item[0], round(item[1]/table_count,2), round(item[2]/table_count,2)] for item in aggregate_table]
    generateEvalLog(average_table, repositoryPath + "/eval/average-eval.txt")
    generateBarChart(average_table, savePath=repositoryPath + "/eval/average-barChart" + os.path.basename(repositoryPath) + ".png")
    print(tabulate(average_table, headers=headers, tablefmt="grid"))

def addToTable(table, singleRefactoringMetrics):
    if len(table) == 0:
        table = singleRefactoringMetrics
    else:
        for i in range(len(table)):
            table[i][1] += singleRefactoringMetrics[i][1]
            table[i][2] += singleRefactoringMetrics[i][2]
    return table 

def generateEvalLog(table, logPath, saveRefactorings=False, data=None):
    with open(logPath, "w") as log_file:
        log_file.write(tabulate(table, headers=["Metric", "Developer Refactoring", "LLM Refactoring"], tablefmt="grid"))
        log_file.write("\n\n")

        if saveRefactorings:
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

def generateBarChart(data, savePath=None):
    metrics = [item[0] for item in data]
    values1 = [round(item[1],2) for item in data]
    values2 = [round(item[2],2) for item in data]

    x = range(len(metrics))
    width = 0.35

    fig, ax = plt.subplots()
    bar1 = ax.bar(x, values1, width, label='LLM Refactoring')
    bar2 = ax.bar([i + width for i in x], values2, width, label='Developer Refactoring')

    # Add labels, title, and legend
    ax.set_ylabel('Difference in eval metrics')
    ax.set_title('Change in eval metrics - before and after refactoring')
    ax.set_xticks([i + width / 2 for i in x])
    ax.set_xticklabels(metrics, rotation=45, ha='right')
    ax.legend()

    plt.tight_layout()
    if savePath:
        plt.savefig(savePath)
        print("Bar chart has been saved to:", savePath)
    else:
        plt.show()

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
        evaluateCommit("refactoring-data/refactoring-toy-example/a5a7f852e45c7cadc8d1524bd4d14a1e39785aa5-0.json", True, True)