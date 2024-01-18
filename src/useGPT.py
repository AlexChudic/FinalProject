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
        return "Refactor the following class:\n" + file
    

def ask_chatGPT( prompt ):
    client = OpenAI()
    response = client.chat.completions.create(
        model="gpt-3.5-turbo-1106",
        messages=[
        {"role": "system", "content": "You are a code quality analyst. Pay close attention to the readability, code smells, and cognitive complexity. Your goal is to optimize the code without changing the functionality. No explanations are needed."},
        {"role": "user", "content": prompt }
        ]
    )
    print(response.choices[0].message.content)
    time.sleep(20)
    return response.choices[0].message.content


def prompt_refactorings(refactorings, directory_path):
    i = 0
    for ref in refactorings:
        if "beforeRefactoring.file" in ref.columns:
            prompt1 = construct_simple_prompt(join_file(ref["beforeRefactoring.file"][0]))
            LLM_answer = ask_chatGPT(prompt1)
            ref['LLM_simple_output'] = LLM_answer

            if "beforeRefactoring.startLine" in ref.columns and "beforeRefactoring.endLine" in ref.columns:
                prompt2 = construct_simple_prompt(join_file(ref["beforeRefactoring.file"][0]), ref["beforeRefactoring.startLine"][0], ref["beforeRefactoring.endLine"][0])
                LLM_answer = ask_chatGPT(prompt2)
                ref['LLM_simple_output_with_columns'] = LLM_answer

            ref.to_json(f"{directory_path}with-answers/{i}_LLM_results.json", orient='records', lines=True)
            print(f"DataFrame {i} successfully saved!")
            i += 1


if __name__ == "__main__":
    load_dotenv()