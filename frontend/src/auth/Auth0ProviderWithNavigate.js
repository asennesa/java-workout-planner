import { Auth0Provider } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

/**
 * Auth0 Provider wrapper that integrates with React Router.
 *
 * This component wraps the Auth0Provider and configures it with:
 * - Your Auth0 domain and client ID from environment variables
 * - The API audience for requesting access tokens
 * - Redirect handling after login
 *
 * Environment variables required:
 * - REACT_APP_AUTH0_DOMAIN: Your Auth0 tenant domain
 * - REACT_APP_AUTH0_CLIENT_ID: Your Auth0 application client ID
 * - REACT_APP_AUTH0_AUDIENCE: Your API identifier (audience)
 */
export const Auth0ProviderWithNavigate = ({ children }) => {
  const navigate = useNavigate();

  const domain = process.env.REACT_APP_AUTH0_DOMAIN;
  const clientId = process.env.REACT_APP_AUTH0_CLIENT_ID;
  const audience = process.env.REACT_APP_AUTH0_AUDIENCE;
  const redirectUri = window.location.origin;

  // Validate required environment variables
  if (!domain || !clientId || !audience) {
    console.error(
      'Auth0 configuration missing. Please set REACT_APP_AUTH0_DOMAIN, ' +
      'REACT_APP_AUTH0_CLIENT_ID, and REACT_APP_AUTH0_AUDIENCE in your .env file.'
    );
    return (
      <div style={{ padding: '20px', color: 'red' }}>
        <h2>Auth0 Configuration Error</h2>
        <p>Missing required environment variables. Check console for details.</p>
      </div>
    );
  }

  /**
   * Called after Auth0 redirects back to the app.
   * Navigates to the originally requested page or home.
   */
  const onRedirectCallback = (appState) => {
    navigate(appState?.returnTo || window.location.pathname);
  };

  return (
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: redirectUri,
        audience: audience,
        scope: 'openid profile email',
      }}
      onRedirectCallback={onRedirectCallback}
      cacheLocation="localstorage"
    >
      {children}
    </Auth0Provider>
  );
};
