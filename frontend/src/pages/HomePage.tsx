import { useAuth0 } from '@auth0/auth0-react';
import { Link } from 'react-router-dom';
import { Button } from '../components/ui';
import './Pages.css';

export const HomePage = (): JSX.Element => {
  const { isAuthenticated, isLoading, loginWithRedirect } = useAuth0();

  if (isLoading) {
    return (
      <div className="page">
        <div className="loading-container">
          <div className="loading-spinner" />
        </div>
      </div>
    );
  }

  return (
    <div className="page home-page">
      <h1>Workout Planner</h1>
      <p>
        Plan, track, and achieve your fitness goals with our comprehensive workout management
        platform.
      </p>

      <div className="home-actions">
        {isAuthenticated ? (
          <Link to="/dashboard" className="cta-button">
            Go to Dashboard
          </Link>
        ) : (
          <Button size="large" onClick={() => loginWithRedirect()}>
            Get Started
          </Button>
        )}
      </div>

      <div className="features-grid">
        <div className="feature-card">
          <div className="feature-icon">📋</div>
          <h3>Plan Your Workouts</h3>
          <p>
            Create and organize workout sessions with a variety of exercises tailored to your
            fitness goals.
          </p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">💪</div>
          <h3>Track Your Progress</h3>
          <p>
            Log sets, reps, and weights for strength training. Track duration and distance for
            cardio exercises.
          </p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">📚</div>
          <h3>Exercise Library</h3>
          <p>
            Access a comprehensive library of exercises with filtering by type, muscle group, and
            difficulty.
          </p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">🎯</div>
          <h3>Stay Motivated</h3>
          <p>Monitor your workout history and see your improvements over time to stay on track.</p>
        </div>
      </div>
    </div>
  );
};
