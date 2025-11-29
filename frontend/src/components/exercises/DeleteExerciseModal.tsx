import { useState } from 'react';
import { apiService } from '../../services/api';
import type { Exercise } from '../../types';
import { Modal, Button, Alert } from '../ui';

interface DeleteExerciseModalProps {
  isOpen: boolean;
  exercise: Exercise | null;
  onClose: () => void;
  onDeleted: (exerciseId: number) => void;
}

export const DeleteExerciseModal = ({
  isOpen,
  exercise,
  onClose,
  onDeleted,
}: DeleteExerciseModalProps): JSX.Element | null => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClose = (): void => {
    setError(null);
    onClose();
  };

  const handleDelete = async (): Promise<void> => {
    if (!exercise) return;

    try {
      setLoading(true);
      setError(null);
      await apiService.deleteExercise(exercise.exerciseId);
      onDeleted(exercise.exerciseId);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete exercise');
    } finally {
      setLoading(false);
    }
  };

  if (!exercise) return null;

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Delete Exercise" size="small">
      <div className="delete-confirmation">
        {error && <Alert type="error">{error}</Alert>}

        <p>
          Are you sure you want to delete <strong>{exercise.name}</strong>?
        </p>
        <p className="warning-text">This action cannot be undone.</p>

        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={handleClose}>
            Cancel
          </Button>
          <Button type="button" variant="danger" onClick={handleDelete} loading={loading}>
            Delete Exercise
          </Button>
        </div>
      </div>
    </Modal>
  );
};
