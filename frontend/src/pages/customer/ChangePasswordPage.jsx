import { useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { profileApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function ChangePasswordPage() {
  const { register, handleSubmit, watch, reset, formState: { errors, isSubmitting } } = useForm();
  const newPassword = watch('newPassword');
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');

  const onSubmit = async (values) => {
    setServerError('');
    setSuccess('');
    try {
      await profileApi.changePassword(values);
      setSuccess('Password changed. Please sign in again on other devices.');
      reset();
    } catch (err) {
      setServerError(extractError(err, 'Could not change password'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Change password
      </Typography>
      <Card sx={{ maxWidth: 480 }}>
        <CardContent>
          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}
          {success && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {success}
            </Alert>
          )}
          <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2}>
              <TextField
                type="password"
                label="Current password"
                fullWidth
                {...register('currentPassword', { required: 'Required' })}
                error={Boolean(errors.currentPassword)}
                helperText={errors.currentPassword?.message}
              />
              <TextField
                type="password"
                label="New password"
                fullWidth
                {...register('newPassword', {
                  required: 'Required',
                  minLength: { value: 8, message: 'Min 8 characters' },
                })}
                error={Boolean(errors.newPassword)}
                helperText={errors.newPassword?.message}
              />
              <TextField
                type="password"
                label="Confirm new password"
                fullWidth
                {...register('confirmPassword', {
                  required: 'Required',
                  validate: (v) => v === newPassword || 'Passwords do not match',
                })}
                error={Boolean(errors.confirmPassword)}
                helperText={errors.confirmPassword?.message}
              />
              <Button type="submit" variant="contained" disabled={isSubmitting}>
                Update password
              </Button>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
