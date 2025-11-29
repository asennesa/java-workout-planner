// ============ Enums ============

export type ExerciseType = 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY';

export type TargetMuscleGroup =
  | 'CHEST'
  | 'BACK'
  | 'SHOULDERS'
  | 'BICEPS'
  | 'TRICEPS'
  | 'FOREARMS'
  | 'QUADRICEPS'
  | 'HAMSTRINGS'
  | 'GLUTES'
  | 'CALVES'
  | 'CORE'
  | 'FULL_BODY';

export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export type WorkoutStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'PAUSED';

export type WorkoutAction = 'START' | 'PAUSE' | 'RESUME' | 'COMPLETE' | 'CANCEL';

// ============ API Response Types ============

export interface User {
  userId: number;
  auth0Id: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

export interface Exercise {
  exerciseId: number;
  name: string;
  description?: string;
  type: ExerciseType;
  targetMuscleGroup: TargetMuscleGroup;
  difficultyLevel: DifficultyLevel;
  instructions?: string;
  equipmentRequired?: string;
  videoUrl?: string;
  imageUrl?: string;
}

export interface WorkoutExercise {
  workoutExerciseId: number;
  exercise: Exercise;
  orderIndex: number;
  notes?: string;
  targetSets?: number;
  targetReps?: number;
  targetWeight?: number;
  targetDuration?: number;
  targetDistance?: number;
  sets?: BaseSet[];
}

export interface BaseSet {
  setId: number;
  setNumber: number;
  completed: boolean;
  notes?: string;
  // Strength-specific
  reps?: number;
  weight?: number;
  // Cardio-specific
  duration?: number;
  distance?: number;
  avgHeartRate?: number;
  caloriesBurned?: number;
  // Flexibility-specific
  holdDuration?: number;
  stretchIntensity?: number;
}

export interface WorkoutSession {
  sessionId: number;
  name: string;
  description?: string;
  status: WorkoutStatus;
  scheduledDate?: string;
  startTime?: string;
  endTime?: string;
  notes?: string;
  workoutExercises: WorkoutExercise[];
  createdAt: string;
  updatedAt: string;
}

// ============ Request Types ============

export interface CreateExerciseRequest {
  name: string;
  description?: string;
  type: ExerciseType;
  targetMuscleGroup: TargetMuscleGroup;
  difficultyLevel: DifficultyLevel;
  instructions?: string;
  equipmentRequired?: string;
  videoUrl?: string;
  imageUrl?: string;
}

export interface CreateWorkoutRequest {
  name: string;
  description?: string;
  scheduledDate?: string;
  notes?: string;
}

export interface UpdateWorkoutRequest {
  name?: string;
  description?: string;
  scheduledDate?: string;
  notes?: string;
}

export interface CreateWorkoutExerciseRequest {
  exerciseId: number;
  orderIndex?: number;
  notes?: string;
  targetSets?: number;
  targetReps?: number;
  targetWeight?: number;
  targetDuration?: number;
  targetDistance?: number;
}

export interface CreateStrengthSetRequest {
  setNumber: number;
  reps: number;
  weight: number;
  completed?: boolean;
  notes?: string;
}

export interface CreateCardioSetRequest {
  setNumber: number;
  duration: number;
  distance?: number;
  avgHeartRate?: number;
  caloriesBurned?: number;
  completed?: boolean;
  notes?: string;
}

export interface CreateFlexibilitySetRequest {
  setNumber: number;
  holdDuration: number;
  stretchIntensity?: number;
  completed?: boolean;
  notes?: string;
}

export interface UpdateSetRequest {
  reps?: number;
  weight?: number;
  duration?: number;
  distance?: number;
  avgHeartRate?: number;
  caloriesBurned?: number;
  holdDuration?: number;
  stretchIntensity?: number;
  completed?: boolean;
  notes?: string;
}

// ============ Pagination Types ============

export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export interface ExistenceCheckResponse {
  exists: boolean;
}

// ============ Constants ============

export const EXERCISE_TYPES: readonly ExerciseType[] = ['STRENGTH', 'CARDIO', 'FLEXIBILITY'] as const;

export const MUSCLE_GROUPS: readonly TargetMuscleGroup[] = [
  'CHEST',
  'BACK',
  'SHOULDERS',
  'BICEPS',
  'TRICEPS',
  'FOREARMS',
  'QUADRICEPS',
  'HAMSTRINGS',
  'GLUTES',
  'CALVES',
  'CORE',
  'FULL_BODY',
] as const;

export const DIFFICULTY_LEVELS: readonly DifficultyLevel[] = [
  'BEGINNER',
  'INTERMEDIATE',
  'ADVANCED',
] as const;

export const WORKOUT_STATUSES: readonly WorkoutStatus[] = [
  'PLANNED',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
  'PAUSED',
] as const;

export const WORKOUT_ACTIONS: readonly WorkoutAction[] = [
  'START',
  'PAUSE',
  'RESUME',
  'COMPLETE',
  'CANCEL',
] as const;
