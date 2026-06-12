import axiosClient from '../../api/axiosClient.js';

const CUSTOMER = '/api/v1/auth/customer';
const EMPLOYEE = '/api/v1/auth/employee';

export const authApi = {
  // ---- Customer ----
  customerLogin: (payload) => axiosClient.post(`${CUSTOMER}/login`, payload),
  customerRegister: (payload) =>
    axiosClient.post(`${CUSTOMER}/register`, payload),
  customerForgotPassword: (payload) =>
    axiosClient.post(`${CUSTOMER}/forgot-password`, payload),
  customerResetPassword: (payload) =>
    axiosClient.post(`${CUSTOMER}/reset-password`, payload),
  customerRefresh: (refreshToken) =>
    axiosClient.post(`${CUSTOMER}/refresh`, { refreshToken }),
  customerLogout: (refreshToken) =>
    axiosClient.post(`${CUSTOMER}/logout`, { refreshToken }),

  // ---- Employee ----
  employeeLogin: (payload) => axiosClient.post(`${EMPLOYEE}/login`, payload),
  employeeForgotPassword: (payload) =>
    axiosClient.post(`${EMPLOYEE}/forgot-password`, payload),
  employeeResetPassword: (payload) =>
    axiosClient.post(`${EMPLOYEE}/reset-password`, payload),
  employeeRefresh: (refreshToken) =>
    axiosClient.post(`${EMPLOYEE}/refresh`, { refreshToken }),
  employeeLogout: (refreshToken) =>
    axiosClient.post(`${EMPLOYEE}/logout`, { refreshToken }),
};
