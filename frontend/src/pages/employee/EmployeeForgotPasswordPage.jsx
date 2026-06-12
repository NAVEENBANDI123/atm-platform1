import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { authApi } from '../../features/auth/authApi.js';
import { extractError } from '../../utils/error.js';
import AuthLayout from '../../components/AuthLayout.jsx';

/**
 * Employee forgot-password (mirror of the customer flow but routed to the
 * employee endpoints).  Step 1 verifies username + mobile and returns a
 * reset token (also emailed to the employee).  Step 2 sets a new password.
 */
export default function EmployeeForgotPasswordPage() {
  const navigate = useNavigate();
  const verifyForm = useForm();
  const resetForm = useForm();
  const [serverError, setServerError] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [done, setDone] = useState(false);

  const onVerify = async (values) => {
    setServerError('');
    try {
      const { data } = await authApi.employeeForgotPassword(values);
      setResetToken(data.data.resetToken);
    } catch (err) {
      setServerError(extractError(err, 'Could not verify identity'));
    }
  };

  const onReset = async (values) => {
    setServerError('');
    try {
      await authApi.employeeResetPassword({
        resetToken,
        newPassword: values.newPassword,
      });
      setDone(true);
      setTimeout(() => navigate('/employee/login', { replace: true }), 1500);
    } catch (err) {
      setServerError(extractError(err, 'Could not reset password'));
    }
  };

  return (
    <AuthLayout
      variant="employee"
      eyebrow="Employee Portal"
      title="Reset your password"
      subtitle={
        resetToken
          ? 'Identity verified. Choose a new password.'
          : 'Enter the username and mobile number on file. A reset token will be emailed to you.'
      }
    >
      {serverError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {serverError}
        </Alert>
      )}
      {done && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Password updated. Redirecting to sign in...
        </Alert>
      )}

      {!resetToken ? (
        <form onSubmit={verifyForm.handleSubmit(onVerify)} noValidate>
          <Stack spacing={2}>
            <TextField
              label="Username"
              fullWidth
              {...verifyForm.register('username', {
                required: 'Username is required',
              })}
              error={Boolean(verifyForm.formState.errors.username)}
              helperText={verifyForm.formState.errors.username?.message}
            />
            <TextField
              label="Mobile (10-15 digits)"
              fullWidth
              {...verifyForm.register('mobile', {
                required: 'Mobile is required',
                pattern: { value: /^[0-9]{10,15}$/, message: '10-15 digits' },
              })}
              error={Boolean(verifyForm.formState.errors.mobile)}
              helperText={verifyForm.formState.errors.mobile?.message}
            />
            <Button
              type="submit"
              variant="contained"
              color="secondary"
              size="large"
              disabled={verifyForm.formState.isSubmitting}
            >
              Verify identity
            </Button>
          </Stack>
        </form>
      ) : (
        <form onSubmit={resetForm.handleSubmit(onReset)} noValidate>
          <Stack spacing={2}>
            <TextField
              label="New password"
              type="password"
              fullWidth
              {...resetForm.register('newPassword', {
                required: 'Password is required',
                minLength: { value: 8, message: 'At least 8 characters' },
              })}
              error={Boolean(resetForm.formState.errors.newPassword)}
              helperText={resetForm.formState.errors.newPassword?.message}
            />
            <Button
              type="submit"
              variant="contained"
              color="secondary"
              size="large"
              disabled={resetForm.formState.isSubmitting}
            >
              Update password
            </Button>
          </Stack>
        </form>
      )}

      <Box sx={{ mt: 3, textAlign: 'center' }}>
        <Typography variant="caption" color="text.secondary">
          Remembered it?{' '}
          <Link component={RouterLink} to="/employee/login" underline="hover">
            Back to staff sign-in
          </Link>
        </Typography>
      </Box>
    </AuthLayout>
  );
}
