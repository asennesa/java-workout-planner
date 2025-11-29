import type { ComponentType } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { Loading } from '../components/Loading';

interface ProtectedRouteProps {
  component: ComponentType;
}

/**
 * Component that protects routes requiring authentication.
 * Uses useAuth0 hook directly instead of HOC to avoid creating
 * new component instances on every render.
 *
 * Usage:
 *   <Route path="/dashboard" element={<ProtectedRoute component={Dashboard} />} />
 */
export const ProtectedRoute = ({ component: Component }: ProtectedRouteProps): JSX.Element => {
  const { isAuthenticated, isLoading, loginWithRedirect } = useAuth0();

  if (isLoading) {
    return <Loading message="Checking authentication..." />;
  }

  if (!isAuthenticated) {
    loginWithRedirect({
      appState: { returnTo: window.location.pathname },
    });
    return <Loading message="Redirecting to login..." />;
  }

  return <Component />;
};
