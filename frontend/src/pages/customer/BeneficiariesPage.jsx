import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { beneficiaryApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function BeneficiariesPage() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const load = useCallback(async () => {
    try {
      const { data } = await beneficiaryApi.list();
      setRows(data.data || []);
    } catch (err) {
      setError(extractError(err, 'Could not load beneficiaries'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onAdd = async (values) => {
    setError('');
    setSuccess('');
    try {
      await beneficiaryApi.add(values);
      setSuccess('Beneficiary added');
      reset();
      load();
    } catch (err) {
      setError(extractError(err, 'Could not add beneficiary'));
    }
  };

  const onRemove = async (id) => {
    setError('');
    try {
      await beneficiaryApi.remove(id);
      load();
    } catch (err) {
      setError(extractError(err, 'Could not remove beneficiary'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Beneficiaries
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="overline" color="primary">
                Add beneficiary
              </Typography>
              <form onSubmit={handleSubmit(onAdd)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    label="Nickname (optional)"
                    fullWidth
                    {...register('nickname')}
                  />
                  <TextField
                    label="Account number"
                    fullWidth
                    {...register('accountNumber', {
                      required: 'Required',
                      pattern: {
                        value: /^[0-9]{6,20}$/,
                        message: '6-20 digits',
                      },
                    })}
                    error={Boolean(errors.accountNumber)}
                    helperText={errors.accountNumber?.message}
                  />
                  <TextField
                    label="Beneficiary name"
                    fullWidth
                    {...register('beneficiaryName', { required: 'Required' })}
                    error={Boolean(errors.beneficiaryName)}
                    helperText={errors.beneficiaryName?.message}
                  />
                  <TextField
                    label="Bank name (optional)"
                    fullWidth
                    {...register('bankName')}
                  />
                  <TextField
                    label="IFSC (optional)"
                    fullWidth
                    {...register('ifsc', {
                      pattern: {
                        value: /^[A-Z]{4}0[A-Z0-9]{6}$/,
                        message: '11 chars, e.g. SBIN0001234',
                      },
                    })}
                    error={Boolean(errors.ifsc)}
                    helperText={errors.ifsc?.message}
                  />
                  <Button type="submit" variant="contained" disabled={isSubmitting}>
                    Add
                  </Button>
                </Stack>
              </form>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={7}>
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nickname</TableCell>
                  <TableCell>Account</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      No beneficiaries yet
                    </TableCell>
                  </TableRow>
                )}
                {rows.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell>{b.nickname || '-'}</TableCell>
                    <TableCell>{b.accountNumber}</TableCell>
                    <TableCell>{b.beneficiaryName}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={b.verified ? 'VERIFIED' : 'UNVERIFIED'}
                        color={b.verified ? 'success' : 'warning'}
                      />
                    </TableCell>
                    <TableCell>
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => onRemove(b.id)}
                        title="Remove"
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>
      </Grid>
    </Box>
  );
}
