#!/bin/bash

# Check if a path parameter is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <path> <sonar_token>"
    exit 1
fi

# Assign the provided parameters to a variable
directory="$1"
sonar_token="$2"

# Change to the specified directory

current_directory=$(pwd)
cd "$directory" || exit 1

sonar_scanner_output=$(sonar-scanner \
  -Dsonar.projectKey=RefactorAssessment \
  -Dsonar.sources=. \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=$sonar_token \
  -Dsonar.java.binaries=.
)

analysis_id=$(echo "$sonar_scanner_output" | grep -o 'http://localhost:9000/api/ce/task?id=.*')

echo "ANALYSIS_URL: $analysis_id"
# echo "Sonar Scanner Output: $sonar_scanner_output"