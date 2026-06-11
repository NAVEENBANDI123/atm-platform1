import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link as RouterLink, Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Box,
  Button,
  Container,
  IconButton,
  Toolbar,
  Tooltip,
  Typography,
  Badge,
} from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { authApi } from '../features/auth/authApi.js';
import { logout, selectAuth } from '../features/auth/authSlice.js';

export default function CustomerLayout() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user, refreshToken } = useSelector(selectAuth);
  const [loggingOut, setLoggingOut] = useState(false);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      if (refreshToken) {
        await authApi.customerLogout(refreshToken);
      }
    } catch {
      /* ignore */
    } finally {
      dispatch(logout());
      navigate('/customer/login', { replace: true });
    }
  };

  const NavBtn = ({ to, label }) => (
    <Button color="inherit" component={RouterLink} to={to}>
      {label}
    </Button>
  );

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static">
        <Toolbar>
          <AccountBalanceIcon sx={{ mr: 1 }} />
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            ATM Platform &middot; Customer
          </Typography>
          <NavBtn to="/customer/dashboard" label="Dashboard" />
          <NavBtn to="/customer/services" label="Services" />
          <Tooltip title="Notifications">
            <IconButton
              color="inherit"
              component={RouterLink}
              to="/customer/notifications"
            >
              <Badge color="error" variant="dot">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>
          <Typography variant="body2" sx={{ mx: 2 }}>
            {user?.fullName}
          </Typography>
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
