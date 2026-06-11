import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
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
import { nomineeApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function NomineesPage() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const load = useCallback(async () => {
    try {
      const { data } = await nomineeApi.list();
      setRows(data.data || []);
    } catch (err) {
      setError(extractError(err, 'Could not load nominees'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onAdd = async (values) => {
    setError('');
    setSuccess('');
    try {
      await nomineeApi.add({
        ...values,
        sharePercent: values.sharePercent
          ? Number(values.sharePercent)
          : undefined,
      });
      setSuccess('Nominee added');
      reset();
      load();
    } catch (err) {
      setError(extractError(err, 'Could not add nominee'));
    }
  };

  const onRemove = async (id) => {
    try {
      await nomineeApi.remove(id);
      load();
    } catch (err) {
      setError(extractError(err, 'Could not remove nominee'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Nominees
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
                Add nominee
              </Typography>
              <form onSubmit={handleSubmit(onAdd)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    label="Name"
                    fullWidth
                    {...register('name', { required: 'Required' })}
                    error={Boolean(errors.name)}
                    helperText={errors.name?.message}
                  />
                  <TextField
                    label="Relationship"
                    fullWidth
                    {...register('relationship')}
                  />
                  <TextField
                    type="date"
                    label="Date of birth"
                    InputLabelProps={{ shrink: true }}
                    fullWidth
                    {...register('dateOfBirth')}
                  />
                  <TextField
                    label="Share percent (default 100)"
                    type="number"
                    inputProps={{ step: '0.01', min: '0.01', max: '100' }}
                    fullWidth
                    {...register('sharePercent')}
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
                  <TableCell>Name</TableCell>
                  <TableCell>Relationship</TableCell>
                  <TableCell>DOB</TableCell>
                  <TableCell align="right">Share %</TableCell>
                  <TableCell></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      No nominees yet
                    </TableCell>
                  </TableRow>
                )}
                {rows.map((n) => (
                  <TableRow key={n.id}>
                    <TableCell>{n.name}</TableCell>
                    <TableCell>{n.relationship || '-'}</TableCell>
                    <TableCell>{n.dateOfBirth || '-'}</TableCell>
                    <TableCell align="right">{n.sharePercent}</TableCell>
                    <TableCell>
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => onRemove(n.id)}
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
