import sys
from openai import OpenAI
from dotenv import load_dotenv
import pandas as pd
import os
import json
import time


def join_file(file_lines):
    return '\n'.join(file_lines)


def construct_simple_prompt(file, changes_start=None, changes_end=None):
    if changes_end:
        return "Refactor the following class between the lines " + str(changes_start) + " and " + str(changes_end) + ":\n" + file
    else:
        # Good to show if the LLM can decide on the type of refactoring
        # Can they maintain the same functionality? and code quality
        return "Refactor the following class:\n" + file
    
    # TODO: add prompt which adds the refactoring miner message
    

def ask_chatGPT( prompt ):
    client = OpenAI()
    print("Asking GPT-3...")
    print(prompt)
    response = client.chat.completions.create(
        model="gpt-3.5-turbo-1106",
        messages=[
        {"role": "system", "content": "You are a code quality analyst. Pay close attention to the readability, code smells, and cognitive complexity. Your goal is to optimize the code without changing the functionality. No explanations are needed."},
        {"role": "user", "content": prompt }
        ]
    )
    print(response.choices[0].message.content)
    time.sleep(20) # Wait for 20 seconds to avoid exceeding the API limit
    return response.choices[0].message.content

def JSON_to_dataFrame(JSON_path):
    if os.path.isfile(JSON_path) and JSON_path[-4:] == "json":
        with open(JSON_path) as f:
            data = json.load(f)

        df = pd.json_normalize(data)  # Convert JSON to DataFrame
        return df
    else:
        return None

def get_LLM_refactorings_for_file(JSON_path):
    refactoring = JSON_to_dataFrame(JSON_path)
    if refactoring is not None:
        if "beforeRefactoring.file" in refactoring.columns:
            prompt1 = construct_simple_prompt(join_file(refactoring["beforeRefactoring.file"][0]))
            LLM_answer = ask_chatGPT(prompt1)
            refactoring['LLMRefactoring.simplePrompt'] = LLM_answer

            if "beforeRefactoring.startLine" in refactoring.columns and "beforeRefactoring.endLine" in refactoring.columns:
                prompt2 = construct_simple_prompt(join_file(refactoring["beforeRefactoring.file"][0]), refactoring["beforeRefactoring.startLine"][0], refactoring["beforeRefactoring.endLine"][0])
                LLM_answer = ask_chatGPT(prompt2)
                refactoring['LLMRefactoring.simplePromptWithColumns'] = LLM_answer

            refactoring.to_json(JSON_path, orient='records', lines=True)
            print(f"DataFrame {JSON_path} successfully saved!")
    else:
        print(f"Invalid JSON file: {JSON_path}")

def prompt_directory_refactorings(directory_path):
    for filename in os.listdir(directory_path):
        if filename[-4:] == "json":
            get_LLM_refactorings_for_file(directory_path + "/" + filename)

def handle_terminal_input(**args):
    load_dotenv()
    JSON_path = None
    if len(sys.argv) > 1:
        JSON_path = sys.argv[1]
        print(f"Parameter passed: {JSON_path}")
    else:
        print("No parameter provided.")

if __name__ == "__main__":
    load_dotenv()
    prompt_directory_refactorings("refactoring-data/refactoring-toy-example")
    