import { useState } from 'react';
import { apiService } from '../../services/api';
import { Modal, Input, TextArea, Button, Alert } from '../ui';

export const CreateWorkoutModal = ({ isOpen, onClose, onCreated }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setError('Workout name is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const workout = await apiService.createWorkout(formData);
      setFormData({ name: '', description: '' });
      onCreated(workout);
    } catch (err) {
      setError(err.message || 'Failed to create workout');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({ name: '', description: '' });
    setError(null);
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Create New Workout">
      <form onSubmit={handleSubmit}>
        {error && <Alert type="error">{error}</Alert>}

        <Input
          label="Workout Name"
          placeholder="e.g., Chest & Triceps Day"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          required
        />

        <TextArea
          label="Description (optional)"
          placeholder="Add notes about this workout..."
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
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
