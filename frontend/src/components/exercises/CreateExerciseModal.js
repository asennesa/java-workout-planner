import { useState } from 'react';
import PropTypes from 'prop-types';
import { apiService, EXERCISE_TYPES, MUSCLE_GROUPS, DIFFICULTY_LEVELS } from '../../services/api';
import { Modal, Input, TextArea, Select, Button, Alert } from '../ui';

const INITIAL_FORM_STATE = {
  name: '',
  description: '',
  type: '',
  targetMuscleGroup: '',
  difficultyLevel: ''
};

export const CreateExerciseModal = ({ isOpen, onClose, onCreated }) => {
  const [formData, setFormData] = useState(INITIAL_FORM_STATE);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const resetForm = () => {
    setFormData(INITIAL_FORM_STATE);
    setError(null);
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleSubmit = async (e) => {
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
      const exercise = await apiService.createExercise(formData);
      resetForm();
      onCreated(exercise);
    } catch (err) {
      setError(err.message || 'Failed to create exercise');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field) => (e) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
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
            options={EXERCISE_TYPES}
            placeholder="Select type"
          />
          <Select
            label="Difficulty *"
            value={formData.difficultyLevel}
            onChange={handleChange('difficultyLevel')}
            options={DIFFICULTY_LEVELS}
            placeholder="Select level"
          />
        </div>

        <Select
          label="Target Muscle Group *"
          value={formData.targetMuscleGroup}
          onChange={handleChange('targetMuscleGroup')}
          options={MUSCLE_GROUPS}
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

CreateExerciseModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onCreated: PropTypes.func.isRequired
};
