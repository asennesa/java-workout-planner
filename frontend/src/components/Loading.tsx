import './Loading.css';

interface LoadingProps {
  message?: string;
}

/**
 * Simple loading spinner component.
 */
export const Loading = ({ message = 'Loading...' }: LoadingProps): JSX.Element => {
  return (
    <div className="loading-container">
      <div className="loading-spinner"></div>
      <p className="loading-message">{message}</p>
    </div>
  );
};
