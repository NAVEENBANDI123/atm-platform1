import axios from 'axios';
import { store } from '../app/store.js';
import { setAccessToken, logout } from '../features/auth/authSlice.js';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

const axiosClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

axiosClient.interceptors.request.use((config) => {
  const { accessToken } = store.getState().auth;
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshing = null;

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const { refreshToken, user } = store.getState().auth;
    const isAuthCall = original?.url?.includes('/api/v1/auth/');

    if (status === 401 && refreshToken && !original._retry && !isAuthCall) {
      original._retry = true;
      try {
        const refreshUrl =
          user?.userType === 'EMPLOYEE'
            ? '/api/v1/auth/employee/refresh'
            : '/api/v1/auth/customer/refresh';
        refreshing =
          refreshing ||
          axios.post(`${baseURL}${refreshUrl}`, { refreshToken });
        const { data } = await refreshing;
        refreshing = null;
        const newToken = data.data.accessToken;
        store.dispatch(setAccessToken(newToken));
        original.headers.Authorization = `Bearer ${newToken}`;
        return axiosClient(original);
      } catch (refreshError) {
        refreshing = null;
        store.dispatch(logout());
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
