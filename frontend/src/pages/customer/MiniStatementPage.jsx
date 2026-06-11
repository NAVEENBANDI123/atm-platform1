import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { transactionApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function MiniStatementPage() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    transactionApi
      .miniStatement()
      .then(({ data }) => setRows(data.data || []))
      .catch((err) => setError(extractError(err, 'Could not load')));
  }, []);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>
        Mini statement (last 5)
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Date</TableCell>
              <TableCell>Type</TableCell>
              <TableCell align="right">Amount</TableCell>
              <TableCell align="right">Balance</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} align="center">
                  No transactions yet
                </TableCell>
              </TableRow>
            )}
            {rows.map((t) => (
              <TableRow key={t.id}>
                <TableCell>{new Date(t.createdAt).toLocaleString()}</TableCell>
                <TableCell>
                  <Chip label={t.type} size="small" />
                </TableCell>
                <TableCell align="right">
                  {Number(t.amount).toFixed(2)}
                </TableCell>
                <TableCell align="right">
                  {Number(t.balanceAfter).toFixed(2)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
