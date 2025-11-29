import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import type {
  User,
  Exercise,
  WorkoutSession,
  WorkoutExercise,
  BaseSet,
  PagedResponse,
  ExistenceCheckResponse,
  CreateExerciseRequest,
  CreateWorkoutRequest,
  UpdateWorkoutRequest,
  CreateWorkoutExerciseRequest,
  CreateStrengthSetRequest,
  CreateCardioSetRequest,
  CreateFlexibilitySetRequest,
  UpdateSetRequest,
  ExerciseType,
  TargetMuscleGroup,
  DifficultyLevel,
  WorkoutAction,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api/v1';

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Type for getAccessTokenSilently from Auth0
type GetAccessTokenSilently = () => Promise<string>;

// Store for the getAccessTokenSilently function
let getAccessToken: GetAccessTokenSilently | null = null;

/**
 * Initialize the API service with Auth0's getAccessTokenSilently function.
 */
export const initializeApi = (getAccessTokenSilently: GetAccessTokenSilently): void => {
  getAccessToken = getAccessTokenSilently;
};

// Request interceptor to add auth token
api.interceptors.request.use(
  async (config: InternalAxiosRequestConfig): Promise<InternalAxiosRequestConfig> => {
    if (getAccessToken) {
      try {
        const token = await getAccessToken();
        config.headers.Authorization = `Bearer ${token}`;
      } catch (error) {
        console.error('Failed to get access token:', error);
      }
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError<{ message?: string; error?: string }>) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        console.error('Unauthorized - token may be expired');
      } else if (status === 403) {
        console.error('Forbidden - insufficient permissions');
      }
      error.message = data?.message || data?.error || `Request failed with status ${status}`;
    } else if (error.request) {
      error.message = 'Network error - please check your connection';
    }
    return Promise.reject(error);
  }
);

/**
 * API service methods for the Workout Planner backend.
 */
export const apiService = {
  // ============ User Endpoints ============

  getCurrentUser: (): Promise<User> => api.get('/users/me').then((res) => res.data),

  updateCurrentUser: (userData: Partial<User>): Promise<User> =>
    api.put('/users/me', userData).then((res) => res.data),

  checkUsername: (username: string): Promise<ExistenceCheckResponse> =>
    api.get('/users/check-username', { params: { username } }).then((res) => res.data),

  checkEmail: (email: string): Promise<ExistenceCheckResponse> =>
    api.get('/users/check-email', { params: { email } }).then((res) => res.data),

  // ============ Workout Endpoints ============

  getWorkouts: (): Promise<WorkoutSession[]> => api.get('/workouts/my').then((res) => res.data),

  getWorkout: (sessionId: number): Promise<WorkoutSession> =>
    api.get(`/workouts/${sessionId}`).then((res) => res.data),

  getWorkoutSmart: (sessionId: number): Promise<WorkoutSession> =>
    api.get(`/workouts/${sessionId}/smart`).then((res) => res.data),

  createWorkout: (workoutData: CreateWorkoutRequest): Promise<WorkoutSession> =>
    api.post('/workouts', workoutData).then((res) => res.data),

  updateWorkout: (sessionId: number, workoutData: UpdateWorkoutRequest): Promise<WorkoutSession> =>
    api.put(`/workouts/${sessionId}`, workoutData).then((res) => res.data),

  deleteWorkout: (sessionId: number): Promise<void> => api.delete(`/workouts/${sessionId}`),

  updateWorkoutStatus: (sessionId: number, action: WorkoutAction): Promise<WorkoutSession> =>
    api.patch(`/workouts/${sessionId}/status`, { action }).then((res) => res.data),

  // ============ Workout Exercise Endpoints ============

  addExerciseToWorkout: (
    sessionId: number,
    exerciseData: CreateWorkoutExerciseRequest
  ): Promise<WorkoutExercise> =>
    api.post(`/workouts/${sessionId}/exercises`, exerciseData).then((res) => res.data),

  getWorkoutExercises: (sessionId: number): Promise<WorkoutExercise[]> =>
    api.get(`/workouts/${sessionId}/exercises`).then((res) => res.data),

  updateWorkoutExercise: (
    workoutExerciseId: number,
    data: Partial<CreateWorkoutExerciseRequest>
  ): Promise<WorkoutExercise> =>
    api.put(`/workout-exercises/${workoutExerciseId}`, data).then((res) => res.data),

  deleteWorkoutExercise: (workoutExerciseId: number): Promise<void> =>
    api.delete(`/workout-exercises/${workoutExerciseId}`),

  // ============ Exercise Library Endpoints ============

  getExercises: (page = 0, size = 20): Promise<PagedResponse<Exercise>> =>
    api.get('/exercises', { params: { page, size } }).then((res) => res.data),

  getExercise: (exerciseId: number): Promise<Exercise> =>
    api.get(`/exercises/${exerciseId}`).then((res) => res.data),

  searchExercises: (name: string): Promise<Exercise[]> =>
    api.get('/exercises/search', { params: { name } }).then((res) => res.data),

  filterExercises: (
    type?: ExerciseType,
    targetMuscleGroup?: TargetMuscleGroup,
    difficultyLevel?: DifficultyLevel,
    page = 0,
    size = 20
  ): Promise<Exercise[]> =>
    api
      .get('/exercises/filter', {
        params: { type, targetMuscleGroup, difficultyLevel, page, size },
      })
      .then((res) => res.data),

  createExercise: (exerciseData: CreateExerciseRequest): Promise<Exercise> =>
    api.post('/exercises', exerciseData).then((res) => res.data),

  updateExercise: (exerciseId: number, exerciseData: CreateExerciseRequest): Promise<Exercise> =>
    api.put(`/exercises/${exerciseId}`, exerciseData).then((res) => res.data),

  deleteExercise: (exerciseId: number): Promise<void> => api.delete(`/exercises/${exerciseId}`),

  // ============ Strength Set Endpoints ============

  getStrengthSets: (workoutExerciseId: number): Promise<BaseSet[]> =>
    api.get(`/workout-exercises/${workoutExerciseId}/strength-sets`).then((res) => res.data),

  createStrengthSet: (
    workoutExerciseId: number,
    setData: CreateStrengthSetRequest
  ): Promise<BaseSet> =>
    api
      .post(`/workout-exercises/${workoutExerciseId}/strength-sets`, setData)
      .then((res) => res.data),

  updateStrengthSet: (
    workoutExerciseId: number,
    setId: number,
    setData: UpdateSetRequest
  ): Promise<BaseSet> =>
    api
      .put(`/workout-exercises/${workoutExerciseId}/strength-sets/${setId}`, setData)
      .then((res) => res.data),

  deleteStrengthSet: (workoutExerciseId: number, setId: number): Promise<void> =>
    api.delete(`/workout-exercises/${workoutExerciseId}/strength-sets/${setId}`),

  // ============ Cardio Set Endpoints ============

  getCardioSets: (workoutExerciseId: number): Promise<BaseSet[]> =>
    api.get(`/workout-exercises/${workoutExerciseId}/cardio-sets`).then((res) => res.data),

  createCardioSet: (workoutExerciseId: number, setData: CreateCardioSetRequest): Promise<BaseSet> =>
    api
      .post(`/workout-exercises/${workoutExerciseId}/cardio-sets`, setData)
      .then((res) => res.data),

  updateCardioSet: (
    workoutExerciseId: number,
    setId: number,
    setData: UpdateSetRequest
  ): Promise<BaseSet> =>
    api
      .put(`/workout-exercises/${workoutExerciseId}/cardio-sets/${setId}`, setData)
      .then((res) => res.data),

  deleteCardioSet: (workoutExerciseId: number, setId: number): Promise<void> =>
    api.delete(`/workout-exercises/${workoutExerciseId}/cardio-sets/${setId}`),

  // ============ Flexibility Set Endpoints ============

  getFlexibilitySets: (workoutExerciseId: number): Promise<BaseSet[]> =>
    api.get(`/workout-exercises/${workoutExerciseId}/flexibility-sets`).then((res) => res.data),

  createFlexibilitySet: (
    workoutExerciseId: number,
    setData: CreateFlexibilitySetRequest
  ): Promise<BaseSet> =>
    api
      .post(`/workout-exercises/${workoutExerciseId}/flexibility-sets`, setData)
      .then((res) => res.data),

  updateFlexibilitySet: (
    workoutExerciseId: number,
    setId: number,
    setData: UpdateSetRequest
  ): Promise<BaseSet> =>
    api
      .put(`/workout-exercises/${workoutExerciseId}/flexibility-sets/${setId}`, setData)
      .then((res) => res.data),

  deleteFlexibilitySet: (workoutExerciseId: number, setId: number): Promise<void> =>
    api.delete(`/workout-exercises/${workoutExerciseId}/flexibility-sets/${setId}`),
};

export default api;
