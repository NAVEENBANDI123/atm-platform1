import { createSlice } from '@reduxjs/toolkit';

const STORAGE_KEY = 'atm.auth';

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

const persisted = loadState();

const initialState = {
  accessToken: persisted?.accessToken || null,
  refreshToken: persisted?.refreshToken || null,
  user: persisted?.user || null,
  isAuthenticated: Boolean(persisted?.accessToken),
};

function persist(state) {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      accessToken: state.accessToken,
      refreshToken: state.refreshToken,
      user: state.user,
    })
  );
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials(state, action) {
      const { accessToken, refreshToken, user } = action.payload;
      state.accessToken = accessToken;
      state.refreshToken = refreshToken;
      state.user = user;
      state.isAuthenticated = true;
      persist(state);
    },
    setAccessToken(state, action) {
      state.accessToken = action.payload;
      persist(state);
    },
    setUser(state, action) {
      state.user = action.payload;
      persist(state);
    },
    logout(state) {
      state.accessToken = null;
      state.refreshToken = null;
      state.user = null;
      state.isAuthenticated = false;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});

export const { setCredentials, setAccessToken, setUser, logout } =
  authSlice.actions;

const ROLE_NAMES = [
  'ROLE_SUPER_ADMIN',
  'ROLE_ADMIN',
  'ROLE_ACCOUNTANT',
  'ROLE_CASHIER',
  'ROLE_CARD_OFFICER',
  'ROLE_LOAN_OFFICER',
  'ROLE_CUSTOMER',
];

export const selectAuth = (state) => state.auth;
export const selectUser = (state) => state.auth.user;
export const selectRoles = (state) => state.auth.user?.roles || [];

const hasAnyRole = (state, roles) => {
  const userRoles = state.auth.user?.roles || [];
  return roles.some((r) => userRoles.includes(r));
};

export const selectIsCustomer = (state) =>
  state.auth.user?.userType === 'CUSTOMER';
export const selectIsEmployee = (state) =>
  state.auth.user?.userType === 'EMPLOYEE';

export const selectIsSuperAdmin = (state) =>
  hasAnyRole(state, ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']);
export const selectIsAccountant = (state) =>
  hasAnyRole(state, ['ROLE_ACCOUNTANT', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN']);
export const selectIsCashier = (state) =>
  hasAnyRole(state, ['ROLE_CASHIER', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN']);
export const selectIsCardOfficer = (state) =>
  hasAnyRole(state, ['ROLE_CARD_OFFICER', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN']);
export const selectIsLoanOfficer = (state) =>
  hasAnyRole(state, ['ROLE_LOAN_OFFICER', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN']);

export const ROLES = ROLE_NAMES;

export default authSlice.reducer;
