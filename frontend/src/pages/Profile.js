import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';
import { apiService } from '../services/api';
import './Pages.css';

/**
 * Protected profile page - shows user info from Auth0 and backend.
 */
export const Profile = () => {
  const { user: auth0User } = useAuth0();
  const [backendUser, setBackendUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const data = await apiService.getCurrentUser();
        setBackendUser(data);
      } catch (err) {
        console.error('Failed to fetch profile:', err);
        setError(err.message || 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  return (
    <div className="page profile-page">
      <h1>Profile</h1>

      <section className="profile-section">
        <h2>Auth0 Profile</h2>
        {auth0User && (
          <div className="profile-card">
            {auth0User.picture && (
              <img
                src={auth0User.picture}
                alt={auth0User.name}
                className="profile-picture"
              />
            )}
            <div className="profile-details">
              <p><strong>Name:</strong> {auth0User.name}</p>
              <p><strong>Email:</strong> {auth0User.email}</p>
              <p><strong>Email Verified:</strong> {auth0User.email_verified ? 'Yes' : 'No'}</p>
            </div>
          </div>
        )}
      </section>

      <section className="profile-section">
        <h2>Backend Profile</h2>

        {loading && <p>Loading backend profile...</p>}

        {error && (
          <div className="error-message">
            <p>{error}</p>
          </div>
        )}

        {!loading && !error && backendUser && (
          <div className="profile-card">
            <div className="profile-details">
              <p><strong>User ID:</strong> {backendUser.userId}</p>
              <p><strong>Username:</strong> {backendUser.username}</p>
              <p><strong>Email:</strong> {backendUser.email}</p>
              <p><strong>First Name:</strong> {backendUser.firstName}</p>
              <p><strong>Last Name:</strong> {backendUser.lastName}</p>
              <p><strong>Role:</strong> {backendUser.role}</p>
            </div>
          </div>
        )}
      </section>
    </div>
  );
};
