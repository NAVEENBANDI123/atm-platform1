import axiosClient from '../../api/axiosClient.js';

// ---- Customer self-service ----

export const accountApi = {
  getMyAccount: () => axiosClient.get('/api/v1/accounts/me'),
  getDashboard: () => axiosClient.get('/api/v1/accounts/me/dashboard'),
  getBalance: () => axiosClient.get('/api/v1/accounts/me/balance'),
  transfer: (payload) => axiosClient.post('/api/v1/accounts/transfer', payload),
};

export const transactionApi = {
  list: (page = 0, size = 20) =>
    axiosClient.get(`/api/v1/transactions?page=${page}&size=${size}`),
  miniStatement: () => axiosClient.get('/api/v1/transactions/mini-statement'),
  downloadStatement: () =>
    axiosClient.get('/api/v1/transactions/statement', { responseType: 'blob' }),
};

export const profileApi = {
  me: () => axiosClient.get('/api/v1/customer/profile'),
  changePassword: (payload) =>
    axiosClient.post('/api/v1/customer/profile/change-password', payload),
};

export const beneficiaryApi = {
  list: () => axiosClient.get('/api/v1/beneficiaries'),
  add: (payload) => axiosClient.post('/api/v1/beneficiaries', payload),
  remove: (id) => axiosClient.delete(`/api/v1/beneficiaries/${id}`),
};

export const nomineeApi = {
  list: () => axiosClient.get('/api/v1/nominees'),
  add: (payload) => axiosClient.post('/api/v1/nominees', payload),
  remove: (id) => axiosClient.delete(`/api/v1/nominees/${id}`),
};

export const cardApi = {
  myApplications: () => axiosClient.get('/api/v1/cards/applications/me'),
  myCards: () => axiosClient.get('/api/v1/cards/me'),
  apply: (payload) =>
    axiosClient.post('/api/v1/cards/applications', payload),
  // Officer / admin
  pending: (page = 0, size = 20) =>
    axiosClient.get(
      `/api/v1/cards/applications/pending?page=${page}&size=${size}`
    ),
  review: (id, payload) =>
    axiosClient.post(`/api/v1/cards/applications/${id}/review`, payload),
  approve: (id) =>
    axiosClient.post(`/api/v1/cards/applications/${id}/approve`),
  reject: (id, reason) =>
    axiosClient.post(`/api/v1/cards/applications/${id}/reject`, { reason }),
};

export const loanApi = {
  myApplications: () => axiosClient.get('/api/v1/loans/applications/me'),
  myLoans: () => axiosClient.get('/api/v1/loans/me'),
  apply: (payload) =>
    axiosClient.post('/api/v1/loans/applications', payload),
  // Officer / admin
  pending: (page = 0, size = 20) =>
    axiosClient.get(
      `/api/v1/loans/applications/pending?page=${page}&size=${size}`
    ),
  review: (id, payload) =>
    axiosClient.post(`/api/v1/loans/applications/${id}/review`, payload),
  approve: (id) =>
    axiosClient.post(`/api/v1/loans/applications/${id}/approve`),
  reject: (id, reason) =>
    axiosClient.post(`/api/v1/loans/applications/${id}/reject`, { reason }),
};

export const depositApi = {
  openFd: (payload) => axiosClient.post('/api/v1/deposits/fd', payload),
  openRd: (payload) => axiosClient.post('/api/v1/deposits/rd', payload),
  mine: () => axiosClient.get('/api/v1/deposits/me'),
};

export const ticketApi = {
  create: (payload) => axiosClient.post('/api/v1/tickets', payload),
  mine: () => axiosClient.get('/api/v1/tickets/me'),
  list: (status = 'OPEN', page = 0, size = 20) =>
    axiosClient.get(
      `/api/v1/tickets?status=${status}&page=${page}&size=${size}`
    ),
  resolve: (id, payload) =>
    axiosClient.post(`/api/v1/tickets/${id}/resolve`, payload),
};

export const notificationApi = {
  list: (page = 0, size = 20) =>
    axiosClient.get(`/api/v1/notifications?page=${page}&size=${size}`),
  unreadCount: () => axiosClient.get('/api/v1/notifications/unread-count'),
  markAllRead: () => axiosClient.post('/api/v1/notifications/mark-all-read'),
};

// ---- Employee / Admin ----

export const adminApi = {
  // customer onboarding
  listCustomers: (status = 'PENDING_APPROVAL', page = 0, size = 20) =>
    axiosClient.get(
      `/api/v1/admin/customers?status=${status}&page=${page}&size=${size}`
    ),
  getCustomer: (id) => axiosClient.get(`/api/v1/admin/customers/${id}`),
  approveCustomer: (id) =>
    axiosClient.post(`/api/v1/admin/customers/${id}/approve`),
  rejectCustomer: (id, reason) =>
    axiosClient.post(`/api/v1/admin/customers/${id}/reject`, { reason }),

  // employee management
  listEmployees: (page = 0, size = 50) =>
    axiosClient.get(`/api/v1/admin/employees?page=${page}&size=${size}`),
  createEmployee: (payload) =>
    axiosClient.post('/api/v1/admin/employees', payload),
  updateEmployee: (id, payload) =>
    axiosClient.patch(`/api/v1/admin/employees/${id}`, payload),
  disableEmployee: (id) =>
    axiosClient.post(`/api/v1/admin/employees/${id}/disable`),
  enableEmployee: (id) =>
    axiosClient.post(`/api/v1/admin/employees/${id}/enable`),

  // audit
  audit: (params = {}) => {
    const q = new URLSearchParams(params).toString();
    return axiosClient.get(`/api/v1/admin/audit${q ? `?${q}` : ''}`);
  },
};

export const cashierApi = {
  lookup: (accountNumber) =>
    axiosClient.get(`/api/v1/cashier/accounts/${accountNumber}`),
  deposit: (payload) => axiosClient.post('/api/v1/cashier/deposit', payload),
  withdraw: (payload) => axiosClient.post('/api/v1/cashier/withdraw', payload),
};
