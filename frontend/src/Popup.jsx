import React from 'react';
import { createRoot } from 'react-dom/client';
import './popup.css';

const Popup = () => {
  return (
    <div className="popup-container">
      <div className="popup-header">
        <img src="icon-128.png" alt="logo" className="popup-logo" />
        <h2 className="popup-title">Polish My Tone</h2>
      </div>
      
      <p className="popup-description">
        Your AI assistant is active. Open a GitHub Pull Request to start seeing suggestions.
      </p>
      
      <div className="popup-status-box">
        Status: <span className="popup-status-indicator">● Running</span>
      </div>
    </div>
  );
};

const container = document.getElementById('root');
if (container) {
  const root = createRoot(container);
  root.render(<Popup />);
}