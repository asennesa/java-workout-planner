import { useEffect, useState, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { initializeApi } from '../services/api';

interface ApiInitializerResult {
  showRefreshPrompt: boolean;
  handleRefresh: () => void;
  dismissPrompt: () => void;
}

/**
 * Hook that initializes the API service with Auth0's getAccessTokenSilently.
 * Call this once at the app level, inside the Auth0Provider.
 *
 * Returns state and handlers for the fallback refresh prompt when silent refresh fails.
 */
export const useApiInitializer = (): ApiInitializerResult => {
  const { getAccessTokenSilently, isAuthenticated, logout, loginWithRedirect } = useAuth0();
  const [showRefreshPrompt, setShowRefreshPrompt] = useState(false);

  const handleRefresh = useCallback(() => {
    // Clear Auth0 session and redirect to login
    // This ensures we get a completely fresh token with updated claims
    logout({
      logoutParams: {
        returnTo: window.location.origin
      }
    }).then(() => {
      // After logout completes, trigger login
      loginWithRedirect();
    });
  }, [logout, loginWithRedirect]);

  const dismissPrompt = useCallback(() => {
    setShowRefreshPrompt(false);
  }, []);

  const onTokenRefreshFailed = useCallback(() => {
    setShowRefreshPrompt(true);
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      initializeApi(getAccessTokenSilently, onTokenRefreshFailed);
    }
  }, [getAccessTokenSilently, isAuthenticated, onTokenRefreshFailed]);

  return {
    showRefreshPrompt,
    handleRefresh,
    dismissPrompt,
  };
};
