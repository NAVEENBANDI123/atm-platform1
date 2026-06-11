import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  Grid,
  Typography,
} from '@mui/material';
import { profileApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function CustomerProfilePage() {
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    profileApi
      .me()
      .then(({ data }) => setProfile(data.data))
      .catch((err) => setError(extractError(err, 'Could not load profile')));
  }, []);

  const Field = ({ label, value }) => (
    <Box sx={{ mb: 1 }}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography>{value || '-'}</Typography>
    </Box>
  );

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        My profile
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {profile && (
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="overline" color="primary">
                  Personal
                </Typography>
                <Field
                  label="Full name"
                  value={`${profile.prefix || ''} ${profile.firstName || ''} ${profile.middleName || ''} ${profile.lastName || ''}`.trim()}
                />
                <Field label="Customer ID" value={profile.customerId} />
                <Field label="Username" value={profile.username} />
                <Field label="Email" value={profile.email} />
                <Field label="Mobile" value={profile.mobile} />
                <Field label="Gender" value={profile.gender} />
                <Field label="Date of birth" value={profile.dateOfBirth} />
                <Field label="Aadhaar" value={profile.aadhaar} />
                <Field label="PAN" value={profile.pan} />
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="overline" color="primary">
                  Address
                </Typography>
                <Field label="House number" value={profile.houseNumber} />
                <Field label="Street" value={profile.street} />
                <Field label="Area" value={profile.area} />
                <Field label="City" value={profile.city} />
                <Field label="State" value={profile.state} />
                <Field label="Country" value={profile.country} />
                <Field label="Postal code" value={profile.postalCode} />
              </CardContent>
            </Card>
            <Card sx={{ mt: 2 }}>
              <CardContent>
                <Typography variant="overline" color="primary">
                  Account & status
                </Typography>
                <Field label="Account number" value={profile.accountNumber} />
                <Field
                  label="Requested account type"
                  value={profile.requestedAccountType}
                />
                <Box sx={{ mt: 1 }}>
                  <Chip
                    label={`KYC: ${profile.kycStatus}`}
                    size="small"
                    sx={{ mr: 1 }}
                  />
                  <Chip
                    label={`Customer: ${profile.customerStatus}`}
                    size="small"
                    color={
                      profile.customerStatus === 'APPROVED'
                        ? 'success'
                        : 'default'
                    }
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
}
