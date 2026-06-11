import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
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
import { ticketApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const STATUS_COLOR = {
  OPEN: 'info',
  IN_PROGRESS: 'warning',
  RESOLVED: 'success',
  CLOSED: 'default',
};

export default function TicketsPage() {
  const [tickets, setTickets] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const load = () =>
    ticketApi
      .mine()
      .then(({ data }) => setTickets(data.data || []))
      .catch((err) => setError(extractError(err, 'Could not load tickets')));

  useEffect(() => {
    load();
  }, []);

  const onCreate = async (values) => {
    setError('');
    setSuccess('');
    try {
      await ticketApi.create(values);
      setSuccess('Ticket created.');
      reset();
      load();
    } catch (err) {
      setError(extractError(err, 'Could not create ticket'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Support tickets
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
                Open a new ticket
              </Typography>
              <form onSubmit={handleSubmit(onCreate)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    label="Subject"
                    fullWidth
                    {...register('subject', { required: 'Required' })}
                    error={Boolean(errors.subject)}
                    helperText={errors.subject?.message}
                  />
                  <TextField
                    label="Description"
                    multiline
                    minRows={4}
                    fullWidth
                    {...register('description', { required: 'Required' })}
                    error={Boolean(errors.description)}
                    helperText={errors.description?.message}
                  />
                  <Button type="submit" variant="contained" disabled={isSubmitting}>
                    Submit
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
                  <TableCell>#</TableCell>
                  <TableCell>Subject</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Updated</TableCell>
                  <TableCell>Resolution</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {tickets.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      No tickets yet
                    </TableCell>
                  </TableRow>
                )}
                {tickets.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell>{t.id}</TableCell>
                    <TableCell>{t.subject}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={t.status}
                        color={STATUS_COLOR[t.status]}
                      />
                    </TableCell>
                    <TableCell>
                      {t.updatedAt ? new Date(t.updatedAt).toLocaleString() : ''}
                    </TableCell>
                    <TableCell>{t.resolution || ''}</TableCell>
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
