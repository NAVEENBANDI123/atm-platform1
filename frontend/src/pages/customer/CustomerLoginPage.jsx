import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { Link as RouterLink, Navigate, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { authApi } from '../../features/auth/authApi.js';
import { setCredentials, selectAuth } from '../../features/auth/authSlice.js';
import { extractError } from '../../utils/error.js';

export default function CustomerLoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector(selectAuth);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
  const [serverError, setServerError] = useState('');

  if (isAuthenticated && user?.userType === 'CUSTOMER') {
    return <Navigate to="/customer/dashboard" replace />;
  }

  const onSubmit = async (values) => {
    setServerError('');
    try {
      const { data } = await authApi.customerLogin(values);
      dispatch(setCredentials(data.data));
      navigate('/customer/dashboard', { replace: true });
    } catch (err) {
      setServerError(extractError(err, 'Login failed'));
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        bgcolor: 'background.default',
      }}
    >
      <Card sx={{ width: 420, m: 2 }}>
        <CardContent>
          <Typography variant="overline" color="primary">
            Customer Portal
          </Typography>
          <Typography variant="h5" gutterBottom>
            Sign in to your account
          </Typography>
          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}
          <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2}>
              <TextField
                label="Username"
                fullWidth
                autoComplete="username"
                {...register('username', { required: 'Username is required' })}
                error={Boolean(errors.username)}
                helperText={errors.username?.message}
              />
              <TextField
                label="Password"
                type="password"
                fullWidth
                autoComplete="current-password"
                {...register('password', { required: 'Password is required' })}
                error={Boolean(errors.password)}
                helperText={errors.password?.message}
              />
              <Button type="submit" variant="contained" disabled={isSubmitting}>
                {isSubmitting ? 'Signing in...' : 'Sign In'}
              </Button>
            </Stack>
          </form>
          <Stack direction="row" justifyContent="space-between" sx={{ mt: 2 }}>
            <Link component={RouterLink} to="/customer/register">
              Open new account
            </Link>
            <Link component={RouterLink} to="/customer/forgot-password">
              Forgot password?
            </Link>
          </Stack>
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Typography variant="caption" color="text.secondary">
              Are you an employee?{' '}
              <Link component={RouterLink} to="/employee/login">
                Use the employee portal
              </Link>
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
