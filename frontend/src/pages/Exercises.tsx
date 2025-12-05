import { useState, useEffect, useCallback, type ChangeEvent } from 'react';
import { apiService } from '../services/api';
import {
  EXERCISE_TYPES,
  MUSCLE_GROUPS,
  DIFFICULTY_LEVELS,
  type Exercise,
  type ExerciseType,
  type TargetMuscleGroup,
  type DifficultyLevel,
} from '../types';
import {
  Card,
  CardBody,
  StatusBadge,
  Select,
  Input,
  EmptyState,
  Alert,
  Button,
} from '../components/ui';
import './Pages.css';

interface Filters {
  type: ExerciseType | '';
  targetMuscleGroup: TargetMuscleGroup | '';
  difficultyLevel: DifficultyLevel | '';
  search: string;
}

const INITIAL_FILTERS: Filters = {
  type: '',
  targetMuscleGroup: '',
  difficultyLevel: '',
  search: '',
};

interface ExerciseCardProps {
  exercise: Exercise;
}

const ExerciseCard = ({ exercise }: ExerciseCardProps): JSX.Element => {
  return (
    <Card className="exercise-library-card">
      <CardBody>
        <div className="exercise-card-header">
          <h3>{exercise.name}</h3>
        </div>
        <div className="exercise-badges">
          <StatusBadge status={exercise.type} />
          <StatusBadge status={exercise.difficultyLevel} />
        </div>
        <p className="muscle-group">{exercise.targetMuscleGroup?.replace(/_/g, ' ')}</p>
        {exercise.description && <p className="exercise-description">{exercise.description}</p>}
      </CardBody>
    </Card>
  );
};

export const Exercises = (): JSX.Element => {
  // Data state
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [filters, setFilters] = useState<Filters>(INITIAL_FILTERS);

  // Pagination state
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  // Fetch exercises based on filters
  const fetchExercises = useCallback(
    async (resetPage = false): Promise<void> => {
      try {
        setLoading(true);
        setError(null);
        const pageToFetch = resetPage ? 0 : page;

        let data: Exercise[] | { content: Exercise[] };
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

        const content = Array.isArray(data) ? data : data.content || [];
        if (resetPage) {
          setExercises(content);
          setPage(0);
        } else {
          setExercises((prev) => (pageToFetch === 0 ? content : [...prev, ...content]));
        }
        setHasMore(Array.isArray(content) && content.length === 20);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load exercises');
      } finally {
        setLoading(false);
      }
    },
    [filters, page]
  );

  // Fetch on filter change (with debounce for search)
  useEffect(() => {
    const debounceTimer = setTimeout(
      () => {
        fetchExercises(true);
      },
      filters.search ? 300 : 0
    );

    return () => clearTimeout(debounceTimer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.type, filters.targetMuscleGroup, filters.difficultyLevel, filters.search]);

  // Filter handlers
  const handleFilterChange = (key: keyof Filters, value: string): void => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const clearFilters = (): void => {
    setFilters(INITIAL_FILTERS);
  };

  const hasActiveFilters =
    filters.type || filters.targetMuscleGroup || filters.difficultyLevel || filters.search;

  // Pagination handler
  const loadMore = (): void => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchExercises(false);
  };

  return (
    <div className="page exercises-page">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1>Exercise Library</h1>
          <p className="page-subtitle">Browse and discover exercises for your workouts</p>
        </div>
      </div>

      {/* Filters Section */}
      <div className="filters-section">
        <Input
          placeholder="Search exercises..."
          value={filters.search}
          onChange={(e: ChangeEvent<HTMLInputElement>) => handleFilterChange('search', e.target.value)}
          className="search-input"
        />
        <Select
          value={filters.type}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => handleFilterChange('type', e.target.value)}
          options={[...EXERCISE_TYPES]}
          placeholder="All Types"
        />
        <Select
          value={filters.targetMuscleGroup}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => handleFilterChange('targetMuscleGroup', e.target.value)}
          options={[...MUSCLE_GROUPS]}
          placeholder="All Muscles"
        />
        <Select
          value={filters.difficultyLevel}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => handleFilterChange('difficultyLevel', e.target.value)}
          options={[...DIFFICULTY_LEVELS]}
          placeholder="All Levels"
        />
        {hasActiveFilters && (
          <Button variant="ghost" size="small" onClick={clearFilters}>
            Clear
          </Button>
        )}
      </div>

      {/* Error Alert */}
      {error && (
        <Alert type="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

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
              ? 'Try adjusting your filters'
              : 'No exercises available in the library yet.'
          }
        />
      ) : (
        <>
          {/* Exercise Grid */}
          <div className="exercise-grid">
            {exercises.map((exercise) => (
              <ExerciseCard
                key={exercise.exerciseId}
                exercise={exercise}
              />
            ))}
          </div>

          {/* Load More */}
          {hasMore && !loading && (
            <div className="load-more">
              <Button variant="outline" onClick={loadMore}>
                Load More
              </Button>
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
    </div>
  );
};
