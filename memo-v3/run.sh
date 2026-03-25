#!/bin/bash
# MEMO_V3 - Activity Tracker Launcher
cd "$(dirname "$0")"

case "$1" in
    --test)
        echo "Running integration tests..."
        mvn test -Dtest=IntegrationTest
        ;;
    --all-tests)
        echo "Running all tests..."
        mvn test
        ;;
    *)
        echo "Running MEMO_V3..."
        mvn exec:java
        ;;
esac
