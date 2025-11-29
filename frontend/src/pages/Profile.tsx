import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';
import { apiService } from '../services/api';
import type { User } from '../types';
import { Alert, Card, CardBody, StatusBadge } from '../components/ui';
import './Pages.css';

export const Profile = (): JSX.Element => {
  const { user: auth0User } = useAuth0();
  const [backendUser, setBackendUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async (): Promise<void> => {
      try {
        setLoading(true);
        const data = await apiService.getCurrentUser();
        setBackendUser(data);
      } catch (err) {
        console.error('Failed to fetch profile:', err);
        setError(err instanceof Error ? err.message : 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  return (
    <div className="page profile-page">
      <div className="page-header">
        <div>
          <h1>Profile</h1>
          <p className="page-subtitle">Manage your account information</p>
        </div>
      </div>

      <section className="profile-section">
        <h2>Account Information</h2>
        {auth0User && (
          <div className="profile-card">
            {auth0User.picture && (
              <img src={auth0User.picture} alt={auth0User.name} className="profile-picture" />
            )}
            <div className="profile-details">
              <p>
                <strong>Name:</strong> {auth0User.name}
              </p>
              <p>
                <strong>Email:</strong> {auth0User.email}
              </p>
              <p>
                <strong>Email Verified:</strong>{' '}
                <StatusBadge status={auth0User.email_verified ? 'COMPLETED' : 'PLANNED'} />
              </p>
            </div>
          </div>
        )}
      </section>

      <section className="profile-section">
        <h2>Application Profile</h2>

        {loading && (
          <Card>
            <CardBody>
              <div className="loading-container" style={{ padding: '20px' }}>
                <div className="loading-spinner" />
                <p className="loading-text">Loading profile...</p>
              </div>
            </CardBody>
          </Card>
        )}

        {error && <Alert type="error">{error}</Alert>}

        {!loading && !error && backendUser && (
          <div className="profile-card">
            <div className="profile-details">
              <p>
                <strong>User ID:</strong> {backendUser.userId}
              </p>
              <p>
                <strong>Username:</strong> {backendUser.username || '-'}
              </p>
              <p>
                <strong>Email:</strong> {backendUser.email}
              </p>
              <p>
                <strong>First Name:</strong> {backendUser.firstName || '-'}
              </p>
              <p>
                <strong>Last Name:</strong> {backendUser.lastName || '-'}
              </p>
              <p>
                <strong>Role:</strong> <StatusBadge status={backendUser.role} />
              </p>
            </div>
          </div>
        )}
      </section>
    </div>
  );
};
