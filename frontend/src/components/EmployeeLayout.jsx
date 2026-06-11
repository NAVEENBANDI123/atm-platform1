import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Box,
  Button,
  Container,
  Toolbar,
  Typography,
  Chip,
  Stack,
} from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import { authApi } from '../features/auth/authApi.js';
import {
  logout,
  selectAuth,
  selectIsAccountant,
  selectIsCardOfficer,
  selectIsCashier,
  selectIsLoanOfficer,
  selectIsSuperAdmin,
} from '../features/auth/authSlice.js';

export default function EmployeeLayout() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user, refreshToken } = useSelector(selectAuth);
  const isSuperAdmin = useSelector(selectIsSuperAdmin);
  const isAccountant = useSelector(selectIsAccountant);
  const isCashier = useSelector(selectIsCashier);
  const isCardOfficer = useSelector(selectIsCardOfficer);
  const isLoanOfficer = useSelector(selectIsLoanOfficer);
  const [loggingOut, setLoggingOut] = useState(false);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      if (refreshToken) {
        await authApi.employeeLogout(refreshToken);
      }
    } catch {
      /* ignore */
    } finally {
      dispatch(logout());
      navigate('/employee/login', { replace: true });
    }
  };

  const NavBtn = ({ to, label, show = true }) =>
    show ? (
      <Button color="inherit" component={RouterLink} to={to}>
        {label}
      </Button>
    ) : null;

  const primaryRole = (user?.roles || []).find((r) =>
    [
      'ROLE_SUPER_ADMIN',
      'ROLE_ADMIN',
      'ROLE_ACCOUNTANT',
      'ROLE_CASHIER',
      'ROLE_CARD_OFFICER',
      'ROLE_LOAN_OFFICER',
    ].includes(r)
  );

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" color="secondary">
        <Toolbar>
          <BusinessCenterIcon sx={{ mr: 1 }} />
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            ATM Platform &middot; Employee
          </Typography>
          <NavBtn to="/employee/dashboard" label="Dashboard" />
          <NavBtn
            to="/employee/customers/pending"
            label="Customers"
            show={isAccountant}
          />
          <NavBtn
            to="/employee/employees"
            label="Employees"
            show={isSuperAdmin}
          />
          <NavBtn
            to="/employee/cards/queue"
            label="Cards"
            show={isCardOfficer}
          />
          <NavBtn
            to="/employee/loans/queue"
            label="Loans"
            show={isLoanOfficer}
          />
          <NavBtn to="/employee/cashier" label="Cashier" show={isCashier} />
          <NavBtn to="/employee/tickets" label="Tickets" />
          <NavBtn to="/employee/audit" label="Audit" show={isSuperAdmin} />
          <Stack direction="row" spacing={1} sx={{ mx: 2 }}>
            <Chip
              label={primaryRole?.replace('ROLE_', '') || 'EMPLOYEE'}
              color="primary"
              size="small"
            />
            <Typography variant="body2">{user?.fullName}</Typography>
          </Stack>
          <Button color="inherit" onClick={handleLogout} disabled={loggingOut}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
