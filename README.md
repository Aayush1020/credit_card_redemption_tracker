# Credit Card Redemption Tracker

This is a local web application designed to help you track credit card reward redemptions. It features a React frontend and a Spring Boot (Java) backend, with data persisted locally in a JSON file.

## Features

-   **Add Credit Cards:** Easily add new credit cards with a name and an optional description.
-   **Add Reward Redemptions:** For each credit card, you can add multiple reward redemptions.
-   **Redemption Details:** Each redemption includes:
    -   A name (string)
    -   An amount (double)
    -   A frequency (MONTHLY, QUARTERLY, BIANNUAL, YEARLY)
    -   A checkbox to mark it as completed.
-   **Check Off Redemptions:** Mark redemptions as complete when you've done them.
-   **Auto-Reset Behavior:** Redemptions automatically reset (uncheck) based on their configured frequency. This logic runs **both every time the backend application starts up** (to catch up on any missed resets) **and nightly at midnight** (if the application is left running).
    -   When a new year begins, the system will save a summary of the total reimbursements for each card from the *previous year* before clearing the current year's tracking. This allows you to review past performance per card.
    -   **MONTHLY:** Resets if the current month is after the month it was last checked, and clears its current month's completion record.
    -   **QUARTERLY:** Resets if the current quarter is after the quarter it was last checked, and clears its current quarter's completion record.
    -   **BIANNUAL:** Resets if the current half-year (Jan-Jun or Jul-Dec) is after the half-year it was last checked, and clears its current half-year's completion record.
    -   **YEARLY:** Resets if the current year is after the year it was last checked, and clears its current year's completion record.
-   **Yearly Reimbursement Tracking:**
    -   **Overall Total:** A dynamically calculated "Total Reimbursements This Year" is displayed at the top of the application, reflecting all reimbursements across all cards for the current calendar year.
    -   **Per-Card Total:** Each credit card's expanded view now displays its "Current Year Total", showing the sum of all its redemptions completed within the current year.
-   **Data Persistence:** All credit card and redemption data is stored locally in a `creditcards.json` file, so your data persists between sessions.
-   **Clean UI:** The frontend provides a simple, card-based layout for easy navigation and interaction.

## Technologies Used

-   **Frontend:** React
-   **Backend:** Spring Boot (Java)
-   **Data Storage:** Local JSON file (`creditcards.json`)

## How to Run the Application

Follow these steps to get the Credit Card Redemption Tracker up and running on your local machine.

### Prerequisites

Before you begin, ensure you have the following installed:

-   **Java Development Kit (JDK) 17 or higher:** [Download from Oracle](https://www.oracle.com/java/technologies/downloads/) or use a package manager like Homebrew (`brew install openjdk@17`).
-   **Apache Maven:** [Download from Apache](https://maven.apache.org/download.cgi) or use Homebrew (`brew install maven`).
-   **Node.js and npm:** [Download from Node.js website](https://nodejs.org/en/download/) or use Homebrew (`brew install node`).
-   **`lsof` utility:** (Usually pre-installed on macOS/Linux). Used by the `start.sh` script to manage processes.

### Quick Start (Recommended)

For the easiest way to start both the backend and frontend with a single command, use the provided `start.sh` script:

1.  Open your terminal.
2.  Navigate to the project root directory:
    ```bash
    cd /Users/aayush/Desktop/Code/cc_redemption
    ```
3.  **Make the script executable (if you haven't already):**
    ```bash
    chmod +x start.sh
    ```
4.  **Run the script:**
    ```bash
    ./start.sh
    ```
    The script will automatically stop any lingering backend processes, build and start the backend, wait for it to initialize, and then start the frontend in your browser at `http://localhost:3000`.

### Manual Start (Alternative)

If you prefer to start the backend and frontend separately, follow these steps:

### 1. Start the Backend

1.  Open your terminal.
2.  Navigate to the backend project directory:
    ```bash
    cd /Users/aayush/Desktop/Code/cc_redemption/backend/cc-redemption-backend
    ```
3.  **Clean and install dependencies (first time setup or after backend code changes):**
    ```bash
    ./mvnw clean install
    ```
4.  **Run the Spring Boot application (in the background):**
    ```bash
    ./mvnw spring-boot:run &
    ```
    The backend will start and be accessible at `http://localhost:8080`. You should see messages in the terminal indicating that it has started successfully.

    *Note: The `creditcards.json` file will be created in this directory (`/Users/aayush/Desktop/Code/cc_redemption/backend/cc-redemption-backend/`) upon the first data save.*

### 2. Start the Frontend

1.  Open a **new terminal window**.
2.  Navigate to the frontend project directory:
    ```bash
    cd /Users/aayush/Desktop/Code/cc_redemption/frontend/cc-redemption-frontend
    ```
3.  **Install frontend dependencies (first time setup):**
    ```bash
    npm install
    ```
4.  **Run the React development server:**
    ```bash
    npm start
    ```
    Your default web browser should automatically open to `http://localhost:3000`. If it doesn't, open your browser and navigate to this URL manually.

## Troubleshooting

-   **`Error: EACCES: permission denied` during `npm install` or `npm cache clean`:** Your npm cache or installation directories might have incorrect permissions. Try running the following command (you will be prompted for your user password):
    ```bash
    sudo chown -R $(whoami) "$(npm config get prefix)"/{\lib\node_modules,\bin,\share}
    # OR, if the above doesn't work, specific to the .npm directory:
    sudo chown -R <your_user_id>:<your_group_id> "/Users/aayush/.npm"
    # (Replace <your_user_id>:<your_group_id> with the actual user and group IDs from the error message, e.g., 501:20)
    ```
    After fixing permissions, try `npm cache clean --force` and then `npm install` again.
-   **Frontend not connecting to Backend:** Ensure both the backend and frontend are running. Check your browser's developer console for CORS errors. The backend has been configured to allow requests from `http://localhost:3000`.
-   **Changes not reflecting:** After making backend code changes, you *must* stop and restart the backend application for the changes to take effect. For frontend changes, a browser refresh is usually sufficient, but a hard refresh (`Ctrl+Shift+R` or `Cmd+Shift+R`) can help if issues persist.
