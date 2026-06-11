import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { adminApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function AuditPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ username: '', entityType: '' });

  const load = useCallback(async () => {
    try {
      const params = { page, size };
      if (filters.username) params.username = filters.username;
      if (filters.entityType) params.entityType = filters.entityType;
      const { data } = await adminApi.audit(params);
      setData(data.data);
    } catch (err) {
      setError(extractError(err, 'Could not load audit log'));
    }
  }, [page, size, filters]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Audit log
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Username"
          value={filters.username}
          onChange={(e) =>
            setFilters({ ...filters, username: e.target.value })
          }
        />
        <TextField
          label="Entity type"
          value={filters.entityType}
          onChange={(e) =>
            setFilters({ ...filters, entityType: e.target.value })
          }
        />
        <Button onClick={() => setPage(0)}>Apply filter</Button>
      </Stack>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Time</TableCell>
              <TableCell>User</TableCell>
              <TableCell>Roles</TableCell>
              <TableCell>Action</TableCell>
              <TableCell>Entity</TableCell>
              <TableCell>Details</TableCell>
              <TableCell>Old → New</TableCell>
              <TableCell>IP</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {(data.content || []).map((a) => (
              <TableRow key={a.id} hover>
                <TableCell>
                  {a.createdAt ? new Date(a.createdAt).toLocaleString() : ''}
                </TableCell>
                <TableCell>{a.username}</TableCell>
                <TableCell>{a.userRole}</TableCell>
                <TableCell>{a.action}</TableCell>
                <TableCell>
                  {a.entityType} #{a.entityId}
                </TableCell>
                <TableCell>{a.details}</TableCell>
                <TableCell>
                  {a.oldValue ? (
                    <span>
                      <em>{a.oldValue}</em> → <strong>{a.newValue}</strong>
                    </span>
                  ) : (
                    a.newValue || ''
                  )}
                </TableCell>
                <TableCell>{a.ipAddress}</TableCell>
              </TableRow>
            ))}
            {(data.content || []).length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  No entries
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={data.totalElements || 0}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={size}
          onRowsPerPageChange={(e) => {
            setSize(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[20, 50, 100]}
        />
      </TableContainer>
    </Box>
  );
}
