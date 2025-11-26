import axios from 'axios';

/**
 * API Service with Auth0 JWT authentication.
 *
 * This service handles all API calls to the backend, automatically
 * attaching the Auth0 access token to requests.
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081/api/v1';

// Create axios instance with default config
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
 * Call this from a component that has access to useAuth0().
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
        // Continue without token - backend will return 401
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;

      if (status === 401) {
        console.error('Unauthorized - token may be expired');
        // Could trigger re-authentication here
      } else if (status === 403) {
        console.error('Forbidden - insufficient permissions');
      }

      // Preserve the error message from backend
      const message = data?.message || data?.error || `Request failed with status ${status}`;
      error.message = message;
    } else if (error.request) {
      // Request made but no response received
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

  /**
   * Get current authenticated user's profile.
   */
  getCurrentUser: async () => {
    const response = await api.get('/users/me');
    return response.data;
  },

  /**
   * Update current user's profile.
   */
  updateCurrentUser: async (userData) => {
    const response = await api.put('/users/me', userData);
    return response.data;
  },

  // ============ Workout Endpoints ============

  /**
   * Get all workouts for the current user.
   */
  getWorkouts: async (page = 0, size = 10) => {
    const response = await api.get('/workouts', {
      params: { page, size },
    });
    return response.data;
  },

  /**
   * Get a specific workout by ID.
   */
  getWorkout: async (sessionId) => {
    const response = await api.get(`/workouts/${sessionId}`);
    return response.data;
  },

  /**
   * Create a new workout.
   */
  createWorkout: async (workoutData) => {
    const response = await api.post('/workouts', workoutData);
    return response.data;
  },

  /**
   * Update an existing workout.
   */
  updateWorkout: async (sessionId, workoutData) => {
    const response = await api.put(`/workouts/${sessionId}`, workoutData);
    return response.data;
  },

  /**
   * Delete a workout.
   */
  deleteWorkout: async (sessionId) => {
    await api.delete(`/workouts/${sessionId}`);
  },

  /**
   * Start a workout session.
   */
  startWorkout: async (sessionId) => {
    const response = await api.post(`/workouts/${sessionId}/start`);
    return response.data;
  },

  /**
   * Complete a workout session.
   */
  completeWorkout: async (sessionId) => {
    const response = await api.post(`/workouts/${sessionId}/complete`);
    return response.data;
  },

  // ============ Exercise Endpoints ============

  /**
   * Get all exercises (exercise library).
   */
  getExercises: async (page = 0, size = 20) => {
    const response = await api.get('/exercises', {
      params: { page, size },
    });
    return response.data;
  },

  /**
   * Get a specific exercise by ID.
   */
  getExercise: async (exerciseId) => {
    const response = await api.get(`/exercises/${exerciseId}`);
    return response.data;
  },

  /**
   * Search exercises by name or muscle group.
   */
  searchExercises: async (query, muscleGroup, page = 0, size = 20) => {
    const response = await api.get('/exercises/search', {
      params: { query, muscleGroup, page, size },
    });
    return response.data;
  },
};

export default api;
