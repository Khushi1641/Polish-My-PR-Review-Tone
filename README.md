# 💅 Polish My Tone - Chrome Extension

An AI-powered Chrome extension that acts as an emotionally intelligent code reviewer. It automatically monitors GitHub Pull Request text areas and uses Google's Gemini AI to suggest friendlier, more constructive versions of your PR comments while preserving the core technical feedback.

## ✨ Features

* **Seamless GitHub Integration:** Automatically detects `<textarea>` inputs on `https://github.com/*` using a `MutationObserver`.
* **Real-time AI Suggestions:** Analyzes your comment while you type (with a smart 2-second debounce to prevent API spam) and fetches polished alternatives.
* **In-Line React UI:** Injects a clean React component directly below the comment box with the suggested text.
* **Actionable Controls:** Easily **Retry** to generate a new suggestion or **Close** the UI when you are done.
* **Lightweight Java Backend:** Uses a custom built-in Java HTTP server to securely interact with the Google GenAI API (Gemini 2.5 Flash).

## 🛠️ Tech Stack

**Frontend (Chrome Extension)**
* **Library:** React 18
* **Build Tool:** Vite (configured for multiple entry points: popup and content script)
* **Extension API:** Manifest V3

**Backend (API Proxy)**
* **Language:** Java
* **Server:** `com.sun.net.httpserver` (Native Java HTTP Server)
* **AI Integration:** Google GenAI SDK (`gemini-2.5-flash`)
* **JSON Parsing:** Google Gson

## 🚀 Getting Started

Follow these steps to run the backend, build the frontend, and install the extension into your browser.

### 1. Backend Setup (Java)

The Java server acts as a secure proxy to communicate with the Gemini API.

1. Ensure you have Java installed on your machine.
2. Set up your Google Gemini API credentials. The backend relies on the default `GEMINI_API_KEY` environment variable on your system.
3. Compile and run the `Main.java` file.
4. You should see the following message in your terminal indicating the server is alive:
   ```text
   ✅ Polish server running at http://localhost:3000

### 2. Frontend Setup (React/Vite)

The frontend contains two Vite configurations (one for the popup and one for the injected content script) that need to be built into the same directory.

1. Open a new terminal and navigate to your frontend directory.
2. Install the project dependencies: npm install
3. Build the extension files: npm run build. This generates a dist folder containing your manifest.json, index.html, content.js, and compiled CSS.

### 3. Loading the Extension into Chrome

Once the frontend is built, you need to load that dist folder into your browser.

1. Open Google Chrome and navigate to chrome://extensions/ in your URL bar.
2. Turn on Developer mode using the toggle switch in the top right corner.
3. Click the Load unpacked button that appears in the top left.
4. Select the dist folder that was just generated inside your frontend directory.
5. Make sure the extension is enabled (the toggle switch on the extension card should be blue).

### 4. How to Use

1. Ensure your Java backend is actively running.
2. Navigate to any GitHub Pull Request (e.g., https://github.com/owner/repo/pull/1).
3. Type a comment that is at least 10 characters long into the PR review box.
4. Wait 2 seconds, and the React UI will appear below the box with a polished suggestion.

## 🐞 Troubleshooting

- No suggestions appearing? Ensure the Java backend is running on localhost:3000. The frontend content script attempts to fetch from this exact endpoint.
- 500 Internal Server Error: Check the Java terminal logs. This usually indicates an issue reaching the Gemini API (e.g., missing API key or network issues) or malformed JSON payload.
- UI Not Injecting: Refresh the GitHub page. Single-page application (SPA) navigation in GitHub sometimes requires the MutationObserver to re-attach.
