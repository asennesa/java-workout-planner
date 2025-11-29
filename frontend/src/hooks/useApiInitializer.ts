import { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { initializeApi } from '../services/api';

/**
 * Hook that initializes the API service with Auth0's getAccessTokenSilently.
 * Call this once at the app level, inside the Auth0Provider.
 */
export const useApiInitializer = (): void => {
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();

  useEffect(() => {
    if (isAuthenticated) {
      initializeApi(getAccessTokenSilently);
    }
  }, [getAccessTokenSilently, isAuthenticated]);
};
