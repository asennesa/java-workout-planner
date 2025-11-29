import { useState, useEffect, useCallback } from 'react';
import { apiService } from '../../services/api';
import { Button, Alert } from '../ui';
import './SetTracker.css';

export const SetTracker = ({ workoutExerciseId, exerciseType }) => {
  const [sets, setSets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [adding, setAdding] = useState(false);

  const fetchSets = useCallback(async () => {
    try {
      setLoading(true);
      let data;
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
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [workoutExerciseId, exerciseType]);

  useEffect(() => {
    fetchSets();
  }, [fetchSets]);

  const handleAddSet = async () => {
    try {
      setAdding(true);
      const setNumber = sets.length + 1;
      let newSet;

      switch (exerciseType) {
        case 'STRENGTH':
          newSet = await apiService.createStrengthSet(workoutExerciseId, {
            setNumber,
            reps: 10,
            weight: 0,
            completed: false
          });
          break;
        case 'CARDIO':
          newSet = await apiService.createCardioSet(workoutExerciseId, {
            setNumber,
            durationInSeconds: 300,
            completed: false
          });
          break;
        case 'FLEXIBILITY':
          newSet = await apiService.createFlexibilitySet(workoutExerciseId, {
            setNumber,
            durationInSeconds: 30,
            stretchType: 'static',
            intensity: 5,
            completed: false
          });
          break;
        default:
          return;
      }
      setSets([...sets, newSet]);
    } catch (err) {
      alert('Failed to add set: ' + err.message);
    } finally {
      setAdding(false);
    }
  };

  const handleUpdateSet = async (setId, field, value) => {
    const setIndex = sets.findIndex(s => s.setId === setId);
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

  const handleDeleteSet = async (setId) => {
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
      setSets(sets.filter(s => s.setId !== setId));
    } catch (err) {
      alert('Failed to delete set: ' + err.message);
    }
  };

  const toggleCompleted = (setId) => {
    const set = sets.find(s => s.setId === setId);
    if (set) {
      handleUpdateSet(setId, 'completed', !set.completed);
    }
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
                    onChange={(e) => handleUpdateSet(set.setId, 'reps', parseInt(e.target.value) || 0)}
                    className="set-input"
                  />
                  <input
                    type="number"
                    step="0.5"
                    value={set.weight || ''}
                    onChange={(e) => handleUpdateSet(set.setId, 'weight', parseFloat(e.target.value) || 0)}
                    className="set-input"
                  />
                </>
              )}

              {exerciseType === 'CARDIO' && (
                <>
                  <input
                    type="number"
                    value={set.durationInSeconds || ''}
                    onChange={(e) => handleUpdateSet(set.setId, 'durationInSeconds', parseInt(e.target.value) || 0)}
                    className="set-input"
                  />
                  <input
                    type="number"
                    step="0.1"
                    value={set.distance || ''}
                    onChange={(e) => handleUpdateSet(set.setId, 'distance', parseFloat(e.target.value) || 0)}
                    className="set-input"
                    placeholder="km"
                  />
                </>
              )}

              {exerciseType === 'FLEXIBILITY' && (
                <>
                  <input
                    type="number"
                    value={set.durationInSeconds || ''}
                    onChange={(e) => handleUpdateSet(set.setId, 'durationInSeconds', parseInt(e.target.value) || 0)}
                    className="set-input"
                  />
                  <input
                    type="number"
                    min="1"
                    max="10"
                    value={set.intensity || ''}
                    onChange={(e) => handleUpdateSet(set.setId, 'intensity', parseInt(e.target.value) || 1)}
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

              <button className="delete-btn" onClick={() => handleDeleteSet(set.setId)}>×</button>
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
