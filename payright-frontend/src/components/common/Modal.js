import React from 'react';
import ReactDOM from 'react-dom';
import './Modal.css'; // Create this for styling

const Modal = ({ isOpen, onClose, title, children, footer }) => {
  if (!isOpen) return null;

  return ReactDOM.createPortal(
    <div className="modal-overlay" onClick={onClose}> {/* Close on overlay click */}
      <div className="modal-content" onClick={(e) => e.stopPropagation()}> {/* Prevent closing when clicking inside modal */}
        <div className="modal-header">
          {title && <h3>{title}</h3>}
          <button onClick={onClose} className="modal-close-btn">×</button>
        </div>
        <div className="modal-body">
          {children}
        </div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>,
    document.getElementById('modal-root') // We need a <div id="modal-root"></div> in public/index.html
  );
};

export default Modal;