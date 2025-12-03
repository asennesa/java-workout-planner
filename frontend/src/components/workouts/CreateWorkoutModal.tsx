import { useState, type FormEvent, type ChangeEvent } from 'react';
import { apiService } from '../../services/api';
import type { WorkoutSession } from '../../types';
import { Modal, Input, TextArea, Button, Alert } from '../ui';

interface CreateWorkoutFormData {
  name: string;
  description: string;
  scheduledDate: string;
}

interface CreateWorkoutModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreated: (workout: WorkoutSession) => void;
}

export const CreateWorkoutModal = ({
  isOpen,
  onClose,
  onCreated,
}: CreateWorkoutModalProps): JSX.Element => {
  const [formData, setFormData] = useState<CreateWorkoutFormData>({
    name: '',
    description: '',
    scheduledDate: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setError('Workout name is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const workout = await apiService.createWorkout({
        name: formData.name,
        description: formData.description || undefined,
        scheduledDate: formData.scheduledDate || undefined,
      });
      setFormData({ name: '', description: '', scheduledDate: '' });
      onCreated(workout);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create workout');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = (): void => {
    setFormData({ name: '', description: '', scheduledDate: '' });
    setError(null);
    onClose();
  };

  const handleChange =
    (field: keyof CreateWorkoutFormData) =>
    (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  // Get today's date in YYYY-MM-DD format for min attribute
  const today = new Date().toISOString().split('T')[0];

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Create New Workout">
      <form onSubmit={handleSubmit}>
        {error && <Alert type="error">{error}</Alert>}

        <Input
          label="Workout Name"
          placeholder="e.g., Chest & Triceps Day"
          value={formData.name}
          onChange={handleChange('name')}
          required
        />

        <div className="form-group">
          <label className="form-label">Scheduled Date (optional)</label>
          <input
            type="date"
            className="form-input"
            value={formData.scheduledDate}
            onChange={(e) => setFormData((prev) => ({ ...prev, scheduledDate: e.target.value }))}
            min={today}
          />
          {formData.scheduledDate && (
            <span className="form-hint">
              Scheduled for: {new Date(formData.scheduledDate + 'T00:00:00').toLocaleDateString('en-US', {
                weekday: 'long',
                month: 'long',
                day: 'numeric',
                year: 'numeric',
              })}
            </span>
          )}
        </div>

        <TextArea
          label="Description (optional)"
          placeholder="Add notes about this workout..."
          value={formData.description}
          onChange={handleChange('description')}
          rows={3}
        />

        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={handleClose}>
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Create Workout
          </Button>
        </div>
      </form>
    </Modal>
  );
};
