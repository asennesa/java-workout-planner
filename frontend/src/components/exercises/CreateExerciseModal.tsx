import { useState, type FormEvent, type ChangeEvent } from 'react';
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

interface CreateExerciseFormData {
  name: string;
  description: string;
  type: ExerciseType | '';
  targetMuscleGroup: TargetMuscleGroup | '';
  difficultyLevel: DifficultyLevel | '';
}

interface CreateExerciseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreated: (exercise: Exercise) => void;
}

const INITIAL_FORM_STATE: CreateExerciseFormData = {
  name: '',
  description: '',
  type: '',
  targetMuscleGroup: '',
  difficultyLevel: '',
};

export const CreateExerciseModal = ({
  isOpen,
  onClose,
  onCreated,
}: CreateExerciseModalProps): JSX.Element => {
  const [formData, setFormData] = useState<CreateExerciseFormData>(INITIAL_FORM_STATE);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
      const exercise = await apiService.createExercise({
        name: formData.name,
        description: formData.description || undefined,
        type: formData.type,
        targetMuscleGroup: formData.targetMuscleGroup,
        difficultyLevel: formData.difficultyLevel,
      });
      resetForm();
      onCreated(exercise);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create exercise');
    } finally {
      setLoading(false);
    }
  };

  const handleChange =
    (field: keyof CreateExerciseFormData) =>
    (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>): void => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Add New Exercise" size="medium">
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
            Create Exercise
          </Button>
        </div>
      </form>
    </Modal>
  );
};
