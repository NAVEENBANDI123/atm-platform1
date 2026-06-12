import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { Link as RouterLink, Navigate, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Divider,
  IconButton,
  InputAdornment,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import LoginIcon from '@mui/icons-material/Login';
import { authApi } from '../../features/auth/authApi.js';
import { setCredentials, selectAuth } from '../../features/auth/authSlice.js';
import { extractError } from '../../utils/error.js';
import AuthLayout from '../../components/AuthLayout.jsx';

export default function CustomerLoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector(selectAuth);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm();
  const [serverError, setServerError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

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
    <AuthLayout
      variant="customer"
      eyebrow="Customer Portal"
      title="Welcome back"
      subtitle="Sign in to manage your accounts, transfers, cards and loans."
    >
      {serverError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {serverError}
        </Alert>
      )}
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
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
            type={showPassword ? 'text' : 'password'}
            fullWidth
            autoComplete="current-password"
            {...register('password', { required: 'Password is required' })}
            error={Boolean(errors.password)}
            helperText={errors.password?.message}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    edge="end"
                    onClick={() => setShowPassword((v) => !v)}
                    aria-label="toggle password visibility"
                  >
                    {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={isSubmitting}
            startIcon={<LoginIcon />}
          >
            {isSubmitting ? 'Signing in...' : 'Sign In'}
          </Button>
        </Stack>
      </form>

      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        sx={{ mt: 2 }}
      >
        <Link component={RouterLink} to="/customer/register" underline="hover">
          Open new account
        </Link>
        <Link
          component={RouterLink}
          to="/customer/forgot-password"
          underline="hover"
        >
          Forgot password?
        </Link>
      </Stack>

      <Divider sx={{ my: 3 }} />

      <Box sx={{ textAlign: 'center' }}>
        <Typography variant="caption" color="text.secondary">
          Are you an employee?{' '}
          <Link component={RouterLink} to="/employee/login" underline="hover">
            Use the employee portal
          </Link>
        </Typography>
      </Box>
    </AuthLayout>
  );
}
