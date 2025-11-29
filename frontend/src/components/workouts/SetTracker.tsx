import { useState, useEffect, useCallback, type ChangeEvent } from 'react';
import { apiService } from '../../services/api';
import type { BaseSet, ExerciseType } from '../../types';
import { Button, Alert } from '../ui';
import './SetTracker.css';

interface SetTrackerProps {
  workoutExerciseId: number;
  exerciseType: ExerciseType;
}

interface ExtendedSet extends BaseSet {
  durationInSeconds?: number;
  intensity?: number;
}

export const SetTracker = ({
  workoutExerciseId,
  exerciseType,
}: SetTrackerProps): JSX.Element => {
  const [sets, setSets] = useState<ExtendedSet[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);

  const fetchSets = useCallback(async (): Promise<void> => {
    try {
      setLoading(true);
      let data: BaseSet[];
      switch (exerciseType) {
        case 'STRENGTH':
          data = await apiService.getStrengthSets(workoutExerciseId);
          break;
        case 'CARDIO':
          data = await apiService.getCardioSets(workoutExerciseId);
          break;
        case 'FLEXIBILITY':
          data = await apiService.getFlexibilitySets(workoutExerciseId);
          break;
        default:
          data = [];
      }
      setSets(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load sets');
    } finally {
      setLoading(false);
    }
  }, [workoutExerciseId, exerciseType]);

  useEffect(() => {
    fetchSets();
  }, [fetchSets]);

  const handleAddSet = async (): Promise<void> => {
    try {
      setAdding(true);
      const setNumber = sets.length + 1;
      let newSet: BaseSet;

      switch (exerciseType) {
        case 'STRENGTH':
          newSet = await apiService.createStrengthSet(workoutExerciseId, {
            setNumber,
            reps: 10,
            weight: 0,
            completed: false,
          });
          break;
        case 'CARDIO':
          newSet = await apiService.createCardioSet(workoutExerciseId, {
            setNumber,
            duration: 300,
            completed: false,
          });
          break;
        case 'FLEXIBILITY':
          newSet = await apiService.createFlexibilitySet(workoutExerciseId, {
            setNumber,
            holdDuration: 30,
            stretchIntensity: 5,
            completed: false,
          });
          break;
        default:
          return;
      }
      setSets([...sets, newSet]);
    } catch (err) {
      alert('Failed to add set: ' + (err instanceof Error ? err.message : 'Unknown error'));
    } finally {
      setAdding(false);
    }
  };

  const handleUpdateSet = async (
    setId: number,
    field: string,
    value: number | boolean
  ): Promise<void> => {
    const setIndex = sets.findIndex((s) => s.setId === setId);
    if (setIndex === -1) return;

    const updatedSet = { ...sets[setIndex], [field]: value };
    const newSets = [...sets];
    newSets[setIndex] = updatedSet;
    setSets(newSets);

    try {
      switch (exerciseType) {
        case 'STRENGTH':
          await apiService.updateStrengthSet(workoutExerciseId, setId, updatedSet);
          break;
        case 'CARDIO':
          await apiService.updateCardioSet(workoutExerciseId, setId, updatedSet);
          break;
        case 'FLEXIBILITY':
          await apiService.updateFlexibilitySet(workoutExerciseId, setId, updatedSet);
          break;
        default:
          break;
      }
    } catch (err) {
      console.error('Failed to update set:', err);
    }
  };

  const handleDeleteSet = async (setId: number): Promise<void> => {
    try {
      switch (exerciseType) {
        case 'STRENGTH':
          await apiService.deleteStrengthSet(workoutExerciseId, setId);
          break;
        case 'CARDIO':
          await apiService.deleteCardioSet(workoutExerciseId, setId);
          break;
        case 'FLEXIBILITY':
          await apiService.deleteFlexibilitySet(workoutExerciseId, setId);
          break;
        default:
          return;
      }
      setSets(sets.filter((s) => s.setId !== setId));
    } catch (err) {
      alert('Failed to delete set: ' + (err instanceof Error ? err.message : 'Unknown error'));
    }
  };

  const toggleCompleted = (setId: number): void => {
    const set = sets.find((s) => s.setId === setId);
    if (set) {
      handleUpdateSet(setId, 'completed', !set.completed);
    }
  };

  const handleInputChange = (setId: number, field: string) => (e: ChangeEvent<HTMLInputElement>) => {
    const value = field === 'weight' || field === 'distance'
      ? parseFloat(e.target.value) || 0
      : parseInt(e.target.value) || 0;
    handleUpdateSet(setId, field, value);
  };

  if (loading) {
    return <div className="set-tracker-loading">Loading sets...</div>;
  }

  if (error) {
    return <Alert type="error">{error}</Alert>;
  }

  return (
    <div className="set-tracker">
      {sets.length === 0 ? (
        <p className="no-sets">No sets recorded yet.</p>
      ) : (
        <div className="sets-list">
          {exerciseType === 'STRENGTH' && (
            <div className="sets-header">
              <span>Set</span>
              <span>Reps</span>
              <span>Weight (kg)</span>
              <span>Done</span>
              <span></span>
            </div>
          )}
          {exerciseType === 'CARDIO' && (
            <div className="sets-header">
              <span>Set</span>
              <span>Duration (sec)</span>
              <span>Distance</span>
              <span>Done</span>
              <span></span>
            </div>
          )}
          {exerciseType === 'FLEXIBILITY' && (
            <div className="sets-header">
              <span>Set</span>
              <span>Duration (sec)</span>
              <span>Intensity</span>
              <span>Done</span>
              <span></span>
            </div>
          )}

          {sets.map((set) => (
            <div key={set.setId} className={`set-row ${set.completed ? 'completed' : ''}`}>
              <span className="set-number">{set.setNumber}</span>

              {exerciseType === 'STRENGTH' && (
                <>
                  <input
                    type="number"
                    value={set.reps || ''}
                    onChange={handleInputChange(set.setId, 'reps')}
                    className="set-input"
                  />
                  <input
                    type="number"
                    step="0.5"
                    value={set.weight || ''}
                    onChange={handleInputChange(set.setId, 'weight')}
                    className="set-input"
                  />
                </>
              )}

              {exerciseType === 'CARDIO' && (
                <>
                  <input
                    type="number"
                    value={set.durationInSeconds || set.duration || ''}
                    onChange={handleInputChange(set.setId, 'durationInSeconds')}
                    className="set-input"
                  />
                  <input
                    type="number"
                    step="0.1"
                    value={set.distance || ''}
                    onChange={handleInputChange(set.setId, 'distance')}
                    className="set-input"
                    placeholder="km"
                  />
                </>
              )}

              {exerciseType === 'FLEXIBILITY' && (
                <>
                  <input
                    type="number"
                    value={set.durationInSeconds || set.holdDuration || ''}
                    onChange={handleInputChange(set.setId, 'durationInSeconds')}
                    className="set-input"
                  />
                  <input
                    type="number"
                    min="1"
                    max="10"
                    value={set.intensity || set.stretchIntensity || ''}
                    onChange={handleInputChange(set.setId, 'intensity')}
                    className="set-input"
                  />
                </>
              )}

              <button
                className={`complete-btn ${set.completed ? 'is-completed' : ''}`}
                onClick={() => toggleCompleted(set.setId)}
              >
                {set.completed ? '✓' : '○'}
              </button>

              <button className="delete-btn" onClick={() => handleDeleteSet(set.setId)}>
                ×
              </button>
            </div>
          ))}
        </div>
      )}

      <Button variant="outline" size="small" onClick={handleAddSet} loading={adding}>
        + Add Set
      </Button>
    </div>
  );
};
