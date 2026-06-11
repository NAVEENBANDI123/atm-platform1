import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  Grid,
  Link,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { authApi } from '../../features/auth/authApi.js';
import { extractError } from '../../utils/error.js';

const PREFIXES = ['Mr', 'Mrs', 'Ms', 'Dr'];
const GENDERS = ['Male', 'Female', 'Other'];
const ACCOUNT_TYPES = [
  { value: 'SAVINGS', label: 'Savings Account' },
  { value: 'SALARY', label: 'Salary Account' },
  { value: 'CURRENT', label: 'Current Account' },
];

export default function CustomerRegisterPage() {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm({ defaultValues: { prefix: 'Mr', gender: 'Male', accountType: 'SAVINGS' } });
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');
  const password = watch('password');

  const onSubmit = async (values) => {
    setServerError('');
    setSuccess('');
    try {
      const { data } = await authApi.customerRegister(values);
      setSuccess(
        data.data?.message ||
          'Your account application has been submitted successfully and is waiting for approval.'
      );
      setTimeout(() => navigate('/customer/login', { replace: true }), 3500);
    } catch (err) {
      setServerError(extractError(err, 'Registration failed'));
    }
  };

  const SectionTitle = ({ children }) => (
    <Typography variant="overline" color="primary" sx={{ mt: 2 }}>
      {children}
    </Typography>
  );

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
      <Card sx={{ width: 720, m: 2 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            Open a new bank account
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Provide the details below. After approval by our team you will
            receive your customer ID and account number by email.
          </Typography>

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

          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <SectionTitle>Personal Information</SectionTitle>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={2}>
                <TextField
                  select
                  fullWidth
                  label="Prefix"
                  defaultValue="Mr"
                  {...register('prefix', { required: true })}
                  error={Boolean(errors.prefix)}
                >
                  {PREFIXES.map((p) => (
                    <MenuItem key={p} value={p}>
                      {p}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="First name"
                  {...register('firstName', { required: 'Required' })}
                  error={Boolean(errors.firstName)}
                  helperText={errors.firstName?.message}
                />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField
                  fullWidth
                  label="Middle name"
                  {...register('middleName')}
                />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField
                  fullWidth
                  label="Last name"
                  {...register('lastName', { required: 'Required' })}
                  error={Boolean(errors.lastName)}
                  helperText={errors.lastName?.message}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  select
                  fullWidth
                  label="Gender"
                  defaultValue="Male"
                  {...register('gender', { required: true })}
                >
                  {GENDERS.map((g) => (
                    <MenuItem key={g} value={g}>
                      {g}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  type="date"
                  fullWidth
                  label="Date of birth"
                  InputLabelProps={{ shrink: true }}
                  {...register('dateOfBirth', { required: 'Required' })}
                  error={Boolean(errors.dateOfBirth)}
                  helperText={errors.dateOfBirth?.message}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Mobile (10 digits)"
                  {...register('mobile', {
                    required: 'Required',
                    pattern: { value: /^[0-9]{10}$/, message: '10 digits' },
                  })}
                  error={Boolean(errors.mobile)}
                  helperText={errors.mobile?.message}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  type="email"
                  fullWidth
                  label="Email"
                  {...register('email', { required: 'Required' })}
                  error={Boolean(errors.email)}
                  helperText={errors.email?.message}
                />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField
                  fullWidth
                  label="Aadhaar (12 digits)"
                  {...register('aadhaar', {
                    required: 'Required',
                    pattern: { value: /^[0-9]{12}$/, message: '12 digits' },
                  })}
                  error={Boolean(errors.aadhaar)}
                  helperText={errors.aadhaar?.message}
                />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField
                  fullWidth
                  label="PAN"
                  {...register('pan', {
                    required: 'Required',
                    pattern: {
                      value: /^[A-Z]{5}[0-9]{4}[A-Z]$/,
                      message: 'Format: AAAAA9999A',
                    },
                  })}
                  error={Boolean(errors.pan)}
                  helperText={errors.pan?.message}
                />
              </Grid>
            </Grid>

            <Divider sx={{ my: 3 }} />
            <SectionTitle>Address Information</SectionTitle>
            <Grid container spacing={2}>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  label="House #"
                  {...register('houseNumber', { required: 'Required' })}
                  error={Boolean(errors.houseNumber)}
                />
              </Grid>
              <Grid item xs={6} sm={4}>
                <TextField
                  fullWidth
                  label="Street"
                  {...register('street', { required: 'Required' })}
                  error={Boolean(errors.street)}
                />
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField
                  fullWidth
                  label="Area / Locality"
                  {...register('area', { required: 'Required' })}
                  error={Boolean(errors.area)}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  label="City"
                  {...register('city', { required: 'Required' })}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  label="State"
                  {...register('state', { required: 'Required' })}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  label="Country"
                  defaultValue="India"
                  {...register('country', { required: 'Required' })}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  label="Postal code"
                  {...register('postalCode', {
                    required: 'Required',
                    pattern: { value: /^[0-9]{4,12}$/, message: '4-12 digits' },
                  })}
                  error={Boolean(errors.postalCode)}
                />
              </Grid>
            </Grid>

            <Divider sx={{ my: 3 }} />
            <SectionTitle>Account Information</SectionTitle>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  fullWidth
                  label="Account type"
                  defaultValue="SAVINGS"
                  {...register('accountType', { required: true })}
                >
                  {ACCOUNT_TYPES.map((t) => (
                    <MenuItem key={t.value} value={t.value}>
                      {t.label}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
            </Grid>

            <Divider sx={{ my: 3 }} />
            <SectionTitle>Security</SectionTitle>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Username"
                  {...register('username', {
                    required: 'Required',
                    minLength: { value: 3, message: 'Min 3' },
                    pattern: {
                      value: /^[a-zA-Z0-9_.]+$/,
                      message: 'letters, digits, ".", "_"',
                    },
                  })}
                  error={Boolean(errors.username)}
                  helperText={errors.username?.message}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  type="password"
                  fullWidth
                  label="Password"
                  {...register('password', {
                    required: 'Required',
                    minLength: { value: 8, message: 'Min 8 chars' },
                  })}
                  error={Boolean(errors.password)}
                  helperText={errors.password?.message}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  type="password"
                  fullWidth
                  label="Confirm password"
                  {...register('confirmPassword', {
                    required: 'Required',
                    validate: (v) => v === password || 'Passwords do not match',
                  })}
                  error={Boolean(errors.confirmPassword)}
                  helperText={errors.confirmPassword?.message}
                />
              </Grid>
            </Grid>

            <Stack direction="row" justifyContent="space-between" sx={{ mt: 4 }}>
              <Link component={RouterLink} to="/customer/login">
                Already have an account? Sign in
              </Link>
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Submitting...' : 'Submit application'}
              </Button>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
