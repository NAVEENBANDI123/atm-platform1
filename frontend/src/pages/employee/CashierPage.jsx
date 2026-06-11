import { useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { cashierApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function CashierPage() {
  const [account, setAccount] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const lookupForm = useForm();
  const opForm = useForm();

  const onLookup = async (values) => {
    setError('');
    setSuccess('');
    setAccount(null);
    try {
      const { data } = await cashierApi.lookup(values.accountNumber);
      setAccount(data.data);
    } catch (err) {
      setError(extractError(err, 'Account not found'));
    }
  };

  const op = async (kind, values) => {
    if (!account) return;
    setError('');
    setSuccess('');
    try {
      const payload = {
        accountNumber: account.accountNumber,
        amount: Number(values.amount),
        description: values.description || undefined,
      };
      if (kind === 'deposit') {
        await cashierApi.deposit(payload);
      } else {
        await cashierApi.withdraw(payload);
      }
      setSuccess(
        `${kind === 'deposit' ? 'Deposited' : 'Withdrew'} ${values.amount} on ${account.accountNumber}`
      );
      opForm.reset();
      // refresh account
      const { data } = await cashierApi.lookup(account.accountNumber);
      setAccount(data.data);
    } catch (err) {
      setError(extractError(err, kind + ' failed'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Cash counter
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
                Lookup customer account
              </Typography>
              <form onSubmit={lookupForm.handleSubmit(onLookup)}>
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                  <TextField
                    label="Account number"
                    fullWidth
                    {...lookupForm.register('accountNumber', { required: true })}
                  />
                  <Button type="submit" variant="contained">
                    Lookup
                  </Button>
                </Stack>
              </form>
            </CardContent>
          </Card>
          {account && (
            <Card sx={{ mt: 2 }} variant="outlined">
              <CardContent>
                <Typography variant="overline" color="text.secondary">
                  {account.accountNumber}
                </Typography>
                <Typography variant="h5">
                  {account.currency} {Number(account.balance).toFixed(2)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {account.ownerName}
                </Typography>
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                  <Chip size="small" label={account.accountType} />
                  <Chip
                    size="small"
                    label={account.status}
                    color={
                      account.status === 'ACTIVE' ? 'success' : 'warning'
                    }
                  />
                </Stack>
              </CardContent>
            </Card>
          )}
        </Grid>

        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="overline" color="primary">
                Operation
              </Typography>
              <form>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    label="Amount"
                    type="number"
                    inputProps={{ step: '0.01', min: '0.01' }}
                    {...opForm.register('amount', { required: true })}
                    disabled={!account}
                  />
                  <TextField
                    label="Description (optional)"
                    {...opForm.register('description')}
                    disabled={!account}
                  />
                  <Stack direction="row" spacing={2}>
                    <Button
                      variant="contained"
                      color="success"
                      disabled={!account}
                      onClick={opForm.handleSubmit((v) => op('deposit', v))}
                    >
                      Deposit
                    </Button>
                    <Button
                      variant="contained"
                      color="warning"
                      disabled={!account}
                      onClick={opForm.handleSubmit((v) => op('withdraw', v))}
                    >
                      Withdraw
                    </Button>
                  </Stack>
                </Stack>
              </form>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
