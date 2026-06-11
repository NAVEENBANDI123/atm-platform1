import { useCallback, useEffect, useState } from 'react';
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

const STATUSES = ['PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SUSPENDED'];
const COLOR = {
  PENDING_APPROVAL: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  SUSPENDED: 'default',
};

export default function PendingCustomersPage() {
  const [status, setStatus] = useState('PENDING_APPROVAL');
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selected, setSelected] = useState(null);
  const [rejectFor, setRejectFor] = useState(null);
  const [rejectReason, setRejectReason] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await adminApi.listCustomers(status, 0, 100);
      setRows(data.data?.content || []);
    } catch (err) {
      setError(extractError(err, 'Could not load customers'));
    }
  }, [status]);

  useEffect(() => {
    load();
  }, [load]);

  const approve = async (id) => {
    setError('');
    setSuccess('');
    try {
      await adminApi.approveCustomer(id);
      setSuccess('Customer approved.');
      load();
      if (selected?.id === id) setSelected(null);
    } catch (err) {
      setError(extractError(err, 'Approval failed'));
    }
  };

  const submitReject = async () => {
    if (!rejectFor || !rejectReason.trim()) return;
    setError('');
    setSuccess('');
    try {
      await adminApi.rejectCustomer(rejectFor, rejectReason.trim());
      setSuccess('Customer rejected.');
      setRejectFor(null);
      setRejectReason('');
      load();
      setSelected(null);
    } catch (err) {
      setError(extractError(err, 'Rejection failed'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Customer onboarding
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

      <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
        <TextField
          select
          label="Status"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          sx={{ width: 220 }}
        >
          {STATUSES.map((s) => (
            <MenuItem key={s} value={s}>
              {s}
            </MenuItem>
          ))}
        </TextField>
      </Stack>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Submitted</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Username</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Mobile</TableCell>
              <TableCell>Status</TableCell>
              <TableCell></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  Nothing to show
                </TableCell>
              </TableRow>
            )}
            {rows.map((c) => (
              <TableRow key={c.id} hover>
                <TableCell>
                  {c.submittedAt ? new Date(c.submittedAt).toLocaleString() : '-'}
                </TableCell>
                <TableCell>{c.fullName}</TableCell>
                <TableCell>{c.username}</TableCell>
                <TableCell>{c.email}</TableCell>
                <TableCell>{c.mobile}</TableCell>
                <TableCell>
                  <Chip
                    size="small"
                    label={c.customerStatus}
                    color={COLOR[c.customerStatus]}
                  />
                </TableCell>
                <TableCell>
                  <Stack direction="row" spacing={1}>
                    <Button size="small" onClick={() => setSelected(c)}>
                      Details
                    </Button>
                    {c.customerStatus === 'PENDING_APPROVAL' && (
                      <>
                        <Button
                          size="small"
                          variant="contained"
                          onClick={() => approve(c.id)}
                        >
                          Approve
                        </Button>
                        <Button
                          size="small"
                          color="error"
                          onClick={() => setRejectFor(c.id)}
                        >
                          Reject
                        </Button>
                      </>
                    )}
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={Boolean(selected)} onClose={() => setSelected(null)} maxWidth="md" fullWidth>
        <DialogTitle>Customer details</DialogTitle>
        <DialogContent dividers>
          {selected && (
            <Card variant="outlined">
              <CardContent>
                {[
                  ['Name', selected.fullName],
                  ['Username', selected.username],
                  ['Email', selected.email],
                  ['Mobile', selected.mobile],
                  ['Gender', selected.gender],
                  ['DOB', selected.dateOfBirth],
                  ['Aadhaar', selected.aadhaar],
                  ['PAN', selected.pan],
                  ['Address', `${selected.houseNumber || ''} ${selected.street || ''}, ${selected.area || ''}, ${selected.city || ''}, ${selected.state || ''}, ${selected.country || ''} - ${selected.postalCode || ''}`],
                  ['Account type', selected.requestedAccountType],
                  ['Status', selected.customerStatus],
                  ['Rejection reason', selected.rejectionReason],
                ].map(([k, v]) => (
                  <Box key={k} sx={{ mb: 1 }}>
                    <Typography variant="caption" color="text.secondary">
                      {k}
                    </Typography>
                    <Typography>{v || '-'}</Typography>
                  </Box>
                ))}
              </CardContent>
            </Card>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelected(null)}>Close</Button>
          {selected?.customerStatus === 'PENDING_APPROVAL' && (
            <>
              <Button color="error" onClick={() => setRejectFor(selected.id)}>
                Reject
              </Button>
              <Button variant="contained" onClick={() => approve(selected.id)}>
                Approve
              </Button>
            </>
          )}
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(rejectFor)} onClose={() => setRejectFor(null)}>
        <DialogTitle>Reject customer #{rejectFor}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            multiline
            minRows={3}
            label="Reason"
            fullWidth
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectFor(null)}>Cancel</Button>
          <Button
            color="error"
            variant="contained"
            disabled={!rejectReason.trim()}
            onClick={submitReject}
          >
            Reject
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
