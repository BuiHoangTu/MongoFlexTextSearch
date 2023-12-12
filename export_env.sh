#!/bin/bash

# Check if an argument was provided
if [ "$#" -lt 1 ]; then
    echo "No .env file path provided. Using default: .env"
    env_file=".env"
else
    env_file=$1
fi

# Check if the .env file exists
if [ ! -f $env_file ]; then
    echo "File $env_file does not exist."
    return 1
fi

# Load the .env file
export $(grep -v '^#' $env_file | xargs)
