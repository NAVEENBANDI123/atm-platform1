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
  MenuItem,
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
import { loanApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const LOAN_TYPES = [
  { value: 'PERSONAL', label: 'Personal Loan' },
  { value: 'EDUCATION', label: 'Education Loan' },
  { value: 'VEHICLE', label: 'Vehicle Loan' },
  { value: 'HOME', label: 'Home Loan' },
];

const STATUS_COLOR = {
  PENDING: 'default',
  UNDER_REVIEW: 'info',
  APPROVED: 'success',
  REJECTED: 'error',
};

export default function LoansPage() {
  const [apps, setApps] = useState([]);
  const [loans, setLoans] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { loanType: 'PERSONAL', tenureMonths: 24 },
  });

  const load = useCallback(async () => {
    try {
      const [a, l] = await Promise.all([
        loanApi.myApplications(),
        loanApi.myLoans(),
      ]);
      setApps(a.data.data || []);
      setLoans(l.data.data || []);
    } catch (err) {
      setError(extractError(err, 'Could not load loan data'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onApply = async (values) => {
    setError('');
    setSuccess('');
    try {
      await loanApi.apply({
        ...values,
        amount: Number(values.amount),
        tenureMonths: Number(values.tenureMonths),
        monthlyIncome: Number(values.monthlyIncome),
      });
      setSuccess('Loan application submitted.');
      reset({ loanType: 'PERSONAL', tenureMonths: 24 });
      load();
    } catch (err) {
      setError(extractError(err, 'Could not apply'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Loans
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
                Apply for a loan
              </Typography>
              <form onSubmit={handleSubmit(onApply)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    select
                    label="Loan type"
                    defaultValue="PERSONAL"
                    {...register('loanType')}
                  >
                    {LOAN_TYPES.map((t) => (
                      <MenuItem key={t.value} value={t.value}>
                        {t.label}
                      </MenuItem>
                    ))}
                  </TextField>
                  <TextField
                    label="Amount"
                    type="number"
                    inputProps={{ step: '0.01', min: '1000' }}
                    {...register('amount', {
                      required: 'Required',
                      min: { value: 1000, message: 'Min 1000' },
                    })}
                    error={Boolean(errors.amount)}
                    helperText={errors.amount?.message}
                  />
                  <TextField
                    label="Tenure (months, 6-360)"
                    type="number"
                    inputProps={{ step: '1', min: '6', max: '360' }}
                    {...register('tenureMonths', {
                      required: 'Required',
                      min: { value: 6, message: 'Min 6 months' },
                      max: { value: 360, message: 'Max 360 months' },
                    })}
                    error={Boolean(errors.tenureMonths)}
                    helperText={errors.tenureMonths?.message}
                  />
                  <TextField
                    label="Monthly income"
                    type="number"
                    inputProps={{ step: '0.01', min: '1000' }}
                    {...register('monthlyIncome', { required: 'Required' })}
                    error={Boolean(errors.monthlyIncome)}
                    helperText={errors.monthlyIncome?.message}
                  />
                  <TextField
                    label="Employment type"
                    {...register('employmentType', { required: 'Required' })}
                    error={Boolean(errors.employmentType)}
                  />
                  <TextField
                    label="Employer name"
                    {...register('employerName')}
                  />
                  <TextField
                    label="Purpose"
                    multiline
                    minRows={2}
                    {...register('purpose', { required: 'Required' })}
                    error={Boolean(errors.purpose)}
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={isSubmitting}
                  >
                    Submit application
                  </Button>
                </Stack>
              </form>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={7}>
          <Typography variant="overline" color="text.secondary">
            My applications
          </Typography>
          <TableContainer component={Paper} sx={{ mb: 3 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Type</TableCell>
                  <TableCell align="right">Amount</TableCell>
                  <TableCell align="right">Tenure</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Notes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {apps.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      No applications
                    </TableCell>
                  </TableRow>
                )}
                {apps.map((a) => (
                  <TableRow key={a.id}>
                    <TableCell>{a.loanType}</TableCell>
                    <TableCell align="right">{a.amount}</TableCell>
                    <TableCell align="right">{a.tenureMonths}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={a.status}
                        color={STATUS_COLOR[a.status]}
                      />
                    </TableCell>
                    <TableCell>
                      {a.status === 'REJECTED'
                        ? a.rejectReason
                        : a.reviewNote || ''}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Typography variant="overline" color="text.secondary">
            My active loans
          </Typography>
          {loans.length === 0 ? (
            <Alert severity="info">No disbursed loans yet.</Alert>
          ) : (
            loans.map((l) => (
              <Card key={l.id} sx={{ mb: 2 }} variant="outlined">
                <CardContent>
                  <Typography variant="h6">{l.loanAccountNo}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Credited to {l.creditedToAccountNumber}
                  </Typography>
                  <Stack direction="row" spacing={3} sx={{ my: 1 }}>
                    <span>Principal: {l.principal}</span>
                    <span>Outstanding: {l.outstanding}</span>
                    <span>EMI: {l.emiAmount}</span>
                    <span>Rate: {l.interestRate}%</span>
                    <span>Tenure: {l.tenureMonths} mo</span>
                  </Stack>
                  <details>
                    <summary>Repayment schedule ({l.schedule?.length} EMIs)</summary>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>#</TableCell>
                          <TableCell>Due</TableCell>
                          <TableCell align="right">EMI</TableCell>
                          <TableCell align="right">Principal</TableCell>
                          <TableCell align="right">Interest</TableCell>
                          <TableCell align="right">Balance</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {(l.schedule || []).map((e) => (
                          <TableRow key={e.installmentNo}>
                            <TableCell>{e.installmentNo}</TableCell>
                            <TableCell>{e.dueDate}</TableCell>
                            <TableCell align="right">{e.emiAmount}</TableCell>
                            <TableCell align="right">{e.principalPart}</TableCell>
                            <TableCell align="right">{e.interestPart}</TableCell>
                            <TableCell align="right">{e.balance}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </details>
                </CardContent>
              </Card>
            ))
          )}
        </Grid>
      </Grid>
    </Box>
  );
}
