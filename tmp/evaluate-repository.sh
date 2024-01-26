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
echo "Current Working Directory: $current_directory"
cd "$directory" || exit 1

sonar-scanner \
  -Dsonar.projectKey=RefactorAssessment \
  -Dsonar.sources=. \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=$sonar_token \
  -Dsonar.java.binaries=.