import { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import { accountApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const MASKED_BALANCE = '********';

export default function CustomerDashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState('');
  const [revealedBalance, setRevealedBalance] = useState(null);

  const load = useCallback(async () => {
    setError('');
    try {
      const { data } = await accountApi.getDashboard();
      setDashboard(data.data);
    } catch (err) {
      setError(extractError(err, 'Could not load dashboard'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const showBalance = async () => {
    try {
      const { data } = await accountApi.getBalance();
      setRevealedBalance(data.data);
    } catch (err) {
      setError(extractError(err, 'Could not reveal balance'));
    }
  };

  const hideBalance = () => setRevealedBalance(null);

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="overline" color="text.secondary">
                Account summary
              </Typography>
              <Typography variant="h6">{dashboard?.customerName}</Typography>

              <Stack direction="row" spacing={4} sx={{ mt: 2 }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Customer ID
                  </Typography>
                  <Typography variant="subtitle1">
                    {dashboard?.customerId || '-'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Account number
                  </Typography>
                  <Typography variant="subtitle1">
                    {dashboard?.accountNumber || '-'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Account type
                  </Typography>
                  <Typography variant="subtitle1">
                    {dashboard?.accountType || '-'}
                  </Typography>
                </Box>
              </Stack>

              <Box sx={{ mt: 4 }}>
                <Typography variant="caption" color="text.secondary">
                  Balance
                </Typography>
                <Stack direction="row" alignItems="center" spacing={2}>
                  <Typography variant="h3">
                    {revealedBalance
                      ? `${revealedBalance.currency} ${Number(
                          revealedBalance.balance
                        ).toFixed(2)}`
                      : MASKED_BALANCE}
                  </Typography>
                  {revealedBalance ? (
                    <Button
                      onClick={hideBalance}
                      startIcon={<VisibilityOffIcon />}
                    >
                      Hide
                    </Button>
                  ) : (
                    <Button
                      onClick={showBalance}
                      startIcon={<VisibilityIcon />}
                      variant="outlined"
                    >
                      Show Balance
                    </Button>
                  )}
                </Stack>
              </Box>

              <Stack direction="row" spacing={1} sx={{ mt: 3 }}>
                <Chip
                  label={dashboard?.accountStatus || 'UNKNOWN'}
                  color={
                    dashboard?.accountStatus === 'ACTIVE'
                      ? 'success'
                      : 'warning'
                  }
                  size="small"
                />
                {dashboard?.dailyTransferLimit && (
                  <Chip
                    label={`Daily limit ${dashboard.dailyTransferLimit}`}
                    size="small"
                  />
                )}
                {dashboard?.unreadNotifications > 0 && (
                  <Chip
                    label={`${dashboard.unreadNotifications} unread notifications`}
                    color="info"
                    size="small"
                  />
                )}
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="overline" color="text.secondary">
                Quick actions
              </Typography>
              <Stack spacing={1.5} sx={{ mt: 1 }}>
                <Button
                  variant="contained"
                  component={RouterLink}
                  to="/customer/services/transfer"
                >
                  Send money
                </Button>
                <Button
                  variant="outlined"
                  component={RouterLink}
                  to="/customer/services/transactions"
                >
                  View transactions
                </Button>
                <Button
                  variant="outlined"
                  component={RouterLink}
                  to="/customer/services/cards"
                >
                  Apply for a card
                </Button>
                <Button
                  variant="outlined"
                  component={RouterLink}
                  to="/customer/services/loans"
                >
                  Apply for a loan
                </Button>
                <Button
                  component={RouterLink}
                  to="/customer/services"
                  color="primary"
                >
                  See all services
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
