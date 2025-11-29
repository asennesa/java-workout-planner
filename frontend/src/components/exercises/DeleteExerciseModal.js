import { useState } from 'react';
import PropTypes from 'prop-types';
import { apiService } from '../../services/api';
import { Modal, Button, Alert } from '../ui';

export const DeleteExerciseModal = ({ isOpen, exercise, onClose, onDeleted }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleClose = () => {
    setError(null);
    onClose();
  };

  const handleDelete = async () => {
    try {
      setLoading(true);
      setError(null);
      await apiService.deleteExercise(exercise.exerciseId);
      onDeleted(exercise.exerciseId);
    } catch (err) {
      setError(err.message || 'Failed to delete exercise');
    } finally {
      setLoading(false);
    }
  };

  if (!exercise) return null;

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Delete Exercise" size="small">
      <div className="delete-confirmation">
        {error && <Alert type="error">{error}</Alert>}

        <p>Are you sure you want to delete <strong>{exercise.name}</strong>?</p>
        <p className="warning-text">This action cannot be undone.</p>

        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={handleClose}>
            Cancel
          </Button>
          <Button
            type="button"
            variant="danger"
            onClick={handleDelete}
            loading={loading}
          >
            Delete Exercise
          </Button>
        </div>
      </div>
    </Modal>
  );
};

DeleteExerciseModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  exercise: PropTypes.shape({
    exerciseId: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired
  }),
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired
};

DeleteExerciseModal.defaultProps = {
  exercise: null
};
