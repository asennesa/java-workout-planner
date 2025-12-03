import { useState, useEffect, useCallback, useMemo } from 'react';
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
  scheduledDate?: string;
  startedAt?: string;
  completedAt?: string;
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
  const [selectedExerciseIds, setSelectedExerciseIds] = useState<Set<number>>(new Set());
  const [exerciseSearch, setExerciseSearch] = useState('');
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

  const handleAddExercises = async (): Promise<void> => {
    if (selectedExerciseIds.size === 0 || !sessionId) return;
    try {
      setAddingExercise(true);
      let order = (workout?.workoutExercises?.length || 0) + 1;

      for (const exerciseId of selectedExerciseIds) {
        await apiService.addExerciseToWorkout(parseInt(sessionId), {
          exerciseId,
          orderInWorkout: order++,
        });
      }

      await fetchWorkout();
      setShowAddExercise(false);
      setSelectedExerciseIds(new Set());
      setExerciseSearch('');
    } catch (err) {
      alert('Failed to add exercises: ' + (err instanceof Error ? err.message : 'Unknown error'));
    } finally {
      setAddingExercise(false);
    }
  };

  const toggleExerciseSelection = (exerciseId: number): void => {
    setSelectedExerciseIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(exerciseId)) {
        newSet.delete(exerciseId);
      } else {
        newSet.add(exerciseId);
      }
      return newSet;
    });
  };

  // Filter exercises based on search and exclude already added ones
  const filteredExercises = useMemo(() => {
    const alreadyAddedIds = new Set(
      workout?.workoutExercises?.map(we => {
        // Find the exercise ID from the exercise list by name
        const ex = exercises.find(e => e.name === we.exerciseName);
        return ex?.exerciseId;
      }).filter(Boolean)
    );

    return exercises.filter(ex => {
      // Exclude already added exercises
      if (alreadyAddedIds.has(ex.exerciseId)) return false;

      // Filter by search term
      if (!exerciseSearch) return true;
      const searchLower = exerciseSearch.toLowerCase();
      return (
        ex.name.toLowerCase().includes(searchLower) ||
        ex.type.toLowerCase().includes(searchLower) ||
        ex.muscleGroup?.toLowerCase().includes(searchLower)
      );
    });
  }, [exercises, workout?.workoutExercises, exerciseSearch]);

  const handleRemoveExercise = async (workoutExerciseId: number): Promise<void> => {
    if (!window.confirm('Remove this exercise from the workout?')) return;
    try {
      await apiService.deleteWorkoutExercise(workoutExerciseId);
      await fetchWorkout();
    } catch (err) {
      alert('Failed to remove exercise: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const handleMoveExercise = async (workoutExerciseId: number, direction: 'up' | 'down'): Promise<void> => {
    if (!workout?.workoutExercises) return;

    const currentIndex = workout.workoutExercises.findIndex(we => we.workoutExerciseId === workoutExerciseId);
    if (currentIndex === -1) return;

    const newIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;
    if (newIndex < 0 || newIndex >= workout.workoutExercises.length) return;

    const currentExercise = workout.workoutExercises[currentIndex];
    const swapExercise = workout.workoutExercises[newIndex];

    try {
      // Swap the order values
      await apiService.updateWorkoutExercise(currentExercise.workoutExerciseId, {
        orderInWorkout: swapExercise.orderInWorkout,
      });
      await apiService.updateWorkoutExercise(swapExercise.workoutExerciseId, {
        orderInWorkout: currentExercise.orderInWorkout,
      });
      await fetchWorkout();
    } catch (err) {
      alert('Failed to reorder exercise: ' + (err instanceof Error ? err.message : 'Unknown error'));
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

  const formatDate = (dateString?: string): string => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const formatDateTime = (dateString?: string): string => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
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
            {workout.scheduledDate && (
              <span>Scheduled: {formatDate(workout.scheduledDate)}</span>
            )}
            {workout.startedAt && (
              <span>Started: {formatDateTime(workout.startedAt)}</span>
            )}
            {workout.completedAt && (
              <span>Completed: {formatDateTime(workout.completedAt)}</span>
            )}
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
            {workout.workoutExercises.map((we, index) => (
              <Card key={we.workoutExerciseId} className="exercise-card">
                <CardHeader>
                  <div className="exercise-header-content">
                    <div className="exercise-reorder-buttons">
                      <button
                        className="reorder-btn"
                        onClick={() => handleMoveExercise(we.workoutExerciseId, 'up')}
                        disabled={index === 0}
                        title="Move up"
                      >
                        ▲
                      </button>
                      <button
                        className="reorder-btn"
                        onClick={() => handleMoveExercise(we.workoutExerciseId, 'down')}
                        disabled={index === workout.workoutExercises.length - 1}
                        title="Move down"
                      >
                        ▼
                      </button>
                    </div>
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
        onClose={() => {
          setShowAddExercise(false);
          setSelectedExerciseIds(new Set());
          setExerciseSearch('');
        }}
        title="Add Exercises to Workout"
        size="large"
      >
        <div className="exercise-picker">
          <div className="exercise-picker-header">
            <input
              type="text"
              className="form-input exercise-search"
              placeholder="Search exercises..."
              value={exerciseSearch}
              onChange={(e) => setExerciseSearch(e.target.value)}
              autoFocus
            />
            {selectedExerciseIds.size > 0 && (
              <span className="selected-count">
                {selectedExerciseIds.size} selected
              </span>
            )}
          </div>

          <div className="exercise-picker-list">
            {filteredExercises.length === 0 ? (
              <div className="exercise-picker-empty">
                {exerciseSearch ? 'No exercises match your search' : 'No exercises available'}
              </div>
            ) : (
              filteredExercises.map((ex) => (
                <div
                  key={ex.exerciseId}
                  className={`exercise-picker-item ${selectedExerciseIds.has(ex.exerciseId) ? 'selected' : ''}`}
                  onClick={() => toggleExerciseSelection(ex.exerciseId)}
                >
                  <div className="exercise-picker-checkbox">
                    {selectedExerciseIds.has(ex.exerciseId) ? '✓' : ''}
                  </div>
                  <div className="exercise-picker-info">
                    <span className="exercise-picker-name">{ex.name}</span>
                    <span className="exercise-picker-meta">
                      <StatusBadge status={ex.type} />
                      {ex.muscleGroup && <span className="muscle-group">{ex.muscleGroup}</span>}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="modal-actions">
          <Button variant="ghost" onClick={() => {
            setShowAddExercise(false);
            setSelectedExerciseIds(new Set());
            setExerciseSearch('');
          }}>
            Cancel
          </Button>
          <Button
            onClick={handleAddExercises}
            loading={addingExercise}
            disabled={selectedExerciseIds.size === 0}
          >
            Add {selectedExerciseIds.size > 0 ? `${selectedExerciseIds.size} ` : ''}Exercise{selectedExerciseIds.size !== 1 ? 's' : ''}
          </Button>
        </div>
      </Modal>
    </div>
  );
};
