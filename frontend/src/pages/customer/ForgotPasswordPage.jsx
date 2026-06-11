import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
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
import { extractError } from '../../utils/error.js';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const verifyForm = useForm();
  const resetForm = useForm();
  const [serverError, setServerError] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [done, setDone] = useState(false);

  const onVerify = async (values) => {
    setServerError('');
    try {
      const { data } = await authApi.customerForgotPassword(values);
      setResetToken(data.data.resetToken);
    } catch (err) {
      setServerError(extractError(err, 'Could not verify identity'));
    }
  };

  const onReset = async (values) => {
    setServerError('');
    try {
      await authApi.customerResetPassword({
        resetToken,
        newPassword: values.newPassword,
      });
      setDone(true);
      setTimeout(() => navigate('/customer/login', { replace: true }), 1500);
    } catch (err) {
      setServerError(extractError(err, 'Could not reset password'));
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
      <Card sx={{ width: 440, m: 2 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            Reset password
          </Typography>
          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}
          {done && (
            <Alert severity="success" sx={{ mb: 2 }}>
              Password updated. Redirecting...
            </Alert>
          )}

          {!resetToken ? (
            <form onSubmit={verifyForm.handleSubmit(onVerify)}>
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
                  label="Mobile"
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
                  disabled={verifyForm.formState.isSubmitting}
                >
                  Verify identity
                </Button>
              </Stack>
            </form>
          ) : (
            <form onSubmit={resetForm.handleSubmit(onReset)}>
              <Stack spacing={2}>
                <Alert severity="info">
                  Identity verified. Choose a new password.
                </Alert>
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
                  disabled={resetForm.formState.isSubmitting}
                >
                  Update password
                </Button>
              </Stack>
            </form>
          )}
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Link component={RouterLink} to="/customer/login">
              Back to login
            </Link>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
