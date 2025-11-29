import './ui.css';

export const Alert = ({ type = 'info', children, onClose, className = '' }) => {
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

export const ErrorMessage = ({ message, onRetry }) => (
  <Alert type="error">
    <p>{message}</p>
    {onRetry && (
      <button className="btn btn-small btn-outline" onClick={onRetry}>
        Try Again
      </button>
    )}
  </Alert>
);
