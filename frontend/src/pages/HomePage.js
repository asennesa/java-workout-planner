import { useAuth0 } from '@auth0/auth0-react';
import { Link } from 'react-router-dom';
import './Pages.css';

/**
 * Public home page - shows different content based on auth status.
 */
export const HomePage = () => {
  const { isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return <div className="page">Loading...</div>;
  }

  return (
    <div className="page home-page">
      <h1>Welcome to Workout Planner</h1>
      <p>Track your workouts, monitor progress, and achieve your fitness goals.</p>

      {isAuthenticated ? (
        <div className="home-actions">
          <Link to="/dashboard" className="cta-button">
            Go to Dashboard
          </Link>
        </div>
      ) : (
        <div className="home-actions">
          <p>Sign in to start tracking your workouts.</p>
        </div>
      )}
    </div>
  );
};
