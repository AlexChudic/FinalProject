import sys
from openai import OpenAI
from dotenv import load_dotenv
import pandas as pd
import os
import json
import time
import tiktoken

def join_file(file_lines):
    return '\n'.join(file_lines)

def construct_simple_prompt(file, type="simple"):
    msg = []
    if type == "simple":
        msg = [
            {"role": "system", "content": "You are a code quality analyst. Pay close attention to the maintainability, code smells, and complexity. Your goal is to optimize the code without changing the functionality. No explanations are needed."},
            {"role": "user", "content": "Refactor the following class:\n" + file }
        ]
    elif type == "getTypeOfRefactoringPrompt":
        msg = [
            {"role": "system", "content": "You are a code quality analyst. Pay close attention to the maintainability, code smells, and complexity. Your goal is to decide if refactoring is required and if so, what type of refactoring is needed. No explanations are needed."},
            {"role": "user", "content": "Does this class require refactoring? Only answer 'YES, refactoring required=<<<REFACTORING_TYPES>>>' or 'NO'. For the answer 'YES' replace the <<<REFACTORING_TYPES>>> with the refactoring types required separated by a semi-colon. No explanation is needed.:\n\n" + file },
        ]
    elif type == "getTypeOfRefactoringPromptVersion2":
        msg = [
            {"role": "system", "content": "You are a code quality analyst. Pay close attention to the maintainability, code smells, and complexity. Your goal is to decide if refactoring is required and if so, what type of refactoring is needed. No explanations are needed. Only consider these refactoring types: Extract Method; Inline Method; Rename Method; Move Method; Move Attribute; Pull Up Method; Pull Up Attribute; Push Down Method; Push Down Attribute; Extract Superclass; Extract Interface; Move Class; Rename Class; Extract and Move Method; Rename Package; Move and Rename Class; Extract Class; Extract Subclass; Extract Variable; Inline Variable; Parameterize Variable; Rename Variable; Rename Parameter; Rename Attribute; Move and Rename Attribute; Replace Variable with Attribute; Replace Attribute ; Merge Variable; Merge Parameter; Merge Attribute; Split Variable; Split Parameter; Split Attribute; Change Variable Type; Change Parameter Type; Change Return Type; Change Attribute Type; Extract Attribute; Move and Rename Method; Move and Inline Method; Add Method Annotation; Remove Method Annotation; Modify Method Annotation; Add Attribute Annotation; Remove Attribute Annotation; Modify Attribute Annotation; Add Class Annotation; Remove Class Annotation; Modify Class Annotation; Add Parameter Annotation; Remove Parameter Annotation; Modify Parameter Annotation; Add Variable Annotation; Remove Variable Annotation; Modify Variable Annotation; Add Parameter; Remove Parameter; Reorder Parameter; Add Thrown Exception Type; Remove Thrown Exception Type; Change Thrown Exception Type; Change Method Access Modifier; Change Attribute Access Modifier; Encapsulate Attribute; Parameterize Attribute; Replace Attribute with Variable; Add Method Modifier; Remove Method Modifier; Add Attribute Modifier; Remove Attribute Modifier; Add Variable Modifier; Add Parameter Modifier; Remove Variable Modifier; Remove Parameter Modifier; Change Class Access Modifier; Add Class Modifier; Remove Class Modifier; Move Package; Split Package; Merge Package; Localize Parameter; Change Type Declaration Kind; Collapse Hierarchy; Replace Loop with Pipeline; Replace Anonymous with Lambda; Merge Class; Inline Attribute; Replace Pipeline with Loop; Split Class; Split Conditional; Invert Condition; Merge Conditional; Merge Catch; Merge Method; Split Method; Move Code; Replace Anonymous with Class; Parameterize Test; Assert Throws"},
            {"role": "user", "content": "Does this class require refactoring? Only answer 'YES, refactoring required=<<<REFACTORING_TYPES>>>' or 'NO'. For the answer 'YES' replace the <<<REFACTORING_TYPES>>> with the refactoring types required separated by a semi-colon. No explanation is needed.:\n\n" + file },
        ]
    return msg
    
def try_prompting(prompt, args, count=0):
    try:
        return ask_chatGPT(prompt, args)
    except Exception as e:
        print(f"Error: {e}")
        print(f"Waiting 10 seconds before trying again... {count}'th attempt.")
        time.sleep(10)
        return try_prompting(prompt, args, count+1) if count <= 6 else None

def ask_chatGPT( msg, args ):
    client = OpenAI()
    print("Asking GPT-3...")
    #print(prompt)

    # get the number of tokens in the message
    num_tokens = num_tokens_in_string(" ".join(item["content"] for item in msg))
    print(f"Number of tokens in the message: {num_tokens}")

    # return none if the number of tokens in the message exceeds the limit of 16370
    if( num_tokens > 16370 ):
        print("Error: The number of tokens in the message exceeds the limit of 16370.")
        return None
    
    # only run the request if the number of remaning tokens is at least 2.5 times the number of tokens in the message
    if args["TPM_available"] < num_tokens*2.5:
        print("Waiting for the token limit to reset...")
        time.sleep(args["TPM_resetTime"])
    
    elif args["RPM_available"] <= 0:
        print("Waiting for the request limit to reset...")
        time.sleep(args["RPM_resetTime"])

    raw_response = client.chat.completions.with_raw_response.create(
        model="gpt-3.5-turbo-0125",
        messages=msg,
        temperature=0.2, # use 0.2 for more consistent answers
    )
    response = raw_response.parse()

    # update the number of remaining tokens and requests
    if( raw_response.status_code == 200 ):
        args["TPM_available"] = int(raw_response.headers['x-ratelimit-remaining-tokens'])
        args["RPM_available"] = int(raw_response.headers['x-ratelimit-remaining-requests'])
        if(raw_response.headers['x-ratelimit-reset-tokens'][-2:]== "ms"):
            args["TPM_resetTime"] = float(raw_response.headers['x-ratelimit-reset-tokens'][:-2])
        else:
            args["TPM_resetTime"] = float(raw_response.headers['x-ratelimit-reset-tokens'][:-1])
        if(raw_response.headers['x-ratelimit-reset-requests'][-2:]== "ms"):
            args["RPM_resetTime"] = float(raw_response.headers['x-ratelimit-reset-requests'][:-2])
        else:
            args["RPM_resetTime"] = float(raw_response.headers['x-ratelimit-reset-requests'][:-1])
    else:
        print(f"Error: {response.error.message}")
        print(response)

    # print(response.choices[0].message.content)
    print(f"Tokens used in the request and answer: {response.usage.total_tokens}; Remaining tokens: {args['TPM_available']}; Reset time: {args['TPM_resetTime']}; Remaining requests: {args['RPM_available']}; Reset time: {args['RPM_resetTime']}")
    return response.choices[0].message.content

def num_tokens_in_string(string) -> int:
    encoding = tiktoken.encoding_for_model("gpt-3.5-turbo")
    num_tokens = len(encoding.encode(string))
    return num_tokens

def JSON_to_dataFrame(JSON_path):
    try :
        if os.path.isfile(JSON_path) and JSON_path[-4:] == "json":
            with open(JSON_path) as f:
                data = json.load(f)
            return data
        else:
            return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def add_new_LLM_refactorings_to_dataFrame(JSON_path, args=None):
    start_time = time.time()
    refactoring = JSON_to_dataFrame(JSON_path)
    if args is None:
        args = init_args()
    if refactoring is not None and isSingleFileRefactoring(refactoring):
        if 'LLMRefactoring' in refactoring:
            if 'typeOfRefactoringNew' in refactoring['LLMRefactoring']:
                print(f"New LLM Refactoring already generated for {JSON_path}.")
                return
            elif "beforeRefactoring" in refactoring and "file" in refactoring["beforeRefactoring"]:
                print(f"NEW: Processing the json file using gpt-3.5-turbo-0125: {JSON_path} ")
                prompt = construct_simple_prompt(join_file(refactoring["beforeRefactoring"]["file"]), "getTypeOfRefactoringPromptVersion2")
                LLM_answer = try_prompting(prompt, args)
                refactoring['LLMRefactoring']['typeOfRefactoringNew'] = LLM_answer
                with open(JSON_path, 'w') as json_file:
                    json.dump(refactoring, json_file, indent=4)
                print(f"DataFrame {JSON_path} successfully saved!")
        else:
            print(f"LLMRefactoring does not exist in JSON file: {JSON_path}")
    else:
        print(f"Invalid JSON file: {JSON_path}")
    print(f"Time taken to process single json: {round(time.time() - start_time, 2)} seconds.")
            

def get_LLM_refactorings_for_file(JSON_path, args=None):
    start_time = time.time()
    refactoring = JSON_to_dataFrame(JSON_path)
    if args is None:
        args = init_args()
    if refactoring is not None and isSingleFileRefactoring(refactoring):
        if 'LLMRefactoringGenerated' in refactoring and refactoring['LLMRefactoringGenerated'] == True:
            print(f"LLM Refactoring already generated for {JSON_path}.")
            return
        elif "beforeRefactoring" in refactoring and "file" in refactoring["beforeRefactoring"]:
            print(f"Processing the json file using gpt-3.5-turbo-0125: {JSON_path} ")
            prompt0 = construct_simple_prompt(join_file(refactoring["beforeRefactoring"]["file"]), "getTypeOfRefactoringPrompt")
            LLM_answer = try_prompting(prompt0, args)
            refactoring['LLMRefactoring'] = {}
            refactoring['LLMRefactoring']['typeOfRefactoring'] = LLM_answer
            
            prompt1 = construct_simple_prompt(join_file(refactoring["beforeRefactoring"]["file"]))
            LLM_answer = try_prompting(prompt1, args)
            refactoring['LLMRefactoring']['simplePrompt'] = LLM_answer

            refactoring['LLMRefactoringGenerated'] = True
            with open(JSON_path, 'w') as json_file:
                json.dump(refactoring, json_file, indent=4)
            print(f"DataFrame {JSON_path} successfully saved!")
    else:
        print(f"Invalid JSON file: {JSON_path}")
    print(f"Time taken to process single json: {round(time.time() - start_time, 2)} seconds.")

def prompt_directory_refactorings(directory_path):
    start_time = time.time()
    args = init_args()
    for filename in os.listdir(directory_path):
        if filename[-4:] == "json":
            file_path = directory_path + "/" + filename
            #get_LLM_refactorings_for_file(file_path, args)
            add_new_LLM_refactorings_to_dataFrame(file_path, args)
    print(f"Time taken to process directory: {round(time.time() - start_time, 2)} seconds.")

def handle_terminal_input(**args):
    load_dotenv()
    JSON_path = None
    if len(sys.argv) > 1:
        JSON_path = sys.argv[1]
        print(f"Parameter passed: {JSON_path}")
    else:
        print("No parameter provided.")

def isSingleFileRefactoring(refactoring):
    if "beforeRefactoring" in refactoring and "filePath" in refactoring["beforeRefactoring"] and "afterRefactoring" in refactoring and "filePath" in refactoring["afterRefactoring"]:
        if refactoring["beforeRefactoring"]["filePath"] == refactoring["afterRefactoring"]["filePath"]:
            return True
    return False

def init_args():
    args = {
        "TPM_available" : int(os.environ.get('OPENAI_TOKEN_LIMIT_PER_MINUTE')),
        "RPM_available" : int(os.environ.get('OPENAI_REQUEST_LIMIT_PER_MINUTE')),
    }
    return args

if __name__ == "__main__":
    load_dotenv(override=True)
    if len(sys.argv) > 1:
        JSON_path = sys.argv[1]
        if os.path.isdir(JSON_path):
            prompt_directory_refactorings(JSON_path)
        else:
            get_LLM_refactorings_for_file(JSON_path)
    else:
        for repoName in os.listdir("refactoring-data"):
            repo_path = "refactoring-data/" + repoName
            if os.path.isdir(repo_path):
                print("PROMPTING DIRECTORY: " + repo_path )
                prompt_directory_refactorings(repo_path)
    