import { useId, type InputHTMLAttributes, type TextareaHTMLAttributes, type SelectHTMLAttributes } from 'react';
import './ui.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}

interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  options: (SelectOption | string)[];
  placeholder?: string;
}

export const Input = ({
  label,
  error,
  type = 'text',
  className = '',
  id: providedId,
  ...props
}: InputProps): JSX.Element => {
  const generatedId = useId();
  const inputId = providedId || generatedId;

  return (
    <div className={`form-group ${className}`}>
      {label && (
        <label htmlFor={inputId} className="form-label">
          {label}
        </label>
      )}
      <input
        id={inputId}
        type={type}
        className={`form-input ${error ? 'input-error' : ''}`}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      />
      {error && (
        <span id={`${inputId}-error`} className="error-text" role="alert">
          {error}
        </span>
      )}
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
}: TextAreaProps): JSX.Element => {
  const generatedId = useId();
  const inputId = providedId || generatedId;

  return (
    <div className={`form-group ${className}`}>
      {label && (
        <label htmlFor={inputId} className="form-label">
          {label}
        </label>
      )}
      <textarea
        id={inputId}
        rows={rows}
        className={`form-input form-textarea ${error ? 'input-error' : ''}`}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      />
      {error && (
        <span id={`${inputId}-error`} className="error-text" role="alert">
          {error}
        </span>
      )}
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
}: SelectProps): JSX.Element => {
  const generatedId = useId();
  const inputId = providedId || generatedId;

  return (
    <div className={`form-group ${className}`}>
      {label && (
        <label htmlFor={inputId} className="form-label">
          {label}
        </label>
      )}
      <select
        id={inputId}
        className={`form-input form-select ${error ? 'input-error' : ''}`}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error ? `${inputId}-error` : undefined}
        {...props}
      >
        <option value="">{placeholder}</option>
        {options.map((option) => {
          const value = typeof option === 'string' ? option : option.value;
          const label = typeof option === 'string' ? option.replace(/_/g, ' ') : option.label;
          return (
            <option key={value} value={value}>
              {label}
            </option>
          );
        })}
      </select>
      {error && (
        <span id={`${inputId}-error`} className="error-text" role="alert">
          {error}
        </span>
      )}
    </div>
  );
};
