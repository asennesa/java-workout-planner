import { useEffect, useRef, useId } from 'react';
import PropTypes from 'prop-types';
import './ui.css';

export const Modal = ({ isOpen, onClose, title, children, size = 'medium' }) => {
  const modalRef = useRef(null);
  const onCloseRef = useRef(onClose);
  const titleId = useId();

  // Keep onClose ref updated without triggering useEffect
  useEffect(() => {
    onCloseRef.current = onClose;
  });

  // Handle escape key and body overflow
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') onCloseRef.current();
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
      // Focus the modal only on initial open
      modalRef.current?.focus();
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]); // Only depend on isOpen, not onClose

  if (!isOpen) return null;

  return (
    <div
      className="modal-overlay"
      onClick={onClose}
      role="presentation"
    >
      <div
        ref={modalRef}
        className={`modal modal-${size}`}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
      >
        <div className="modal-header">
          <h2 id={titleId}>{title}</h2>
          <button
            className="modal-close"
            onClick={onClose}
            aria-label="Close modal"
            type="button"
          >
            &times;
          </button>
        </div>
        <div className="modal-content">{children}</div>
      </div>
    </div>
  );
};

Modal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  children: PropTypes.node,
  size: PropTypes.oneOf(['small', 'medium', 'large']),
};
