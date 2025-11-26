import { withAuthenticationRequired } from '@auth0/auth0-react';
import { Loading } from '../components/Loading';

/**
 * Higher-order component that protects routes requiring authentication.
 *
 * Usage:
 *   <Route path="/dashboard" element={<ProtectedRoute component={Dashboard} />} />
 *
 * If the user is not authenticated, they will be redirected to Auth0 login.
 * While checking authentication status, a loading spinner is shown.
 */
export const ProtectedRoute = ({ component }) => {
  const Component = withAuthenticationRequired(component, {
    onRedirecting: () => <Loading message="Redirecting to login..." />,
    returnTo: window.location.pathname,
  });

  return <Component />;
};
