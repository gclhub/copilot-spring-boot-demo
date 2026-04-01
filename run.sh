#!/bin/bash

# Run script for E-Commerce Monolith Demo
# This script ensures Java 25 is used for running the application

echo "🚀 Starting E-Commerce Monolith..."
echo "=================================="

# Find Java 25 — prefer Homebrew installation, fall back to java_home
JAVA_25_HOME=""

# Check Homebrew OpenJDK 25 (not registered with /usr/libexec/java_home)
if [ -d "/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" ]; then
    _candidate="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
    _ver=$("$_candidate/bin/java" -version 2>&1 | awk -F'"' '/version/{print $2}' | cut -d. -f1)
    [ "$_ver" = "25" ] && JAVA_25_HOME="$_candidate"
fi

# Also check versioned Homebrew path
if [ -z "$JAVA_25_HOME" ] && [ -d "/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home" ]; then
    JAVA_25_HOME="/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home"
fi

# Fall back to java_home (may not find Homebrew JDK)
if [ -z "$JAVA_25_HOME" ]; then
    JAVA_25_HOME=$(/usr/libexec/java_home -v 25 2>/dev/null)
fi

if [ -z "$JAVA_25_HOME" ]; then
    echo "❌ Java 25 not found!"
    echo "Please install Java 25 (e.g., using Homebrew: brew install openjdk)"
    exit 1
fi

echo "✓ Using Java 25: $JAVA_25_HOME"
echo ""
echo "Application will be available at:"
echo "  • REST API: http://localhost:8080/api"
echo "  • H2 Console: http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Run with Java 25
JAVA_HOME=$JAVA_25_HOME mvn spring-boot:run
