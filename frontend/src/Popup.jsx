import React, { useState, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import './popup.css';

const BACKEND_URL = 'http://localhost:3000';

const Popup = () => {
  // "checking" | "running" | "offline"
  const [status, setStatus] = useState('checking');

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const res = await fetch(`${BACKEND_URL}/api/health`, { method: 'GET' });
        if (res.ok) {
          const data = await res.json();
          setStatus(data.status === 'ok' ? 'running' : 'offline');
        } else {
          setStatus('offline');
        }
      } catch {
        setStatus('offline');
      }
    };

    checkHealth();

    // Re-check every 30 seconds while the popup is open
    const interval = setInterval(checkHealth, 30_000);
    return () => clearInterval(interval);
  }, []);

  const statusConfig = {
    checking: { label: '⏳ Checking…', className: 'status-checking' },
    running:  { label: '● Running',    className: 'status-running'  },
    offline:  { label: '○ Offline',    className: 'status-offline'  },
  };

  const { label, className } = statusConfig[status];

  return (
    <div className="popup-container">
      <div className="popup-header">
        <img src="icon-128.png" alt="logo" className="popup-logo" />
        <h2 className="popup-title">Polish My Tone</h2>
      </div>

      <p className="popup-description">
        {status === 'running'
          ? 'Your AI assistant is active. Open a GitHub Pull Request to start seeing suggestions.'
          : status === 'offline'
            ? 'Backend server is unreachable. Please make sure the server is running.'
            : 'Checking backend server status…'}
      </p>

      <div className="popup-status-box">
        Status: <span className={`popup-status-indicator ${className}`}>{label}</span>
      </div>
    </div>
  );
};

const container = document.getElementById('root');
if (container) {
  const root = createRoot(container);
  root.render(<Popup />);
}