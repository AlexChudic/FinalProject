import os
import json
import sys
import matplotlib.pyplot as plt
from Levenshtein import distance
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
    
    pretty_evaluation_metrics = {
        'vulnerabilities': "Vulnerabilities", 
        'sqale_index': "Technical Debt Index", 
        'bugs': "Bugs", 
        'comment_lines': "Comment Lines", 
        'cognitive_complexity': "Cognitive Complexity",
        'code_smells': "Code Smells",
        'complexity': "Cyclomatic Complexity",
        'lines': "Lines of Code",
        'ncloc': "Non Comment Lines of Code",
        'levenstein_distance': 'Levenshtein Distance',
    }

    # Compute evaluation metrics for developer_refactoring and LLM_refactoring
    baseline = {measure["metric"]: int(measure["value"]) for measure in baseline_measures if "value" in measure }
    developer_metrics = {measure["metric"]: int(measure["value"]) for measure in developer_measures if "value" in measure}
    developer_refactoring = {metric: developer_metrics[metric] - baseline.get(metric, 0) for metric in developer_metrics }
    LLM_metrics = {measure["metric"]: int(measure["value"]) for measure in LLM_measures if "value" in measure}
    LLM_refactoring = {metric: LLM_metrics[metric] - baseline.get(metric, 0) for metric in LLM_metrics}

    # Compute the edit distance between the code before and after refactoring
    beforeRefactoring = "\n".join(data["beforeRefactoring"]["file"])
    developerRefactoring = "\n".join(data["afterRefactoring"]["file"])
    LLMRefactoring = data["LLMRefactoring"]["simplePrompt"]

    baseline["levenstein_distance"] = 0
    developer_metrics["levenstein_distance"] = distance(beforeRefactoring, developerRefactoring)
    developer_refactoring["levenstein_distance"] = distance(beforeRefactoring, developerRefactoring)
    LLM_metrics["levenstein_distance"] = distance(beforeRefactoring, LLMRefactoring)
    LLM_refactoring["levenstein_distance"] = distance(beforeRefactoring, LLMRefactoring)

    # Display metrics in table format
    headers = ["Metric", "Baseline", "Developer Ref", "DevRef Change", "LLM Ref", "LLMRef Change"]
    table = []
    for metric in developer_refactoring:
        if metric in baseline and metric in developer_metrics and metric in developer_refactoring and metric in LLM_metrics and metric in LLM_refactoring:
            table.append([pretty_evaluation_metrics[metric], baseline[metric], developer_metrics[metric], developer_refactoring[metric], LLM_metrics[metric], LLM_refactoring[metric]])
        else:
            print(f"Metric {metric} not found in all measures.")
            return None
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
        #print(tabulate(table, headers=headers, tablefmt="grid"))
        pass
    
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
                singleRefactoringMetrics = evaluateCommit(repositoryPath + "/" + filename, True)
                if singleRefactoringMetrics:
                    aggregate_table = addToTable(aggregate_table, singleRefactoringMetrics)
                    table_count += 1

    headers = ["Metric", "Average Developer Ref Change", "Average LLM Ref Change"]
    average_table = [[item[0], round(item[1]/table_count,2), round(item[2]/table_count,2)] for item in aggregate_table]
    generateEvalLog(average_table, repositoryPath + "/eval/average-eval.txt")
    generateBarChart(average_table, savePath=repositoryPath + "/eval/average-barChart" + os.path.basename(repositoryPath) + ".png")
    print(tabulate(average_table, headers=headers, tablefmt="grid"))

def getRepositoryEvaluationMetrics( repositoryPath ):
    aggregate_table = []
    table_count = 0

    evalPath = repositoryPath + "/eval/"
    if not os.path.exists(evalPath):
        os.makedirs(evalPath)

    for filename in os.listdir(repositoryPath):
        if filename[-4:] == "json":
            if hasEvaluationMetrics(repositoryPath + "/" + filename):
                singleRefactoringMetrics = evaluateCommit(repositoryPath + "/" + filename, False, False)
                if singleRefactoringMetrics:
                    aggregate_table = addToTable(aggregate_table, singleRefactoringMetrics)
                    table_count += 1

    return aggregate_table, table_count

def addToTable(table, singleRefactoringMetrics):
    if len(table) == 0:
        table = singleRefactoringMetrics
    else:
        for i in range(len(table)):
            table[i][1] += singleRefactoringMetrics[i][3]
            table[i][2] += singleRefactoringMetrics[i][5]
    return table 

def generateRQ1Log(data, path=None):
    if path is None:
        path = "data/evaluation/RQ1-log.txt"
    with open(path, "w") as log_file:
        log_file.write(f"Number of refactorings: {data['numberOfRefactorings']}\n")
        log_file.write(f"Number of YES answers: {data['yesCount']}\n\n")
        
        # log_file.write("Developer Refactoring Types:\n")
        # headers = ["Developer Refactoring Type", "Performed", "Correctly Proposed"]
        # table = [ [key, value[0], value[1]] for key, value in data["refTypesPerformed"].items() ]
        # log_file.write(tabulate(table, headers=headers, tablefmt="grid"))
        # log_file.write("\n\n")

        # log_file.write("LLM Refactoring Types:\n")
        # headers = ["LLM Refactoring Type", "Proposed", "Correctly Proposed"]
        # table = [ [key, value[0], value[1]] for key, value in data["refTypesProposed"].items() ]
        # log_file.write(tabulate(table, headers=headers, tablefmt="grid"))     

        log_file.write("\nConcise combined table:\n")
        headers = ["Developer Refactoring Type", "Performed", "Correctly Proposed", "Total Proposed"]
        table = [ [key, value[0], value[1]] for key, value in data["refTypesPerformed"].items()]
        # populate with number of times the refactoring type was proposed by LLM
        for key, value in data["refTypesProposed"].items():
            for table_entry in table:
                if table_entry[0] == key:
                    table_entry.append(value[0])

        # add refactoring types in RM, which has not been performed at all
        RMRefTypes = ["Extract Method", "Inline Method", "Rename Method", "Move Method", "Move Attribute", "Pull Up Method", "Pull Up Attribute", "Push Down Method", "Push Down Attribute", "Extract Superclass", "Extract Interface", "Move Class", "Rename Class", "Extract And Move Method", "Rename Package", "Move and Rename Class", "Extract Class", "Extract Subclass", "Extract Variable", "Inline Variable", "Parameterize Variable", "Rename Variable", "Rename Parameter", "Rename Attribute", "Move And Rename Attribute", "Replace Variable With Attribute", "Replace Attribute ", "Merge Variable", "Merge Parameter", "Merge Attribute", "Split Variable", "Split Parameter", "Split Attribute", "Change Variable Type", "Change Parameter Type", "Change Return Type", "Change Attribute Type", "Extract Attribute", "Move And Rename Method", "Move And Inline Method", "Add Method Annotation", "Remove Method Annotation", "Modify Method Annotation", "Add Attribute Annotation", "Remove Attribute Annotation", "Modify Attribute Annotation", "Add Class Annotation", "Remove Class Annotation", "Modify Class Annotation", "Add Parameter Annotation", "Remove Parameter Annotation", "Modify Parameter Annotation", "Add Variable Annotation", "Remove Variable Annotation", "Modify Variable Annotation", "Add Parameter", "Remove Parameter", "Reorder Parameter", "Add Thrown Exception Type", "Remove Thrown Exception Type", "Change Thrown Exception Type", "Change Method Access Modifier", "Change Attribute Access Modifier", "Encapsulate Attribute", "Parameterize Attribute", "Replace Attribute With Variable", "Add Method Modifier", "Remove Method Modifier", "Add Attribute Modifier", "Remove Attribute Modifier", "Add Variable Modifier", "Add Parameter Modifier", "Remove Variable Modifier", "Remove Parameter Modifier", "Change Class Access Modifier", "Add Class Modifier", "Remove Class Modifier", "Move Package", "Split Package", "Merge Package", "Localize Parameter", "Change Type Declaration Kind", "Collapse Hierarchy", "Replace Loop With Pipeline", "Replace Anonymous With Lambda", "Merge Class", "Inline Attribute", "Replace Pipeline With Loop", "Split Class", "Split Conditional", "Invert Condition", "Merge Conditional", "Merge Catch", "Merge Method", "Split Method", "Move Code", "Replace Anonymous With Class", "Parameterize Test", "Assert Throws"]
        for refType in RMRefTypes:
            if refType not in data["refTypesPerformed"]:
                table.append([refType, 0, 0, data["refTypesProposed"].get(refType, [0])[0]])

        total = sum([table_entry[1] for table_entry in table])
        concise_table = []
        otherSumPerformed = 0
        otherSumCorrectlyProposed = 0
        otherSumTotalProposed = 0
        for i in range(len(table)):
            if len(table[i]) == 3:
                    table[i].append(0)
            if table[i][1]/total <= 0.016:
                otherSumPerformed += table[i][1]
                otherSumCorrectlyProposed += table[i][2]
                otherSumTotalProposed += table[i][3]
            else:
                concise_table.append(table[i])
        concise_table.sort(key=lambda x: x[1], reverse=True)
        concise_table.append(["Other", otherSumPerformed, otherSumCorrectlyProposed, otherSumTotalProposed])
        log_file.write(tabulate(concise_table, headers=headers, tablefmt="grid"))

        log_file.write("\nFull combined table:\n")
        table.sort(key=lambda x: x[1], reverse=True)
        log_file.write(tabulate(table, headers=headers, tablefmt="grid"))

        # check what proportion of proposed refactoring types are not in RM
        notInRMSum = 0
        notInRM = []
        inRMSum = 0
        for key, value in data["refTypesProposed"].items():
            if key not in RMRefTypes:
                notInRM.append(key)
                notInRMSum += value[0]
            else:
                inRMSum += value[0]

        correctlyProposed = sum([table_entry[2] for table_entry in table])
        log_file.write(f"\n\nTotal number of refactorings: {total}\n")
        log_file.write(f"Total number of correctly proposed refactorings: {correctlyProposed}\n")

        log_file.write(f"\n\nTotal proposed refactoring: {notInRMSum + inRMSum}\n")
        log_file.write(f"Total proposed refactoring types not in RM: {notInRMSum}\n")
        log_file.write(f"Total proposed refactoring types in RM: {inRMSum}\n")
        log_file.write(f"Proportion of proposed refactoring types not in RM: {round(notInRMSum/(notInRMSum+inRMSum), 2)}\n")
        log_file.write(f"Refactoring types not in RM: {notInRM}\n")

    print(f"RQ1 results have been written to: {path}")

def generatePieChartOfRefTypes(data, savePath=None):
    sorted_data = sorted(data.items(), key=lambda x: x[1][0], reverse=True)
    labels = []
    sizes = []
    otherSum = 0
    for key, value in sorted_data:
        if value[0]/2045 > 0.016:
            labels.append(key)
            sizes.append(value[0])
        else:
            otherSum += value[0]
    labels.append("Other")
    sizes.append(otherSum)

    colorMap = plt.cm.tab20
    plt.pie(sizes, labels=labels, autopct='%1.1f%%', colors=colorMap.colors , startangle=140)
    #plt.axis('equal')

    if savePath:
        plt.savefig(savePath)
        print("Pie chart has been saved to:", savePath)
    else:
        plt.show()

def generateEvalLog(table, logPath, saveRefactorings=False, data=None):
    with open(logPath, "w") as log_file:
        log_file.write(tabulate(table, headers=["Metric", "Baseline", "Developer Ref", "DevRef Change", "LLM Ref", "LLMRef Change"], tablefmt="grid"))
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


def hasGeneratedLLMResponse(jsonPath):
    try:
        with open(jsonPath) as f:
            data = json.load(f)
        if "LLMRefactoring" in data and "LLMRefactoringGenerated" in data:
            return True
        else:
            return False
    except Exception as e:
        print(f"Error reading file: {jsonPath}")
        print(f"Error: {e}")
        return False


def hasEvaluationMetrics(jsonPath):
    try:
        with open(jsonPath) as f:
            data = json.load(f)
        if "evaluation" in data and "developer" in data["evaluation"] and "LLM" in data["evaluation"] and "baseline" in data["evaluation"]:
            return True
        else:
            return False
    except Exception as e:
        print(f"Error reading file: {jsonPath}")
        print(f"Error: {e}")
        return False


def getProposedRefactoringTypes(LLMAnswer):
    if(LLMAnswer.find("=") == -1):
        return []
    listOfTypes = LLMAnswer.split("=")[1]
    refactoringTypes = listOfTypes[:-1].split(";") if listOfTypes[-1] == ";" else listOfTypes.split(";")
    
    for index, refType in enumerate(refactoringTypes):
        if refType.find("_") != -1:
            ref = refType.replace("_", " ")
            refactoringTypes[index] = ref.title().strip()
        else:
            refactoringTypes[index] = refType.strip().title()

    return list(set(refactoringTypes))


def evaluateRefactoringTypeDetectionForFile(jsonPath, response="typeOfRefactoring"):
    out = {
        "numberOfRefactorings" : 0,
        "yesCount" : 0,
        "refTypesPerformed" : {},
        "refTypesProposed" : {}
    }

    with open(jsonPath) as f:
        data = json.load(f)
        if response not in data["LLMRefactoring"]:
            return out
        developerRefType = data["description"].split("\t")[0]
        LLMAnswer = data["LLMRefactoring"][response]
        if LLMAnswer is None:
            return out
        refPerformed = LLMAnswer.split(",")[0]
        proposedRefTypes = getProposedRefactoringTypes(LLMAnswer)

        if len(proposedRefTypes) == 0:
            print ("No refactoring types proposed by LLM in file:", jsonPath)

        out["numberOfRefactorings"] += 1
        if refPerformed == "YES":
            out["yesCount"] += 1 # count the number of yes answers
            
            if developerRefType not in out["refTypesPerformed"]:
                out["refTypesPerformed"][developerRefType] = [0,0]
            
            # count the number of times the refactoring was performed by the developer
            out["refTypesPerformed"][developerRefType][0] += 1

            # count the number of times the performed refactoring was proposed by the LLM
            if developerRefType in proposedRefTypes:
                out["refTypesPerformed"][developerRefType][1] += 1 


            for refType in proposedRefTypes:
                if refType not in out["refTypesProposed"]:
                    out["refTypesProposed"][refType] = [0,0]
                
                # count the number of times the refactoring was proposed by the LLM
                out["refTypesProposed"][refType][0] += 1

                # count the number of times the proposed refactoring was performed by the developer
                if refType == developerRefType:
                    out["refTypesProposed"][refType][1] += 1

    return out
    

def evaluateRefactoringTypeDetection(repositoryPath, response="typeOfRefactoring"):
    res = {
        "numberOfRefactorings" : 0,
        "yesCount" : 0,
        "refTypesPerformed" : {},
        "refTypesProposed" : {}
    }

    for filename in os.listdir(repositoryPath):
        if filename[-4:] == "json":
            if not hasGeneratedLLMResponse(repositoryPath + "/" + filename):
                continue
            file_res = evaluateRefactoringTypeDetectionForFile(repositoryPath + "/" + filename, response)

            # add the file results to the repository results
            res["numberOfRefactorings"] += file_res["numberOfRefactorings"]
            res["yesCount"] += file_res["yesCount"]
            for key, value in file_res["refTypesPerformed"].items():
                if key not in res["refTypesPerformed"]:
                    res["refTypesPerformed"][key] = [0,0]
                res["refTypesPerformed"][key][0] += value[0]
                res["refTypesPerformed"][key][1] += value[1]
            for key, value in file_res["refTypesProposed"].items():
                if key not in res["refTypesProposed"]:
                    res["refTypesProposed"][key] = [0,0]
                res["refTypesProposed"][key][0] += value[0]
                res["refTypesProposed"][key][1] += value[1]

    # print("Number of refactorings:", res["numberOfRefactorings"])
    # print("Number of YES answers:", res["yesCount"])

    # headers = ["Developer Refactoring Type", "Performed", "Correctly Proposed"]
    # table = [ [key, value[0], value[1]] for key, value in res["refTypesPerformed"].items() ]
    # print(tabulate(table, headers=headers, tablefmt="grid"))

    # headers = ["LLM Refactoring Type", "Proposed", "Correctly Proposed"]
    # table = [ [key, value[0], value[1]] for key, value in res["refTypesProposed"].items() ]
    # print(tabulate(table, headers=headers, tablefmt="grid"))
    return res



def evaluateAllRepositories():
    eval_table = []
    entry_count = 0
    refTypeDetectionResults = {
        "numberOfRefactorings" : 0,
        "yesCount" : 0,
        "refTypesPerformed" : {},
        "refTypesProposed" : {}
    }
    refTypeDetectionResultsNew = {
        "numberOfRefactorings" : 0,
        "yesCount" : 0,
        "refTypesPerformed" : {},
        "refTypesProposed" : {}
    }

    for filename in os.listdir("refactoring-data/"):
        if os.path.isdir("refactoring-data/" + filename):
            print("Evaluating repository:", filename)
            # RQ 1: Evaluate the detection of refactoring requirements and the refactoring type
            refTypeRes = evaluateRefactoringTypeDetection("refactoring-data/" + filename)

            # add the repository results to the global results
            refTypeDetectionResults["numberOfRefactorings"] += refTypeRes["numberOfRefactorings"]
            refTypeDetectionResults["yesCount"] += refTypeRes["yesCount"]
            for key, value in refTypeRes["refTypesPerformed"].items():
                if key not in refTypeDetectionResults["refTypesPerformed"]:
                    refTypeDetectionResults["refTypesPerformed"][key] = [0,0]
                refTypeDetectionResults["refTypesPerformed"][key][0] += value[0]
                refTypeDetectionResults["refTypesPerformed"][key][1] += value[1]
            for key, value in refTypeRes["refTypesProposed"].items():
                if key not in refTypeDetectionResults["refTypesProposed"]:
                    refTypeDetectionResults["refTypesProposed"][key] = [0,0]
                refTypeDetectionResults["refTypesProposed"][key][0] += value[0]
                refTypeDetectionResults["refTypesProposed"][key][1] += value[1]

            # RQ 1.2: Evaluate the detection of refactoring requirements and the refactoring type
            refTypeRes = evaluateRefactoringTypeDetection("refactoring-data/" + filename, "typeOfRefactoringNew")

            # add the repository results to the global results
            refTypeDetectionResultsNew["numberOfRefactorings"] += refTypeRes["numberOfRefactorings"]
            refTypeDetectionResultsNew["yesCount"] += refTypeRes["yesCount"]
            for key, value in refTypeRes["refTypesPerformed"].items():
                if key not in refTypeDetectionResultsNew["refTypesPerformed"]:
                    refTypeDetectionResultsNew["refTypesPerformed"][key] = [0,0]
                refTypeDetectionResultsNew["refTypesPerformed"][key][0] += value[0]
                refTypeDetectionResultsNew["refTypesPerformed"][key][1] += value[1]
            for key, value in refTypeRes["refTypesProposed"].items():
                if key not in refTypeDetectionResultsNew["refTypesProposed"]:
                    refTypeDetectionResultsNew["refTypesProposed"][key] = [0,0]
                refTypeDetectionResultsNew["refTypesProposed"][key][0] += value[0]
                refTypeDetectionResultsNew["refTypesProposed"][key][1] += value[1]


            # RQ 2: Evaluate the code quality metrics
            aggregate_table, ref_count = getRepositoryEvaluationMetrics("refactoring-data/" + filename)
            if ref_count > 0:
                eval_table = addToTable(eval_table, aggregate_table)
            entry_count += ref_count

    print("RQ 1: Refactoring Type Detection")
    print("Number of refactorings:", refTypeDetectionResults["numberOfRefactorings"])
    print("Number of YES answers:", refTypeDetectionResults["yesCount"])
    generateRQ1Log(refTypeDetectionResults)


    print("RQ 1.2: Refactoring Type Detection NEW")
    print("Number of refactorings:", refTypeDetectionResultsNew["numberOfRefactorings"])
    print("Number of YES answers:", refTypeDetectionResultsNew["yesCount"])
    generateRQ1Log(refTypeDetectionResultsNew, "data/evaluation/RQ1-log-new.txt")

    generatePieChartOfRefTypes(refTypeDetectionResultsNew["refTypesPerformed"], "data/evaluation/developerRefTypePieChart.png")

    print("RQ 2: Code Quality Metrics")
    headers = ["Metric", "Average Developer Ref Change", "Average LLM Ref Change"]
    average_table = [[item[0], round(item[1]/entry_count,2), round(item[2]/entry_count,2)] for item in aggregate_table]
    print(f"Number of entries: {entry_count}")
    generateEvalLog(average_table, "data/evaluation/average-eval.txt")
    generateBarChart(average_table, savePath="data/evaluation/average-barChart.png")
    print(tabulate(average_table, headers=headers, tablefmt="grid"))


if __name__ == "__main__":
    load_dotenv(override=True)
    if len(sys.argv) > 1:
        JSON_path = sys.argv[1]
        evaluateRepository(JSON_path)
    else:
        evaluateAllRepositories()
        #evaluateRefactoringTypeDetection("refactoring-data/dingdong-helper")