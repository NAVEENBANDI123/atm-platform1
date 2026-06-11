import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
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
import { adminApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const ROLE_OPTIONS = [
  { value: 'ROLE_SUPER_ADMIN', label: 'Super Admin' },
  { value: 'ROLE_ACCOUNTANT', label: 'Accountant' },
  { value: 'ROLE_CASHIER', label: 'Cashier' },
  { value: 'ROLE_CARD_OFFICER', label: 'Card Officer' },
  { value: 'ROLE_LOAN_OFFICER', label: 'Loan Officer' },
];

export default function EmployeesPage() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editing, setEditing] = useState(null);
  const createForm = useForm({ defaultValues: { role: 'ROLE_ACCOUNTANT' } });
  const editForm = useForm();

  const load = useCallback(async () => {
    try {
      const { data } = await adminApi.listEmployees();
      setRows(data.data?.content || []);
    } catch (err) {
      setError(extractError(err, 'Could not load employees'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onCreate = async (values) => {
    setError('');
    setSuccess('');
    try {
      await adminApi.createEmployee(values);
      setSuccess('Employee created.');
      createForm.reset({ role: 'ROLE_ACCOUNTANT' });
      load();
    } catch (err) {
      setError(extractError(err, 'Could not create employee'));
    }
  };

  const onUpdate = async (values) => {
    setError('');
    setSuccess('');
    try {
      await adminApi.updateEmployee(editing.id, values);
      setSuccess('Employee updated.');
      setEditing(null);
      load();
    } catch (err) {
      setError(extractError(err, 'Could not update employee'));
    }
  };

  const toggle = async (e) => {
    setError('');
    setSuccess('');
    try {
      if (e.status === 'DISABLED') {
        await adminApi.enableEmployee(e.id);
      } else {
        await adminApi.disableEmployee(e.id);
      }
      load();
    } catch (err) {
      setError(extractError(err, 'Could not toggle employee'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Employees
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
                Create new employee
              </Typography>
              <form onSubmit={createForm.handleSubmit(onCreate)}>
                <Stack spacing={2} sx={{ mt: 1 }}>
                  <TextField
                    label="Full name"
                    fullWidth
                    {...createForm.register('fullName', { required: true })}
                  />
                  <TextField
                    label="Username"
                    fullWidth
                    {...createForm.register('username', { required: true })}
                  />
                  <TextField
                    label="Email"
                    type="email"
                    fullWidth
                    {...createForm.register('email', { required: true })}
                  />
                  <TextField
                    label="Mobile (10 digits)"
                    fullWidth
                    {...createForm.register('mobile', { required: true })}
                  />
                  <TextField
                    label="Initial password"
                    type="password"
                    fullWidth
                    {...createForm.register('password', {
                      required: true,
                      minLength: 8,
                    })}
                  />
                  <TextField
                    select
                    label="Role"
                    fullWidth
                    defaultValue="ROLE_ACCOUNTANT"
                    {...createForm.register('role', { required: true })}
                  >
                    {ROLE_OPTIONS.map((r) => (
                      <MenuItem key={r.value} value={r.value}>
                        {r.label}
                      </MenuItem>
                    ))}
                  </TextField>
                  <TextField
                    label="Designation"
                    fullWidth
                    {...createForm.register('designation')}
                  />
                  <TextField
                    label="Department"
                    fullWidth
                    {...createForm.register('department')}
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={createForm.formState.isSubmitting}
                  >
                    Create employee
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
                  <TableCell>Code</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Username</TableCell>
                  <TableCell>Roles</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      No employees
                    </TableCell>
                  </TableRow>
                )}
                {rows.map((e) => (
                  <TableRow key={e.id}>
                    <TableCell>{e.employeeCode || '-'}</TableCell>
                    <TableCell>{e.fullName}</TableCell>
                    <TableCell>{e.username}</TableCell>
                    <TableCell>
                      <Stack direction="row" spacing={0.5} flexWrap="wrap">
                        {(e.roles || []).map((r) => (
                          <Chip
                            key={r}
                            size="small"
                            label={r.replace('ROLE_', '')}
                          />
                        ))}
                      </Stack>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={e.status}
                        color={
                          e.status === 'ACTIVE'
                            ? 'success'
                            : e.status === 'DISABLED'
                              ? 'error'
                              : 'default'
                        }
                      />
                    </TableCell>
                    <TableCell>
                      <Stack direction="row" spacing={1}>
                        <Button
                          size="small"
                          onClick={() => {
                            setEditing(e);
                            editForm.reset({
                              fullName: e.fullName,
                              email: e.email,
                              mobile: e.mobile,
                              role: (e.roles || [])[0],
                              designation: e.designation,
                              department: e.department,
                            });
                          }}
                        >
                          Edit
                        </Button>
                        <Button
                          size="small"
                          color={e.status === 'DISABLED' ? 'success' : 'error'}
                          onClick={() => toggle(e)}
                        >
                          {e.status === 'DISABLED' ? 'Enable' : 'Disable'}
                        </Button>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>
      </Grid>

      <Dialog open={Boolean(editing)} onClose={() => setEditing(null)} fullWidth maxWidth="sm">
        <DialogTitle>Edit employee</DialogTitle>
        <form onSubmit={editForm.handleSubmit(onUpdate)}>
          <DialogContent dividers>
            <Stack spacing={2}>
              <TextField label="Full name" fullWidth {...editForm.register('fullName')} />
              <TextField label="Email" fullWidth {...editForm.register('email')} />
              <TextField label="Mobile" fullWidth {...editForm.register('mobile')} />
              <TextField select label="Role" fullWidth {...editForm.register('role')}>
                {ROLE_OPTIONS.map((r) => (
                  <MenuItem key={r.value} value={r.value}>
                    {r.label}
                  </MenuItem>
                ))}
              </TextField>
              <TextField label="Designation" fullWidth {...editForm.register('designation')} />
              <TextField label="Department" fullWidth {...editForm.register('department')} />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditing(null)}>Cancel</Button>
            <Button type="submit" variant="contained">
              Save
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
