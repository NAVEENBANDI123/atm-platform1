import { useCallback, useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
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
import { loanApi } from '../../features/account/accountApi.js';
import { selectIsSuperAdmin } from '../../features/auth/authSlice.js';
import { extractError } from '../../utils/error.js';

const STATUS_COLOR = {
  PENDING: 'default',
  UNDER_REVIEW: 'info',
  APPROVED: 'success',
  REJECTED: 'error',
};

export default function LoanQueuePage() {
  const isSuperAdmin = useSelector(selectIsSuperAdmin);
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [reviewFor, setReviewFor] = useState(null);
  const [recommendation, setRecommendation] = useState('RECOMMEND');
  const [reviewNote, setReviewNote] = useState('');
  const [rejectFor, setRejectFor] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [details, setDetails] = useState(null);

  const load = useCallback(async () => {
    try {
      const { data } = await loanApi.pending(0, 100);
      setRows(data.data?.content || []);
    } catch (err) {
      setError(extractError(err, 'Could not load loan queue'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const submitReview = async () => {
    if (!reviewFor) return;
    try {
      await loanApi.review(reviewFor.id, {
        recommendation,
        note: reviewNote || undefined,
      });
      setSuccess('Review recorded.');
      setReviewFor(null);
      setReviewNote('');
      setRecommendation('RECOMMEND');
      load();
    } catch (err) {
      setError(extractError(err, 'Could not record review'));
    }
  };

  const approve = async (id) => {
    try {
      await loanApi.approve(id);
      setSuccess('Loan approved and disbursed.');
      load();
    } catch (err) {
      setError(extractError(err, 'Could not approve'));
    }
  };

  const submitReject = async () => {
    if (!rejectFor || !rejectReason.trim()) return;
    try {
      await loanApi.reject(rejectFor, rejectReason.trim());
      setSuccess('Loan rejected.');
      setRejectFor(null);
      setRejectReason('');
      load();
    } catch (err) {
      setError(extractError(err, 'Could not reject'));
    }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Loan application queue
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
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Created</TableCell>
              <TableCell>Applicant</TableCell>
              <TableCell>Loan type</TableCell>
              <TableCell align="right">Amount</TableCell>
              <TableCell align="right">Tenure</TableCell>
              <TableCell>Status</TableCell>
              <TableCell></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  No pending applications
                </TableCell>
              </TableRow>
            )}
            {rows.map((a) => (
              <TableRow key={a.id}>
                <TableCell>{new Date(a.createdAt).toLocaleString()}</TableCell>
                <TableCell>{a.applicantName} ({a.applicantUsername})</TableCell>
                <TableCell>{a.loanType}</TableCell>
                <TableCell align="right">{a.amount}</TableCell>
                <TableCell align="right">{a.tenureMonths}</TableCell>
                <TableCell>
                  <Chip size="small" label={a.status} color={STATUS_COLOR[a.status]} />
                </TableCell>
                <TableCell>
                  <Stack direction="row" spacing={1}>
                    <Button size="small" onClick={() => setDetails(a)}>
                      Details
                    </Button>
                    <Button
                      size="small"
                      onClick={() => {
                        setReviewFor(a);
                        setRecommendation('RECOMMEND');
                        setReviewNote('');
                      }}
                    >
                      Review
                    </Button>
                    {isSuperAdmin && (
                      <>
                        <Button
                          size="small"
                          variant="contained"
                          onClick={() => approve(a.id)}
                        >
                          Approve
                        </Button>
                        <Button
                          size="small"
                          color="error"
                          onClick={() => setRejectFor(a.id)}
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

      <Dialog open={Boolean(details)} onClose={() => setDetails(null)} fullWidth maxWidth="sm">
        <DialogTitle>Loan details #{details?.id}</DialogTitle>
        <DialogContent dividers>
          {details && (
            <Stack spacing={1}>
              {[
                ['Applicant', `${details.applicantName} (${details.applicantUsername})`],
                ['Loan type', details.loanType],
                ['Amount', details.amount],
                ['Tenure', `${details.tenureMonths} months`],
                ['Monthly income', details.monthlyIncome],
                ['Employment', details.employmentType],
                ['Employer', details.employerName],
                ['Purpose', details.purpose],
                ['Status', details.status],
              ].map(([k, v]) => (
                <Box key={k}>
                  <Typography variant="caption" color="text.secondary">
                    {k}
                  </Typography>
                  <Typography>{v ?? '-'}</Typography>
                </Box>
              ))}
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetails(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(reviewFor)} onClose={() => setReviewFor(null)} fullWidth maxWidth="sm">
        <DialogTitle>Review loan application #{reviewFor?.id}</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2}>
            <TextField
              select
              label="Recommendation"
              value={recommendation}
              onChange={(e) => setRecommendation(e.target.value)}
            >
              <MenuItem value="RECOMMEND">Recommend for approval</MenuItem>
              <MenuItem value="RETURN">Return to customer</MenuItem>
            </TextField>
            <TextField
              label="Note (optional)"
              multiline
              minRows={3}
              value={reviewNote}
              onChange={(e) => setReviewNote(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReviewFor(null)}>Cancel</Button>
          <Button variant="contained" onClick={submitReview}>
            Submit review
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(rejectFor)} onClose={() => setRejectFor(null)}>
        <DialogTitle>Reject loan application #{rejectFor}</DialogTitle>
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
          <Button color="error" variant="contained" onClick={submitReject}>
            Reject
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
