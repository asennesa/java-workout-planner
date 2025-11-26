import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';
import { apiService } from '../services/api';
import './Pages.css';

/**
 * Protected dashboard page - requires authentication.
 * Fetches user's workouts from the API.
 */
export const Dashboard = () => {
  const { user } = useAuth0();
  const [workouts, setWorkouts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchWorkouts = async () => {
      try {
        setLoading(true);
        const data = await apiService.getWorkouts();
        setWorkouts(data.content || []);
      } catch (err) {
        console.error('Failed to fetch workouts:', err);
        setError(err.message || 'Failed to load workouts');
      } finally {
        setLoading(false);
      }
    };

    fetchWorkouts();
  }, []);

  return (
    <div className="page dashboard-page">
      <h1>Dashboard</h1>
      <p>Welcome back, {user?.name || user?.email}!</p>

      <section className="workouts-section">
        <h2>Your Workouts</h2>

        {loading && <p>Loading workouts...</p>}

        {error && (
          <div className="error-message">
            <p>{error}</p>
            <p className="error-hint">
              Make sure the backend is running and you have the correct permissions.
            </p>
          </div>
        )}

        {!loading && !error && workouts.length === 0 && (
          <p>No workouts yet. Create your first workout to get started!</p>
        )}

        {!loading && !error && workouts.length > 0 && (
          <ul className="workouts-list">
            {workouts.map((workout) => (
              <li key={workout.sessionId} className="workout-item">
                <strong>{workout.name}</strong>
                <span className="workout-status">{workout.status}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
};
