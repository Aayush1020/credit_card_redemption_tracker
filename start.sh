#!/bin/bash

echo "Starting Credit Card Redemption Tracker Application..."

# --- Stop any existing backend process on port 8080 ---
echo "Checking for and stopping any existing backend processes on port 8080..."
PID=$(lsof -t -i:8080)

if [ -n "$PID" ]; then
    echo "Found process on port 8080 with PID: $PID. Killing it..."
    kill -9 $PID
    sleep 2 # Give it a moment to terminate
    echo "Previous backend process stopped."
else
    echo "No existing process found on port 8080."
fi

# --- Start Backend ---
echo "Building and starting backend..."
cd backend/cc-redemption-backend

# Ensure backend is built (optional, but good for first run or after changes)
./mvnw clean install

# Start backend in the background
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "Backend starting with PID: $BACKEND_PID"

# Wait a moment for the backend to fully start
# You might need to adjust this sleep duration depending on your system's performance
echo "Waiting for backend to fully initialize (15 seconds)..."
sleep 15

cd .. # Go back to the root directory
cd .. # Go back to the project root directory

# --- Start Frontend ---
echo "Starting frontend..."
cd frontend/cc-redemption-frontend

# Install frontend dependencies (optional, but good for first run or after package.json changes)
npm install

# Start frontend
npm start

# After the frontend starts, the script will likely wait for it to be stopped.
# If you want to stop the backend separately, you'll need to find its PID (e.g., using `lsof -i :8080` or `ps aux | grep java`) and kill it.

echo "Application startup complete. Frontend should be open in your browser."
