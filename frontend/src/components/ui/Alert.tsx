import type { ReactNode } from 'react';
import './ui.css';

type AlertType = 'info' | 'success' | 'warning' | 'error';

interface AlertProps {
  type?: AlertType;
  children: ReactNode;
  onClose?: () => void;
  className?: string;
}

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export const Alert = ({
  type = 'info',
  children,
  onClose,
  className = '',
}: AlertProps): JSX.Element => {
  return (
    <div className={`alert alert-${type} ${className}`}>
      <div className="alert-content">{children}</div>
      {onClose && (
        <button className="alert-close" onClick={onClose}>
          &times;
        </button>
      )}
    </div>
  );
};

export const ErrorMessage = ({ message, onRetry }: ErrorMessageProps): JSX.Element => (
  <Alert type="error">
    <p>{message}</p>
    {onRetry && (
      <button className="btn btn-small btn-outline" onClick={onRetry}>
        Try Again
      </button>
    )}
  </Alert>
);
