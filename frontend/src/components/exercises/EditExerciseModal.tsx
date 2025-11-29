import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react';
import { apiService } from '../../services/api';
import {
  EXERCISE_TYPES,
  MUSCLE_GROUPS,
  DIFFICULTY_LEVELS,
  type Exercise,
  type ExerciseType,
  type TargetMuscleGroup,
  type DifficultyLevel,
} from '../../types';
import { Modal, Input, TextArea, Select, Button, Alert } from '../ui';

interface EditExerciseFormData {
  name: string;
  description: string;
  type: ExerciseType | '';
  targetMuscleGroup: TargetMuscleGroup | '';
  difficultyLevel: DifficultyLevel | '';
}

interface EditExerciseModalProps {
  isOpen: boolean;
  exercise: Exercise | null;
  onClose: () => void;
  onUpdated: (exercise: Exercise) => void;
}

const INITIAL_FORM_STATE: EditExerciseFormData = {
  name: '',
  description: '',
  type: '',
  targetMuscleGroup: '',
  difficultyLevel: '',
};

export const EditExerciseModal = ({
  isOpen,
  exercise,
  onClose,
  onUpdated,
}: EditExerciseModalProps): JSX.Element | null => {
  const [formData, setFormData] = useState<EditExerciseFormData>(INITIAL_FORM_STATE);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Populate form when exercise changes
  useEffect(() => {
    if (exercise) {
      setFormData({
        name: exercise.name || '',
        description: exercise.description || '',
        type: exercise.type || '',
        targetMuscleGroup: exercise.targetMuscleGroup || '',
        difficultyLevel: exercise.difficultyLevel || '',
      });
    }
  }, [exercise]);

  const resetForm = (): void => {
    setFormData(INITIAL_FORM_STATE);
    setError(null);
  };

  const handleClose = (): void => {
    resetForm();
    onClose();
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();

    if (!exercise) return;

    if (!formData.name.trim()) {
      setError('Exercise name is required');
      return;
    }
    if (!formData.type) {
      setError('Exercise type is required');
      return;
    }
    if (!formData.targetMuscleGroup) {
      setError('Target muscle group is required');
      return;
    }
    if (!formData.difficultyLevel) {
      setError('Difficulty level is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const updatedExercise = await apiService.updateExercise(exercise.exerciseId, {
        name: formData.name,
        description: formData.description || undefined,
        type: formData.type,
        targetMuscleGroup: formData.targetMuscleGroup,
        difficultyLevel: formData.difficultyLevel,
      });
      onUpdated(updatedExercise);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update exercise');
    } finally {
      setLoading(false);
    }
  };

  const handleChange =
    (field: keyof EditExerciseFormData) =>
    (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>): void => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  if (!exercise) return null;

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Edit Exercise" size="medium">
      <form onSubmit={handleSubmit}>
        {error && <Alert type="error">{error}</Alert>}

        <Input
          label="Exercise Name *"
          placeholder="e.g., Bench Press"
          value={formData.name}
          onChange={handleChange('name')}
          required
        />

        <div className="form-row">
          <Select
            label="Type *"
            value={formData.type}
            onChange={handleChange('type')}
            options={[...EXERCISE_TYPES]}
            placeholder="Select type"
          />
          <Select
            label="Difficulty *"
            value={formData.difficultyLevel}
            onChange={handleChange('difficultyLevel')}
            options={[...DIFFICULTY_LEVELS]}
            placeholder="Select level"
          />
        </div>

        <Select
          label="Target Muscle Group *"
          value={formData.targetMuscleGroup}
          onChange={handleChange('targetMuscleGroup')}
          options={[...MUSCLE_GROUPS]}
          placeholder="Select muscle group"
        />

        <TextArea
          label="Description"
          placeholder="How to perform this exercise..."
          value={formData.description}
          onChange={handleChange('description')}
          rows={3}
        />

        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={handleClose}>
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Save Changes
          </Button>
        </div>
      </form>
    </Modal>
  );
};
