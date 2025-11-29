import { useId } from 'react';
import './ui.css';

export const Input = ({
  label,
  error,
  type = 'text',
  className = '',
  id: providedId,
  ...props
}) => {
  const generatedId = useId();
  const inputId = providedId || generatedId;

  return (
    <div className={`form-group ${className}`}>
      {label && <label htmlFor={inputId} className="form-label">{label}</label>}
      <input
        id={inputId}
        type={type}
        className={`form-input ${error ? 'input-error' : ''}`}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      />
      {error && <span id={`${inputId}-error`} className="error-text" role="alert">{error}</span>}
    </div>
  );
};

export const TextArea = ({
  label,
  error,
  className = '',
  rows = 3,
  id: providedId,
  ...props
}) => {
  const generatedId = useId();
  const inputId = providedId || generatedId;

  return (
    <div className={`form-group ${className}`}>
      {label && <label htmlFor={inputId} className="form-label">{label}</label>}
      <textarea
        id={inputId}
        rows={rows}
        className={`form-input form-textarea ${error ? 'input-error' : ''}`}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      />
      {error && <span id={`${inputId}-error`} className="error-text" role="alert">{error}</span>}
    </div>
  );
};

export const Select = ({
  label,
  error,
  options,
  placeholder = 'Select...',
  className = '',
  id: providedId,
  ...props
}) => {
  const generatedId = useId();
  const inputId = providedId || generatedId;

  return (
    <div className={`form-group ${className}`}>
      {label && <label htmlFor={inputId} className="form-label">{label}</label>}
      <select
        id={inputId}
        className={`form-input form-select ${error ? 'input-error' : ''}`}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      >
        <option value="">{placeholder}</option>
        {options.map((option) => (
          <option key={option.value || option} value={option.value || option}>
            {option.label || option.replace(/_/g, ' ')}
          </option>
        ))}
      </select>
      {error && <span id={`${inputId}-error`} className="error-text" role="alert">{error}</span>}
    </div>
  );
};
