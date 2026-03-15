import React from 'react';
import { createRoot } from 'react-dom/client';
import Suggestion from './Suggestion';
import './styles.css';

const endpoint = "http://localhost:3000/polish";

async function fetchPolishedComment(text) {
  try {
    const response = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ comment: text }),
    });
    
    if (!response.ok) {
      return null;
    }
    
    const data = await response.json();
    return data.suggestion;
  } catch (e) {
    return null;
  }
}

const observer = new MutationObserver(() => {

  const commentBoxes = document.querySelectorAll("textarea");

  commentBoxes.forEach((box) => {
    if (box.dataset.emotionHooked) return;
    box.dataset.emotionHooked = "true";

    const rootContainer = document.createElement("div");
    rootContainer.className = "react-injection-root";
    box.parentElement.appendChild(rootContainer);
    
    const root = createRoot(rootContainer);

    let timeout;
    box.addEventListener("input", () => {
      clearTimeout(timeout);
      timeout = setTimeout(async () => {
        if (box.value.length < 10) {
          return;
        }
        
        const suggestion = await fetchPolishedComment(box.value);
        if (suggestion) {
          root.render(
            <Suggestion 
              box={box} 
              initialSuggestion={suggestion} 
              onRetry={() => fetchPolishedComment(box.value)} 
            />
          );
        }
      }, 2000);
    });
  });
});

observer.observe(document.body, { childList: true, subtree: true });