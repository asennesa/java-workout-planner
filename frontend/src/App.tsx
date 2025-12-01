import { lazy, Suspense } from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { Auth0ProviderWithNavigate, ProtectedRoute } from './auth';
import { AuthNav, ErrorBoundary, Loading, Modal, Button } from './components';
import { useApiInitializer } from './hooks';
import { HomePage } from './pages';
import './App.css';

// Lazy load pages for code splitting
const Dashboard = lazy(() =>
  import('./pages/Dashboard').then((m) => ({ default: m.Dashboard }))
);
const Profile = lazy(() =>
  import('./pages/Profile').then((m) => ({ default: m.Profile }))
);
const WorkoutDetail = lazy(() =>
  import('./pages/WorkoutDetail').then((m) => ({ default: m.WorkoutDetail }))
);
const Exercises = lazy(() =>
  import('./pages/Exercises').then((m) => ({ default: m.Exercises }))
);

// 404 Page component
const NotFound = (): JSX.Element => (
  <div className="page" style={{ textAlign: 'center', paddingTop: '60px' }}>
    <h1>404 - Page Not Found</h1>
    <p>The page you're looking for doesn't exist.</p>
    <Link to="/" className="cta-button" style={{ marginTop: '20px', display: 'inline-block' }}>
      Go Home
    </Link>
  </div>
);

function AppContent(): JSX.Element {
  const { showRefreshPrompt, handleRefresh, dismissPrompt } = useApiInitializer();

  return (
    <div className="App">
      <header className="App-header">
        <nav className="navbar">
          <div className="nav-brand">
            <Link to="/">Workout Planner</Link>
          </div>
          <div className="nav-links">
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/exercises">Exercises</Link>
          </div>
          <AuthNav />
        </nav>
      </header>

      <main className="App-main">
        <Suspense fallback={<Loading message="Loading..." />}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/dashboard" element={<ProtectedRoute component={Dashboard} />} />
            <Route
              path="/workouts/:sessionId"
              element={<ProtectedRoute component={WorkoutDetail} />}
            />
            <Route path="/exercises" element={<ProtectedRoute component={Exercises} />} />
            <Route path="/profile" element={<ProtectedRoute component={Profile} />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </main>

      {/* Modal when email verification is required */}
      <Modal
        isOpen={showRefreshPrompt}
        onClose={handleRefresh}
        title="Email Verification Required"
        size="small"
      >
        <p style={{ marginBottom: '16px' }}>
          Please verify your email address to continue. Check your inbox for a verification link from Auth0, then log in again.
        </p>
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button variant="primary" onClick={handleRefresh}>
            Log In
          </Button>
        </div>
      </Modal>
    </div>
  );
}

function App(): JSX.Element {
  return (
    <ErrorBoundary>
      <Auth0ProviderWithNavigate>
        <AppContent />
      </Auth0ProviderWithNavigate>
    </ErrorBoundary>
  );
}

export default App;
