import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
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
import { ticketApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

export default function TicketQueuePage() {
  const [status, setStatus] = useState('OPEN');
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editing, setEditing] = useState(null);
  const [newStatus, setNewStatus] = useState('IN_PROGRESS');
  const [resolution, setResolution] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await ticketApi.list(status, 0, 100);
      setRows(data.data?.content || []);
    } catch (err) {
      setError(extractError(err, 'Could not load tickets'));
    }
  }, [status]);

  useEffect(() => {
    load();
  }, [load]);

  const submit = async () => {
    if (!editing) return;
    try {
      await ticketApi.resolve(editing.id, {
        status: newStatus,
        resolution: resolution || undefined,
      });
      setSuccess('Ticket updated');
      setEditing(null);
      setResolution('');
      load();
    } catch (err) {
      setError(extractError(err, 'Update failed'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Ticket queue
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
              <TableCell>#</TableCell>
              <TableCell>Customer</TableCell>
              <TableCell>Subject</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Updated</TableCell>
              <TableCell></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No tickets in this status
                </TableCell>
              </TableRow>
            )}
            {rows.map((t) => (
              <TableRow key={t.id}>
                <TableCell>{t.id}</TableCell>
                <TableCell>
                  {t.customerFullName} ({t.customerUsername})
                </TableCell>
                <TableCell>{t.subject}</TableCell>
                <TableCell>
                  <Chip size="small" label={t.status} />
                </TableCell>
                <TableCell>
                  {t.updatedAt ? new Date(t.updatedAt).toLocaleString() : ''}
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    onClick={() => {
                      setEditing(t);
                      setNewStatus('IN_PROGRESS');
                      setResolution(t.resolution || '');
                    }}
                  >
                    Update
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={Boolean(editing)} onClose={() => setEditing(null)} fullWidth maxWidth="sm">
        <DialogTitle>Update ticket #{editing?.id}</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2}>
            <Typography variant="body2">
              <strong>{editing?.subject}</strong>
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {editing?.description}
            </Typography>
            <TextField
              select
              label="New status"
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value)}
            >
              {STATUSES.map((s) => (
                <MenuItem key={s} value={s}>
                  {s}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Resolution"
              multiline
              minRows={3}
              value={resolution}
              onChange={(e) => setResolution(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditing(null)}>Cancel</Button>
          <Button variant="contained" onClick={submit}>
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
