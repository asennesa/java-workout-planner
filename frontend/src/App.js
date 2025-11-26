import { Routes, Route } from 'react-router-dom';
import { Auth0ProviderWithNavigate, ProtectedRoute } from './auth';
import { AuthNav } from './components';
import { useApiInitializer } from './hooks';
import { HomePage } from './pages/HomePage';
import { Dashboard } from './pages/Dashboard';
import { Profile } from './pages/Profile';
import './App.css';

/**
 * Inner app component that has access to Auth0 context.
 * Initializes the API service with Auth0 token getter.
 */
function AppContent() {
  useApiInitializer();

  return (
    <div className="App">
        <header className="App-header">
          <nav className="navbar">
            <div className="nav-brand">
              <a href="/">Workout Planner</a>
            </div>
            <AuthNav />
          </nav>
        </header>

        <main className="App-main">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route
              path="/dashboard"
              element={<ProtectedRoute component={Dashboard} />}
            />
            <Route
              path="/profile"
              element={<ProtectedRoute component={Profile} />}
            />
          </Routes>
        </main>
    </div>
  );
}

/**
 * Main App component - wraps everything with Auth0 provider.
 */
function App() {
  return (
    <Auth0ProviderWithNavigate>
      <AppContent />
    </Auth0ProviderWithNavigate>
  );
}

export default App;
