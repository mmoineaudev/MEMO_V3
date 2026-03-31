#!/bin/bash

# Memo V3 - Launcher Script
# This script builds and runs the Memo time tracking application

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "Memo V3 - Time Tracking Application"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

echo "Building application..."
mvn clean compile

echo ""
echo "Running tests..."
mvn test

if [ $? -eq 0 ]; then
    echo ""
    echo "All tests passed! Starting application..."
    echo ""
    
    # Run the application
    mvn exec:java -Dexec.mainClass="com.memo.ui.MemoFrame"
else
    echo ""
    echo "Tests failed. Please fix the errors before running the application."
    exit 1
fi