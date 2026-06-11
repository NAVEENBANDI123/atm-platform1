import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
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
import { depositApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function FixedDepositPage() {
  const [list, setList] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const load = () =>
    depositApi
      .mine()
      .then(({ data }) => setList(data.data || []))
      .catch((err) => setError(extractError(err, 'Could not load')));

  useEffect(() => {
    load();
  }, []);

  const onOpen = async (values) => {
    setError('');
    setSuccess('');
    try {
      await depositApi.openFd({
        principal: Number(values.principal),
        tenureMonths: Number(values.tenureMonths),
      });
      setSuccess('Fixed deposit opened.');
      reset();
      load();
    } catch (err) {
      setError(extractError(err, 'Could not open FD'));
    }
  };

  const fds = list.filter((d) => d.depositType === 'FIXED');

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Fixed Deposits
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
                Open a new FD
              </Typography>
              <form onSubmit={handleSubmit(onOpen)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    label="Principal (min 1000)"
                    type="number"
                    inputProps={{ step: '0.01', min: '1000' }}
                    {...register('principal', {
                      required: 'Required',
                      min: { value: 1000, message: 'Min 1000' },
                    })}
                    error={Boolean(errors.principal)}
                    helperText={errors.principal?.message}
                  />
                  <TextField
                    label="Tenure (months)"
                    type="number"
                    inputProps={{ step: '1', min: '3', max: '120' }}
                    {...register('tenureMonths', {
                      required: 'Required',
                      min: { value: 3, message: 'Min 3 months' },
                    })}
                    error={Boolean(errors.tenureMonths)}
                    helperText={errors.tenureMonths?.message}
                  />
                  <Button type="submit" variant="contained" disabled={isSubmitting}>
                    Open FD
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
                  <TableCell align="right">Principal</TableCell>
                  <TableCell align="right">Rate</TableCell>
                  <TableCell align="right">Tenure</TableCell>
                  <TableCell align="right">Maturity Amount</TableCell>
                  <TableCell>Maturity Date</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {fds.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      No FDs yet
                    </TableCell>
                  </TableRow>
                )}
                {fds.map((d) => (
                  <TableRow key={d.id}>
                    <TableCell align="right">{d.principal}</TableCell>
                    <TableCell align="right">{d.interestRate}%</TableCell>
                    <TableCell align="right">{d.tenureMonths}</TableCell>
                    <TableCell align="right">{d.maturityAmount}</TableCell>
                    <TableCell>{d.maturityDate}</TableCell>
                    <TableCell>{d.status}</TableCell>
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
