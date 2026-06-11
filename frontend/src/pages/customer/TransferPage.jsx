import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import {
  accountApi,
  beneficiaryApi,
} from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function TransferPage() {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm({ defaultValues: { mode: 'beneficiary' } });
  const mode = watch('mode');
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    beneficiaryApi
      .list()
      .then(({ data }) => setBeneficiaries(data.data || []))
      .catch(() => setBeneficiaries([]));
  }, []);

  const onSubmit = async (values) => {
    setError('');
    setSuccess('');
    const payload = {
      amount: Number(values.amount),
      description: values.description || undefined,
    };
    if (values.mode === 'beneficiary') {
      payload.beneficiaryId = Number(values.beneficiaryId);
    } else {
      payload.targetAccountNumber = values.targetAccountNumber;
    }
    try {
      const { data } = await accountApi.transfer(payload);
      setSuccess(
        `Transferred ${values.amount}. Reference: ${data.data?.reference || ''}`
      );
      reset({ mode: values.mode });
      setValue('mode', values.mode);
    } catch (err) {
      setError(extractError(err, 'Transfer failed'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Fund transfer
      </Typography>
      <Card sx={{ maxWidth: 540 }}>
        <CardContent>
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
          <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2}>
              <TextField
                select
                label="Send to"
                {...register('mode')}
                value={mode}
                onChange={(e) => setValue('mode', e.target.value)}
              >
                <MenuItem value="beneficiary">A saved beneficiary</MenuItem>
                <MenuItem value="number">A specific account number</MenuItem>
              </TextField>

              {mode === 'beneficiary' ? (
                <TextField
                  select
                  label="Beneficiary"
                  fullWidth
                  defaultValue=""
                  {...register('beneficiaryId', {
                    required: 'Choose a beneficiary',
                  })}
                  error={Boolean(errors.beneficiaryId)}
                  helperText={
                    errors.beneficiaryId?.message ||
                    (beneficiaries.length === 0
                      ? 'No beneficiaries yet - add one first'
                      : '')
                  }
                  disabled={beneficiaries.length === 0}
                >
                  {beneficiaries.map((b) => (
                    <MenuItem key={b.id} value={b.id}>
                      {b.beneficiaryName} - {b.accountNumber}{' '}
                      {b.verified ? '' : '(unverified)'}
                    </MenuItem>
                  ))}
                </TextField>
              ) : (
                <TextField
                  label="Target account number"
                  fullWidth
                  {...register('targetAccountNumber', {
                    required: 'Required',
                    pattern: { value: /^[0-9]{6,20}$/, message: '6-20 digits' },
                  })}
                  error={Boolean(errors.targetAccountNumber)}
                  helperText={errors.targetAccountNumber?.message}
                />
              )}

              <TextField
                label="Amount"
                type="number"
                inputProps={{ step: '0.01', min: '0' }}
                fullWidth
                {...register('amount', {
                  required: 'Required',
                  min: { value: 0.01, message: 'Greater than zero' },
                })}
                error={Boolean(errors.amount)}
                helperText={errors.amount?.message}
              />
              <TextField
                label="Description (optional)"
                fullWidth
                {...register('description')}
              />
              <Button type="submit" variant="contained" disabled={isSubmitting}>
                Transfer
              </Button>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
