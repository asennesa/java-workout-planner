import type { ReactNode } from 'react';
import { Auth0Provider, AppState } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

interface Auth0ProviderWithNavigateProps {
  children: ReactNode;
}

/**
 * Auth0 Provider wrapper that integrates with React Router.
 *
 * This component wraps the Auth0Provider and configures it with:
 * - Your Auth0 domain and client ID from environment variables
 * - The API audience for requesting access tokens
 * - Redirect handling after login
 */
export const Auth0ProviderWithNavigate = ({
  children,
}: Auth0ProviderWithNavigateProps): JSX.Element => {
  const navigate = useNavigate();

  const domain = import.meta.env.VITE_AUTH0_DOMAIN;
  const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID;
  const audience = import.meta.env.VITE_AUTH0_AUDIENCE;
  const redirectUri = window.location.origin;

  // Validate required environment variables
  if (!domain || !clientId || !audience) {
    console.error(
      'Auth0 configuration missing. Please set VITE_AUTH0_DOMAIN, ' +
        'VITE_AUTH0_CLIENT_ID, and VITE_AUTH0_AUDIENCE in your .env file.'
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
  const onRedirectCallback = (appState?: AppState): void => {
    navigate(appState?.returnTo || window.location.pathname);
  };

  return (
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: redirectUri,
        audience: audience,
        scope: 'openid profile email offline_access',
      }}
      onRedirectCallback={onRedirectCallback}
      cacheLocation="localstorage"
      useRefreshTokens={true}
    >
      {children}
    </Auth0Provider>
  );
};
