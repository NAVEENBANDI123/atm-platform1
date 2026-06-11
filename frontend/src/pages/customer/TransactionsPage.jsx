import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { transactionApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const typeColor = {
  DEPOSIT: 'success',
  TRANSFER_IN: 'success',
  WITHDRAWAL: 'warning',
  TRANSFER_OUT: 'warning',
};

export default function TransactionsPage() {
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const res = await transactionApi.list(page, size);
      setData(res.data.data);
    } catch (err) {
      setError(extractError(err, 'Could not load transactions'));
    }
  }, [page, size]);

  useEffect(() => {
    load();
  }, [load]);

  const downloadCsv = async () => {
    try {
      const res = await transactionApi.downloadStatement();
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = 'statement.csv';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(extractError(err, 'Could not download statement'));
    }
  };

  return (
    <Box>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        sx={{ mb: 2 }}
      >
        <Typography variant="h5">Transaction history</Typography>
        <Button startIcon={<DownloadIcon />} onClick={downloadCsv}>
          Download CSV
        </Button>
      </Stack>
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
              <TableCell>Reference</TableCell>
              <TableCell>Type</TableCell>
              <TableCell align="right">Amount</TableCell>
              <TableCell align="right">Balance</TableCell>
              <TableCell>Description</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No transactions yet
                </TableCell>
              </TableRow>
            )}
            {data.content.map((tx) => (
              <TableRow key={tx.id}>
                <TableCell>
                  {new Date(tx.createdAt).toLocaleString()}
                </TableCell>
                <TableCell>{tx.reference}</TableCell>
                <TableCell>
                  <Chip
                    label={tx.type}
                    size="small"
                    color={typeColor[tx.type] || 'default'}
                  />
                </TableCell>
                <TableCell align="right">
                  {Number(tx.amount).toFixed(2)}
                </TableCell>
                <TableCell align="right">
                  {Number(tx.balanceAfter).toFixed(2)}
                </TableCell>
                <TableCell>{tx.description}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={data.totalElements}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={size}
          onRowsPerPageChange={(e) => {
            setSize(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[10, 20, 50]}
        />
      </TableContainer>
    </Box>
  );
}
