import React, { useState, useEffect } from 'react';

const Suggestion = ({ box, initialSuggestion, onRetry }) => {
  const [suggestion, setSuggestion] = useState(initialSuggestion);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setSuggestion(initialSuggestion);
  }, [initialSuggestion]);

  const handleClose = () => {
    setSuggestion(""); 
  };

  if (!suggestion) return null;

  const handleRetry = async () => {
    setLoading(true);
    const newText = await onRetry();
    if (newText) setSuggestion(newText);
    setLoading(false);
  };

  return (
    <div className="suggestion">
      <div className="suggestion-header">✨ Suggestions</div>
      
      <div className="suggestion-text">
        {suggestion}
      </div>
      
      <div className="suggestion-actions">
        
        <button 
          className="suggestion-btn btn-primary" 
          onClick={handleRetry} 
          disabled={loading}
        >
          {loading ? 'Retrying...' : 'Retry'}
        </button>

        <button 
          className="suggestion-btn btn-secondary" 
          onClick={handleClose}
        >
          Close
        </button>
      </div>
    </div>
  );
};

export default Suggestion;