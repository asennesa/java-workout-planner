import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { apiService } from '../services/api';
import type { WorkoutSession, WorkoutAction } from '../types';
import {
  Button,
  Card,
  CardBody,
  StatusBadge,
  EmptyState,
  Alert,
} from '../components/ui';
import { CreateWorkoutModal } from '../components/workouts/CreateWorkoutModal';
import './Pages.css';

interface StatusAction {
  action: WorkoutAction;
  label: string;
  variant: 'primary' | 'secondary' | 'success' | 'danger' | 'ghost' | 'outline';
}

export const Dashboard = (): JSX.Element => {
  const { user } = useAuth0();
  const [workouts, setWorkouts] = useState<WorkoutSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  const fetchWorkouts = useCallback(async (): Promise<void> => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiService.getWorkouts();
      setWorkouts(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load workouts');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWorkouts();
  }, [fetchWorkouts]);

  const handleWorkoutCreated = (newWorkout: WorkoutSession): void => {
    setWorkouts((prev) => [newWorkout, ...prev]);
    setShowCreateModal(false);
  };

  const handleDeleteWorkout = async (sessionId: number): Promise<void> => {
    if (!window.confirm('Are you sure you want to delete this workout?')) return;
    try {
      await apiService.deleteWorkout(sessionId);
      setWorkouts((prev) => prev.filter((w) => w.sessionId !== sessionId));
    } catch (err) {
      setError('Failed to delete workout: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const handleStatusChange = async (sessionId: number, action: WorkoutAction): Promise<void> => {
    try {
      const updated = await apiService.updateWorkoutStatus(sessionId, action);
      setWorkouts((prev) => prev.map((w) => (w.sessionId === sessionId ? updated : w)));
    } catch (err) {
      setError('Failed to update status: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const getStatusActions = (status: string): StatusAction[] => {
    switch (status) {
      case 'PLANNED':
        return [{ action: 'START', label: 'Start', variant: 'success' }];
      case 'IN_PROGRESS':
        return [
          { action: 'PAUSE', label: 'Pause', variant: 'secondary' },
          { action: 'COMPLETE', label: 'Complete', variant: 'success' },
        ];
      case 'PAUSED':
        return [{ action: 'RESUME', label: 'Resume', variant: 'primary' }];
      default:
        return [];
    }
  };

  const formatDate = (dateString?: string): string => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  return (
    <div className="page dashboard-page">
      <div className="page-header">
        <div>
          <h1>My Workouts</h1>
          <p className="page-subtitle">Welcome back, {user?.name || user?.email}!</p>
        </div>
        <Button onClick={() => setShowCreateModal(true)}>+ New Workout</Button>
      </div>

      {error && (
        <Alert type="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="loading-spinner" />
          <p className="loading-text">Loading workouts...</p>
        </div>
      ) : workouts.length === 0 ? (
        <EmptyState
          icon="🏋️"
          title="No workouts yet"
          description="Create your first workout to start tracking your fitness journey!"
          action={<Button onClick={() => setShowCreateModal(true)}>Create Workout</Button>}
        />
      ) : (
        <div className="workout-grid">
          {workouts.map((workout) => (
            <Card key={workout.sessionId} className="workout-card">
              <CardBody>
                <div className="workout-card-header">
                  <h3>{workout.name}</h3>
                  <StatusBadge status={workout.status} />
                </div>
                {workout.description && (
                  <p className="workout-description">{workout.description}</p>
                )}
                <div className="workout-meta">
                  {workout.scheduledDate && (
                    <span>Scheduled: {formatDate(workout.scheduledDate)}</span>
                  )}
                  <span>Created: {formatDate(workout.createdAt)}</span>
                  {workout.workoutExercises && (
                    <span>{workout.workoutExercises.length} exercises</span>
                  )}
                </div>
                <div className="workout-actions">
                  <Link to={`/workouts/${workout.sessionId}`}>
                    <Button variant="outline" size="small">
                      View Details
                    </Button>
                  </Link>
                  {getStatusActions(workout.status).map(({ action, label, variant }) => (
                    <Button
                      key={action}
                      variant={variant}
                      size="small"
                      onClick={() => handleStatusChange(workout.sessionId, action)}
                    >
                      {label}
                    </Button>
                  ))}
                  <Button
                    variant="ghost"
                    size="small"
                    onClick={() => handleDeleteWorkout(workout.sessionId)}
                  >
                    Delete
                  </Button>
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}

      <CreateWorkoutModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleWorkoutCreated}
      />
    </div>
  );
};
