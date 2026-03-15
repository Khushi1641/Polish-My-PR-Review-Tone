# ✨ Polish My Tone

We've all been there - you're deep in a code review, you type out honest feedback, and later realize it came across way harsher than you meant. Rewording takes time, and sometimes you just don't know how to say it better.

Polish My Tone is a Chrome extension that fixes this. It sits inside GitHub's PR comment boxes, watches what you type, and suggests a friendlier version of your comment - keeping all the technical feedback intact. Think of it as a tone filter for code reviews.

**😬 Type this:**
> "This code is terrible, rewrite the whole thing."

**✨ Get this:**
> "There are a few areas here that could use some restructuring - happy to walk through some ideas if that'd help!"

Same point. Better delivery.

## 🎬 Demo

<video src="Demo.mp4" controls width="100%"></video>

## 🤔 Why?

- Harsh PR comments kill motivation. Nobody writes their best code after being told their work is "terrible."
- You shouldn't have to spend 5 minutes rephrasing every comment just to not sound rude.
- Better tone → fewer conflicts → stronger teams. It's that simple.

## 🛠 Setup

You'll need **Java 17+** and **Node.js 24** installed.

### Backend

The backend is a lightweight Java HTTP server that talks to the Gemini API.

```bash
# set your Gemini API key
export GEMINI_API_KEY=<your-key>
```

Compile and run the `Main.java` file. The server will start at `http://localhost:3000`.

### Frontend

```bash
# from the frontend/ directory
npm install
npm run build
```

This creates a `dist/` folder with the extension files.

### Load it in Chrome

1. Go to `chrome://extensions`
2. Enable **Developer mode** (top-right toggle)
3. Click **Load unpacked** → select the `frontend/dist` folder

Open any GitHub PR, type a comment (10+ chars), and wait a couple seconds. A polished suggestion appears right below your text box. Hit use it or retry for a different take.

## ⚙️ Tech Stack

| Layer | Tech |
|-------|------|
| Extension | Manifest V3, React 18 |
| Backend | Java |
| AI | Google Gemini 2.5 Flash |
