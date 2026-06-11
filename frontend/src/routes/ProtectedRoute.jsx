import { useSelector } from 'react-redux';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import {
  selectAuth,
  selectIsCustomer,
  selectIsEmployee,
} from '../features/auth/authSlice.js';

/**
 * Generic guard.  Three modes:
 *  - {customerOnly}   - require an authenticated CUSTOMER user.
 *  - {employeeOnly}   - require an authenticated EMPLOYEE user.
 *  - {requireAnyRole} - require at least one of the listed Spring roles.
 *
 * Can either wrap children directly or be used as a layout route with <Outlet/>.
 */
export default function ProtectedRoute({
  children,
  customerOnly = false,
  employeeOnly = false,
  requireAnyRole = null,
}) {
  const { isAuthenticated, user } = useSelector(selectAuth);
  const isCustomer = useSelector(selectIsCustomer);
  const isEmployee = useSelector(selectIsEmployee);
  const location = useLocation();

  if (!isAuthenticated) {
    const target = employeeOnly ? '/employee/login' : '/customer/login';
    return <Navigate to={target} state={{ from: location }} replace />;
  }
  if (customerOnly && !isCustomer) {
    return <Navigate to="/employee/dashboard" replace />;
  }
  if (employeeOnly && !isEmployee) {
    return <Navigate to="/customer/dashboard" replace />;
  }
  if (
    requireAnyRole &&
    !requireAnyRole.some((role) => (user?.roles || []).includes(role))
  ) {
    return <Navigate to="/employee/dashboard" replace />;
  }

  return children ?? <Outlet />;
}
