import { useAuth0 } from '@auth0/auth0-react';
import './AuthButtons.css';

/**
 * Login button that redirects to Auth0 Universal Login.
 */
export const LoginButton = (): JSX.Element => {
  const { loginWithRedirect, isLoading } = useAuth0();

  return (
    <button
      className="auth-button login-button"
      onClick={() => loginWithRedirect()}
      disabled={isLoading}
    >
      Log In
    </button>
  );
};

/**
 * Signup button that redirects to Auth0 signup screen.
 */
export const SignupButton = (): JSX.Element => {
  const { loginWithRedirect, isLoading } = useAuth0();

  return (
    <button
      className="auth-button signup-button"
      onClick={() =>
        loginWithRedirect({
          authorizationParams: {
            screen_hint: 'signup',
          },
        })
      }
      disabled={isLoading}
    >
      Sign Up
    </button>
  );
};

/**
 * Logout button that clears the session and redirects to home.
 */
export const LogoutButton = (): JSX.Element => {
  const { logout, isLoading } = useAuth0();

  return (
    <button
      className="auth-button logout-button"
      onClick={() =>
        logout({
          logoutParams: {
            returnTo: window.location.origin,
          },
        })
      }
      disabled={isLoading}
    >
      Log Out
    </button>
  );
};

/**
 * Displays user profile info and logout button when authenticated,
 * or login/signup buttons when not authenticated.
 */
export const AuthNav = (): JSX.Element => {
  const { isAuthenticated, isLoading, user } = useAuth0();

  if (isLoading) {
    return <div className="auth-nav">Loading...</div>;
  }

  return (
    <div className="auth-nav">
      {isAuthenticated ? (
        <div className="auth-nav-authenticated">
          <span className="user-greeting">Hello, {user?.name || user?.email}</span>
          <LogoutButton />
        </div>
      ) : (
        <div className="auth-nav-unauthenticated">
          <LoginButton />
          <SignupButton />
        </div>
      )}
    </div>
  );
};
