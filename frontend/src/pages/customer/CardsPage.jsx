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
import { cardApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const STATUS_COLOR = {
  PENDING: 'default',
  UNDER_REVIEW: 'info',
  APPROVED: 'success',
  REJECTED: 'error',
};

export default function CardsPage() {
  const [cards, setCards] = useState([]);
  const [apps, setApps] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({
    defaultValues: { cardType: 'DEBIT' },
  });

  const load = useCallback(async () => {
    try {
      const [cardsRes, appsRes] = await Promise.all([
        cardApi.myCards(),
        cardApi.myApplications(),
      ]);
      setCards(cardsRes.data.data || []);
      setApps(appsRes.data.data || []);
    } catch (err) {
      setError(extractError(err, 'Could not load card data'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onApply = async (values) => {
    setError('');
    setSuccess('');
    try {
      await cardApi.apply(values);
      setSuccess('Card application submitted. Status: PENDING.');
      load();
    } catch (err) {
      setError(extractError(err, 'Could not apply for card'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Cards
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
                Apply for a card
              </Typography>
              <form onSubmit={handleSubmit(onApply)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    select
                    label="Card type"
                    defaultValue="DEBIT"
                    {...register('cardType')}
                  >
                    <MenuItem value="DEBIT">Debit Card</MenuItem>
                    <MenuItem value="CREDIT">Credit Card</MenuItem>
                  </TextField>
                  <Button type="submit" variant="contained" disabled={isSubmitting}>
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
                  <TableCell>Status</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell>Notes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {apps.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={4} align="center">
                      No applications
                    </TableCell>
                  </TableRow>
                )}
                {apps.map((a) => (
                  <TableRow key={a.id}>
                    <TableCell>{a.cardType}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={a.status}
                        color={STATUS_COLOR[a.status]}
                      />
                    </TableCell>
                    <TableCell>
                      {new Date(a.createdAt).toLocaleString()}
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
            My cards
          </Typography>
          {cards.length === 0 ? (
            <Alert severity="info">
              {apps.some((a) =>
                ['PENDING', 'UNDER_REVIEW'].includes(a.status)
              )
                ? 'Card Application Under Review.  Card details will appear here once approved.'
                : 'No cards yet. Apply above.'}
            </Alert>
          ) : (
            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Card</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Expiry</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Daily limit</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cards.map((c) => (
                    <TableRow key={c.id}>
                      <TableCell>{c.maskedNumber}</TableCell>
                      <TableCell>{c.cardType}</TableCell>
                      <TableCell>{c.expiryDate}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={c.status}
                          color={c.status === 'ACTIVE' ? 'success' : 'default'}
                        />
                      </TableCell>
                      <TableCell align="right">{c.dailyLimit}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Grid>
      </Grid>
    </Box>
  );
}
