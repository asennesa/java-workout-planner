import { useState, useEffect, useCallback, type ChangeEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { apiService } from '../services/api';
import type { Exercise, WorkoutAction, WorkoutStatus } from '../types';
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  StatusBadge,
  Alert,
  EmptyState,
  Modal,
  Select,
} from '../components/ui';
import { SetTracker } from '../components/workouts/SetTracker';
import './Pages.css';

interface WorkoutExerciseExtended {
  workoutExerciseId: number;
  exerciseName: string;
  exerciseType: 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY';
  orderInWorkout: number;
}

interface WorkoutSessionExtended {
  sessionId: number;
  name: string;
  description?: string;
  status: WorkoutStatus;
  workoutExercises: WorkoutExerciseExtended[];
  actualDurationInMinutes?: number;
  createdAt: string;
  updatedAt: string;
}

interface StatusAction {
  action: WorkoutAction;
  label: string;
  variant: 'primary' | 'secondary' | 'success' | 'danger' | 'ghost' | 'outline';
}

export const WorkoutDetail = (): JSX.Element => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const [workout, setWorkout] = useState<WorkoutSessionExtended | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddExercise, setShowAddExercise] = useState(false);
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [selectedExerciseId, setSelectedExerciseId] = useState<string>('');
  const [addingExercise, setAddingExercise] = useState(false);
  const [expandedExercise, setExpandedExercise] = useState<number | null>(null);

  const fetchWorkout = useCallback(async (): Promise<void> => {
    if (!sessionId) return;
    try {
      setLoading(true);
      const data = await apiService.getWorkout(parseInt(sessionId));
      setWorkout(data as unknown as WorkoutSessionExtended);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load workout');
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const fetchExercises = useCallback(async (): Promise<void> => {
    try {
      const data = await apiService.getExercises(0, 100);
      setExercises(data.content || []);
    } catch (err) {
      console.error('Failed to fetch exercises:', err);
    }
  }, []);

  useEffect(() => {
    fetchWorkout();
    fetchExercises();
  }, [fetchWorkout, fetchExercises]);

  const handleStatusChange = async (action: WorkoutAction): Promise<void> => {
    if (!sessionId) return;
    try {
      const updated = await apiService.updateWorkoutStatus(parseInt(sessionId), action);
      setWorkout(updated as unknown as WorkoutSessionExtended);
    } catch (err) {
      alert('Failed to update status: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const handleAddExercise = async (): Promise<void> => {
    if (!selectedExerciseId || !sessionId) return;
    try {
      setAddingExercise(true);
      const order = (workout?.workoutExercises?.length || 0) + 1;
      await apiService.addExerciseToWorkout(parseInt(sessionId), {
        exerciseId: parseInt(selectedExerciseId),
        orderIndex: order,
      });
      await fetchWorkout();
      setShowAddExercise(false);
      setSelectedExerciseId('');
    } catch (err) {
      alert('Failed to add exercise: ' + (err instanceof Error ? err.message : 'Unknown error'));
    } finally {
      setAddingExercise(false);
    }
  };

  const handleRemoveExercise = async (workoutExerciseId: number): Promise<void> => {
    if (!window.confirm('Remove this exercise from the workout?')) return;
    try {
      await apiService.deleteWorkoutExercise(workoutExerciseId);
      await fetchWorkout();
    } catch (err) {
      alert('Failed to remove exercise: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const handleDelete = async (): Promise<void> => {
    if (!sessionId || !window.confirm('Are you sure you want to delete this workout?')) return;
    try {
      await apiService.deleteWorkout(parseInt(sessionId));
      navigate('/dashboard');
    } catch (err) {
      alert('Failed to delete workout: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const getStatusActions = (): StatusAction[] => {
    if (!workout) return [];
    switch (workout.status) {
      case 'PLANNED':
        return [{ action: 'START', label: 'Start Workout', variant: 'success' }];
      case 'IN_PROGRESS':
        return [
          { action: 'PAUSE', label: 'Pause', variant: 'secondary' },
          { action: 'COMPLETE', label: 'Complete Workout', variant: 'success' },
        ];
      case 'PAUSED':
        return [{ action: 'RESUME', label: 'Resume', variant: 'primary' }];
      default:
        return [];
    }
  };

  const formatDuration = (minutes?: number): string => {
    if (!minutes) return '-';
    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;
  };

  if (loading) {
    return (
      <div className="page">
        <div className="loading-container">
          <div className="loading-spinner" />
          <p className="loading-text">Loading workout...</p>
        </div>
      </div>
    );
  }

  if (error || !workout) {
    return (
      <div className="page">
        <Alert type="error">
          {error || 'Workout not found'}
          <Link to="/dashboard">
            <Button variant="outline" size="small">
              Back to Dashboard
            </Button>
          </Link>
        </Alert>
      </div>
    );
  }

  return (
    <div className="page workout-detail-page">
      <div className="page-header">
        <div>
          <Link to="/dashboard" className="back-link">
            &larr; Back to Workouts
          </Link>
          <h1>{workout.name}</h1>
          <div className="workout-detail-meta">
            <StatusBadge status={workout.status} />
            {workout.actualDurationInMinutes && (
              <span>Duration: {formatDuration(workout.actualDurationInMinutes)}</span>
            )}
          </div>
        </div>
        <div className="header-actions">
          {getStatusActions().map(({ action, label, variant }) => (
            <Button key={action} variant={variant} onClick={() => handleStatusChange(action)}>
              {label}
            </Button>
          ))}
          <Button variant="danger" onClick={handleDelete}>
            Delete
          </Button>
        </div>
      </div>

      {workout.description && (
        <Card className="workout-notes-card">
          <CardBody>
            <p>{workout.description}</p>
          </CardBody>
        </Card>
      )}

      <div className="exercises-section">
        <div className="section-header">
          <h2>Exercises ({workout.workoutExercises?.length || 0})</h2>
          <Button onClick={() => setShowAddExercise(true)} size="small">
            + Add Exercise
          </Button>
        </div>

        {!workout.workoutExercises?.length ? (
          <EmptyState
            icon="💪"
            title="No exercises yet"
            description="Add exercises to build your workout"
            action={<Button onClick={() => setShowAddExercise(true)}>Add Exercise</Button>}
          />
        ) : (
          <div className="exercise-list">
            {workout.workoutExercises.map((we) => (
              <Card key={we.workoutExerciseId} className="exercise-card">
                <CardHeader>
                  <div className="exercise-header-content">
                    <span className="exercise-order">{we.orderInWorkout}</span>
                    <div>
                      <h3>{we.exerciseName}</h3>
                      <StatusBadge status={we.exerciseType} />
                    </div>
                  </div>
                  <div className="exercise-header-actions">
                    <Button
                      variant="ghost"
                      size="small"
                      onClick={() =>
                        setExpandedExercise(
                          expandedExercise === we.workoutExerciseId ? null : we.workoutExerciseId
                        )
                      }
                    >
                      {expandedExercise === we.workoutExerciseId ? 'Hide Sets' : 'Track Sets'}
                    </Button>
                    <Button
                      variant="ghost"
                      size="small"
                      onClick={() => handleRemoveExercise(we.workoutExerciseId)}
                    >
                      Remove
                    </Button>
                  </div>
                </CardHeader>
                {expandedExercise === we.workoutExerciseId && (
                  <CardBody>
                    <SetTracker
                      workoutExerciseId={we.workoutExerciseId}
                      exerciseType={we.exerciseType}
                    />
                  </CardBody>
                )}
              </Card>
            ))}
          </div>
        )}
      </div>

      <Modal
        isOpen={showAddExercise}
        onClose={() => setShowAddExercise(false)}
        title="Add Exercise to Workout"
      >
        <Select
          label="Select Exercise"
          value={selectedExerciseId}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => setSelectedExerciseId(e.target.value)}
          options={exercises.map((ex) => ({
            value: String(ex.exerciseId),
            label: `${ex.name} (${ex.type})`,
          }))}
          placeholder="Choose an exercise..."
        />
        <div className="modal-actions">
          <Button variant="ghost" onClick={() => setShowAddExercise(false)}>
            Cancel
          </Button>
          <Button onClick={handleAddExercise} loading={addingExercise} disabled={!selectedExerciseId}>
            Add to Workout
          </Button>
        </div>
        <p className="modal-hint">
          Don't see the exercise you need? <Link to="/exercises">Browse or create exercises</Link>
        </p>
      </Modal>
    </div>
  );
};
