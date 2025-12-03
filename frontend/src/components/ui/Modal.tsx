import { useEffect, useRef, useId, useState, type ReactNode, type MouseEvent } from 'react';
import './ui.css';

type ModalSize = 'small' | 'medium' | 'large';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children?: ReactNode;
  size?: ModalSize;
}

export const Modal = ({
  isOpen,
  onClose,
  title,
  children,
  size = 'medium',
}: ModalProps): JSX.Element | null => {
  const modalRef = useRef<HTMLDivElement>(null);
  const onCloseRef = useRef(onClose);
  const titleId = useId();
  const [hasAnimated, setHasAnimated] = useState(false);

  // Keep onClose ref updated without triggering useEffect
  useEffect(() => {
    onCloseRef.current = onClose;
  });

  // Reset animation state when modal closes
  useEffect(() => {
    if (!isOpen) {
      setHasAnimated(false);
    }
  }, [isOpen]);

  // Handle escape key and body overflow
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent): void => {
      if (e.key === 'Escape') onCloseRef.current();
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
      // Focus the modal only on initial open
      modalRef.current?.focus();
      // Mark as animated after first render
      if (!hasAnimated) {
        setHasAnimated(true);
      }
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, hasAnimated]);

  if (!isOpen) return null;

  const handleOverlayClick = (): void => {
    onClose();
  };

  const handleModalClick = (e: MouseEvent<HTMLDivElement>): void => {
    e.stopPropagation();
  };

  // Apply animation class only on first render when opening
  const overlayClassName = hasAnimated ? 'modal-overlay modal-overlay-static' : 'modal-overlay';
  const modalClassName = hasAnimated
    ? `modal modal-${size} modal-static`
    : `modal modal-${size}`;

  return (
    <div className={overlayClassName} onClick={handleOverlayClick} role="presentation">
      <div
        ref={modalRef}
        className={modalClassName}
        onClick={handleModalClick}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
      >
        <div className="modal-header">
          <h2 id={titleId}>{title}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close modal" type="button">
            &times;
          </button>
        </div>
        <div className="modal-content">{children}</div>
      </div>
    </div>
  );
};
