import { useState, useEffect, useCallback } from 'react';
import { apiService, EXERCISE_TYPES, MUSCLE_GROUPS, DIFFICULTY_LEVELS } from '../services/api';
import { Button, Card, CardBody, StatusBadge, Select, Input, EmptyState, Alert } from '../components/ui';
import { CreateExerciseModal, EditExerciseModal, DeleteExerciseModal } from '../components/exercises';
import './Pages.css';

const INITIAL_FILTERS = {
  type: '',
  targetMuscleGroup: '',
  difficultyLevel: '',
  search: ''
};

export const Exercises = () => {
  // Data state
  const [exercises, setExercises] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filter state
  const [filters, setFilters] = useState(INITIAL_FILTERS);

  // Pagination state
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  // Modal state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedExercise, setSelectedExercise] = useState(null);

  // Fetch exercises based on filters
  const fetchExercises = useCallback(async (resetPage = false) => {
    try {
      setLoading(true);
      setError(null);
      const pageToFetch = resetPage ? 0 : page;

      let data;
      if (filters.search) {
        data = await apiService.searchExercises(filters.search);
      } else if (filters.type || filters.targetMuscleGroup || filters.difficultyLevel) {
        data = await apiService.filterExercises(
          filters.type || undefined,
          filters.targetMuscleGroup || undefined,
          filters.difficultyLevel || undefined,
          pageToFetch,
          20
        );
      } else {
        data = await apiService.getExercises(pageToFetch, 20);
      }

      const content = data.content || data || [];
      if (resetPage) {
        setExercises(content);
        setPage(0);
      } else {
        setExercises(prev => pageToFetch === 0 ? content : [...prev, ...content]);
      }
      setHasMore(Array.isArray(content) && content.length === 20);
    } catch (err) {
      setError(err.message || 'Failed to load exercises');
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  // Fetch on filter change (with debounce for search)
  useEffect(() => {
    const debounceTimer = setTimeout(() => {
      fetchExercises(true);
    }, filters.search ? 300 : 0);

    return () => clearTimeout(debounceTimer);
  }, [filters.type, filters.targetMuscleGroup, filters.difficultyLevel, filters.search]); // eslint-disable-line react-hooks/exhaustive-deps

  // Filter handlers
  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const clearFilters = () => {
    setFilters(INITIAL_FILTERS);
  };

  const hasActiveFilters = filters.type || filters.targetMuscleGroup || filters.difficultyLevel || filters.search;

  // Pagination handler
  const loadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchExercises(false);
  };

  // Modal handlers
  const handleEditClick = (exercise) => {
    setSelectedExercise(exercise);
    setShowEditModal(true);
  };

  const handleDeleteClick = (exercise) => {
    setSelectedExercise(exercise);
    setShowDeleteModal(true);
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
    setSelectedExercise(null);
  };

  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
    setSelectedExercise(null);
  };

  // CRUD callbacks
  const handleExerciseCreated = (newExercise) => {
    setExercises(prev => [newExercise, ...prev]);
    setShowCreateModal(false);
  };

  const handleExerciseUpdated = (updatedExercise) => {
    setExercises(prev =>
      prev.map(ex => ex.exerciseId === updatedExercise.exerciseId ? updatedExercise : ex)
    );
    handleCloseEditModal();
  };

  const handleExerciseDeleted = (exerciseId) => {
    setExercises(prev => prev.filter(ex => ex.exerciseId !== exerciseId));
    handleCloseDeleteModal();
  };

  return (
    <div className="page exercises-page">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1>Exercise Library</h1>
          <p className="page-subtitle">Browse and discover exercises for your workouts</p>
        </div>
        <Button onClick={() => setShowCreateModal(true)}>+ Add Exercise</Button>
      </div>

      {/* Filters Section */}
      <div className="filters-section">
        <Input
          placeholder="Search exercises..."
          value={filters.search}
          onChange={(e) => handleFilterChange('search', e.target.value)}
          className="search-input"
        />
        <Select
          value={filters.type}
          onChange={(e) => handleFilterChange('type', e.target.value)}
          options={EXERCISE_TYPES}
          placeholder="All Types"
        />
        <Select
          value={filters.targetMuscleGroup}
          onChange={(e) => handleFilterChange('targetMuscleGroup', e.target.value)}
          options={MUSCLE_GROUPS}
          placeholder="All Muscles"
        />
        <Select
          value={filters.difficultyLevel}
          onChange={(e) => handleFilterChange('difficultyLevel', e.target.value)}
          options={DIFFICULTY_LEVELS}
          placeholder="All Levels"
        />
        {hasActiveFilters && (
          <Button variant="ghost" size="small" onClick={clearFilters}>Clear</Button>
        )}
      </div>

      {/* Error Alert */}
      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      {/* Content */}
      {loading && exercises.length === 0 ? (
        <div className="loading-container">
          <div className="loading-spinner" />
          <p className="loading-text">Loading exercises...</p>
        </div>
      ) : exercises.length === 0 ? (
        <EmptyState
          icon="🔍"
          title="No exercises found"
          description={
            hasActiveFilters
              ? "Try adjusting your filters"
              : "Be the first to add an exercise to the library!"
          }
          action={<Button onClick={() => setShowCreateModal(true)}>Add Exercise</Button>}
        />
      ) : (
        <>
          {/* Exercise Grid */}
          <div className="exercise-grid">
            {exercises.map((exercise) => (
              <ExerciseCard
                key={exercise.exerciseId}
                exercise={exercise}
                onEdit={handleEditClick}
                onDelete={handleDeleteClick}
              />
            ))}
          </div>

          {/* Load More */}
          {hasMore && !loading && (
            <div className="load-more">
              <Button variant="outline" onClick={loadMore}>Load More</Button>
            </div>
          )}

          {/* Loading More Indicator */}
          {loading && exercises.length > 0 && (
            <div className="loading-more">
              <div className="loading-spinner" />
            </div>
          )}
        </>
      )}

      {/* Modals */}
      <CreateExerciseModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleExerciseCreated}
      />

      <EditExerciseModal
        isOpen={showEditModal}
        exercise={selectedExercise}
        onClose={handleCloseEditModal}
        onUpdated={handleExerciseUpdated}
      />

      <DeleteExerciseModal
        isOpen={showDeleteModal}
        exercise={selectedExercise}
        onClose={handleCloseDeleteModal}
        onDeleted={handleExerciseDeleted}
      />
    </div>
  );
};

/**
 * Exercise Card Component
 * Displays a single exercise with edit/delete actions
 */
const ExerciseCard = ({ exercise, onEdit, onDelete }) => {
  return (
    <Card className="exercise-library-card">
      <CardBody>
        <div className="exercise-card-header">
          <h3>{exercise.name}</h3>
          <div className="exercise-card-actions">
            <button
              className="icon-button"
              onClick={() => onEdit(exercise)}
              title="Edit exercise"
              aria-label="Edit exercise"
            >
              ✏️
            </button>
            <button
              className="icon-button delete"
              onClick={() => onDelete(exercise)}
              title="Delete exercise"
              aria-label="Delete exercise"
            >
              🗑️
            </button>
          </div>
        </div>
        <div className="exercise-badges">
          <StatusBadge status={exercise.type} />
          <StatusBadge status={exercise.difficultyLevel} />
        </div>
        <p className="muscle-group">
          {exercise.targetMuscleGroup?.replace(/_/g, ' ')}
        </p>
        {exercise.description && (
          <p className="exercise-description">{exercise.description}</p>
        )}
      </CardBody>
    </Card>
  );
};
