import React from 'react';

const AlertMessage = ({ type = 'info', message, onClose }) => {
  if (!message) return null;

  const alertTypeClass = `alert-${type}`; // e.g., alert-danger, alert-success

  return (
    <div className={`alert ${alertTypeClass}`} role="alert">
      {message}
      {onClose && (
        <button type="button" className="close" onClick={onClose} aria-label="Close" style={{ float: 'right', background: 'none', border: 'none', fontSize: '1.2rem', fontWeight: 'bold', cursor: 'pointer', marginLeft: '15px' }}>
          <span aria-hidden="true">×</span>
        </button>
      )}
    </div>
  );
};

export default AlertMessage;