import axios from 'axios';

/**
 * API Service with Auth0 JWT authentication.
 *
 * Provides methods for all backend API endpoints.
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081/api/v1';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Store for the getAccessTokenSilently function
let getAccessToken = null;

/**
 * Initialize the API service with Auth0's getAccessTokenSilently function.
 */
export const initializeApi = (getAccessTokenSilently) => {
  getAccessToken = getAccessTokenSilently;
};

// Request interceptor to add auth token
api.interceptors.request.use(
  async (config) => {
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
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
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

  getCurrentUser: () => api.get('/users/me').then(res => res.data),

  updateCurrentUser: (userData) => api.put('/users/me', userData).then(res => res.data),

  checkUsername: (username) => api.get('/users/check-username', { params: { username } }).then(res => res.data),

  checkEmail: (email) => api.get('/users/check-email', { params: { email } }).then(res => res.data),

  // ============ Workout Endpoints ============

  getWorkouts: () => api.get('/workouts/my').then(res => res.data),

  getWorkout: (sessionId) => api.get(`/workouts/${sessionId}`).then(res => res.data),

  getWorkoutSmart: (sessionId) => api.get(`/workouts/${sessionId}/smart`).then(res => res.data),

  createWorkout: (workoutData) => api.post('/workouts', workoutData).then(res => res.data),

  updateWorkout: (sessionId, workoutData) => api.put(`/workouts/${sessionId}`, workoutData).then(res => res.data),

  deleteWorkout: (sessionId) => api.delete(`/workouts/${sessionId}`),

  updateWorkoutStatus: (sessionId, action) =>
    api.patch(`/workouts/${sessionId}/status`, { action }).then(res => res.data),

  // ============ Workout Exercise Endpoints ============

  addExerciseToWorkout: (sessionId, exerciseData) =>
    api.post(`/workouts/${sessionId}/exercises`, exerciseData).then(res => res.data),

  getWorkoutExercises: (sessionId) =>
    api.get(`/workouts/${sessionId}/exercises`).then(res => res.data),

  updateWorkoutExercise: (workoutExerciseId, data) =>
    api.put(`/workout-exercises/${workoutExerciseId}`, data).then(res => res.data),

  deleteWorkoutExercise: (workoutExerciseId) =>
    api.delete(`/workout-exercises/${workoutExerciseId}`),

  // ============ Exercise Library Endpoints ============

  getExercises: (page = 0, size = 20) =>
    api.get('/exercises', { params: { page, size } }).then(res => res.data),

  getExercise: (exerciseId) =>
    api.get(`/exercises/${exerciseId}`).then(res => res.data),

  searchExercises: (name) =>
    api.get('/exercises/search', { params: { name } }).then(res => res.data),

  filterExercises: (type, targetMuscleGroup, difficultyLevel, page = 0, size = 20) =>
    api.get('/exercises/filter', {
      params: { type, targetMuscleGroup, difficultyLevel, page, size }
    }).then(res => res.data),

  createExercise: (exerciseData) =>
    api.post('/exercises', exerciseData).then(res => res.data),

  updateExercise: (exerciseId, exerciseData) =>
    api.put(`/exercises/${exerciseId}`, exerciseData).then(res => res.data),

  deleteExercise: (exerciseId) =>
    api.delete(`/exercises/${exerciseId}`),

  // ============ Strength Set Endpoints ============

  getStrengthSets: (workoutExerciseId) =>
    api.get(`/workout-exercises/${workoutExerciseId}/strength-sets`).then(res => res.data),

  createStrengthSet: (workoutExerciseId, setData) =>
    api.post(`/workout-exercises/${workoutExerciseId}/strength-sets`, setData).then(res => res.data),

  updateStrengthSet: (workoutExerciseId, setId, setData) =>
    api.put(`/workout-exercises/${workoutExerciseId}/strength-sets/${setId}`, setData).then(res => res.data),

  deleteStrengthSet: (workoutExerciseId, setId) =>
    api.delete(`/workout-exercises/${workoutExerciseId}/strength-sets/${setId}`),

  // ============ Cardio Set Endpoints ============

  getCardioSets: (workoutExerciseId) =>
    api.get(`/workout-exercises/${workoutExerciseId}/cardio-sets`).then(res => res.data),

  createCardioSet: (workoutExerciseId, setData) =>
    api.post(`/workout-exercises/${workoutExerciseId}/cardio-sets`, setData).then(res => res.data),

  updateCardioSet: (workoutExerciseId, setId, setData) =>
    api.put(`/workout-exercises/${workoutExerciseId}/cardio-sets/${setId}`, setData).then(res => res.data),

  deleteCardioSet: (workoutExerciseId, setId) =>
    api.delete(`/workout-exercises/${workoutExerciseId}/cardio-sets/${setId}`),

  // ============ Flexibility Set Endpoints ============

  getFlexibilitySets: (workoutExerciseId) =>
    api.get(`/workout-exercises/${workoutExerciseId}/flexibility-sets`).then(res => res.data),

  createFlexibilitySet: (workoutExerciseId, setData) =>
    api.post(`/workout-exercises/${workoutExerciseId}/flexibility-sets`, setData).then(res => res.data),

  updateFlexibilitySet: (workoutExerciseId, setId, setData) =>
    api.put(`/workout-exercises/${workoutExerciseId}/flexibility-sets/${setId}`, setData).then(res => res.data),

  deleteFlexibilitySet: (workoutExerciseId, setId) =>
    api.delete(`/workout-exercises/${workoutExerciseId}/flexibility-sets/${setId}`),
};

// Constants for dropdowns (frozen to prevent accidental mutation)
export const EXERCISE_TYPES = Object.freeze(['STRENGTH', 'CARDIO', 'FLEXIBILITY']);

export const MUSCLE_GROUPS = Object.freeze([
  'CHEST', 'BACK', 'SHOULDERS', 'BICEPS', 'TRICEPS', 'FOREARMS',
  'QUADRICEPS', 'HAMSTRINGS', 'GLUTES', 'CALVES', 'CORE', 'FULL_BODY'
]);

export const DIFFICULTY_LEVELS = Object.freeze(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']);

export const WORKOUT_STATUSES = Object.freeze(['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'PAUSED']);

export const WORKOUT_ACTIONS = Object.freeze(['START', 'PAUSE', 'RESUME', 'COMPLETE', 'CANCEL']);

export default api;
