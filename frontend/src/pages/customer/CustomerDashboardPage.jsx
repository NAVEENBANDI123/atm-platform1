import { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardActionArea,
  CardContent,
  Chip,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import SendIcon from '@mui/icons-material/Send';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import RequestQuoteIcon from '@mui/icons-material/RequestQuote';
import GroupIcon from '@mui/icons-material/Group';
import SavingsIcon from '@mui/icons-material/Savings';
import GridViewIcon from '@mui/icons-material/GridView';
import { accountApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

const MASKED_BALANCE = '* * * *  * * * *';

export default function CustomerDashboardPage() {
  const theme = useTheme();
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

  const quickActions = [
    {
      icon: <SendIcon />,
      title: 'Send money',
      desc: 'Transfer to a beneficiary or any account',
      to: '/customer/services/transfer',
    },
    {
      icon: <ReceiptLongIcon />,
      title: 'Transactions',
      desc: 'See your full statement and download it',
      to: '/customer/services/transactions',
    },
    {
      icon: <CreditCardIcon />,
      title: 'Cards',
      desc: 'Apply for debit / credit cards',
      to: '/customer/services/cards',
    },
    {
      icon: <RequestQuoteIcon />,
      title: 'Loans',
      desc: 'Personal, education, vehicle or home',
      to: '/customer/services/loans',
    },
    {
      icon: <SavingsIcon />,
      title: 'Deposits',
      desc: 'Open an FD or RD',
      to: '/customer/services/fd',
    },
    {
      icon: <GroupIcon />,
      title: 'Beneficiaries',
      desc: 'Manage your saved payees',
      to: '/customer/services/beneficiaries',
    },
  ];

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Hero / Account summary */}
      <Card
        sx={{
          color: '#fff',
          backgroundImage: theme.custom.customerGradient,
          border: 'none',
          mb: 3,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <CardContent sx={{ p: { xs: 3, md: 4 } }}>
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            justifyContent="space-between"
            alignItems={{ xs: 'flex-start', md: 'flex-end' }}
            spacing={3}
          >
            <Box>
              <Typography variant="overline" sx={{ opacity: 0.85 }}>
                Welcome back
              </Typography>
              <Typography
                variant="h4"
                sx={{ fontWeight: 800, lineHeight: 1.1, mt: 0.5 }}
              >
                {dashboard?.customerName || ''}
              </Typography>
              <Stack
                direction="row"
                spacing={3}
                sx={{ mt: 2.5, flexWrap: 'wrap' }}
              >
                <HeroStat
                  label="Customer ID"
                  value={dashboard?.customerId || '-'}
                />
                <HeroStat
                  label="Account number"
                  value={dashboard?.accountNumber || '-'}
                />
                <HeroStat
                  label="Account type"
                  value={dashboard?.accountType || '-'}
                />
              </Stack>
            </Box>

            {/* Balance card */}
            <Box
              sx={{
                bgcolor: alpha('#ffffff', 0.14),
                border: '1px solid rgba(255,255,255,0.25)',
                borderRadius: 3,
                px: 3,
                py: 2.5,
                minWidth: 280,
                backdropFilter: 'blur(6px)',
              }}
            >
              <Typography variant="overline" sx={{ opacity: 0.85 }}>
                Available balance
              </Typography>
              <Typography
                variant="h3"
                sx={{
                  fontWeight: 800,
                  letterSpacing: revealedBalance ? '-0.02em' : '0.18em',
                  mt: 0.5,
                }}
              >
                {revealedBalance
                  ? `${revealedBalance.currency} ${Number(
                      revealedBalance.balance
                    ).toFixed(2)}`
                  : MASKED_BALANCE}
              </Typography>
              <Stack direction="row" spacing={1} sx={{ mt: 1.5 }}>
                {revealedBalance ? (
                  <Button
                    onClick={hideBalance}
                    startIcon={<VisibilityOffIcon />}
                    size="small"
                    variant="contained"
                    color="inherit"
                    sx={{ color: 'primary.main', bgcolor: '#fff' }}
                  >
                    Hide
                  </Button>
                ) : (
                  <Button
                    onClick={showBalance}
                    startIcon={<VisibilityIcon />}
                    size="small"
                    variant="contained"
                    color="inherit"
                    sx={{ color: 'primary.main', bgcolor: '#fff' }}
                  >
                    Show Balance
                  </Button>
                )}
              </Stack>
            </Box>
          </Stack>

          <Stack direction="row" spacing={1} sx={{ mt: 3, flexWrap: 'wrap' }}>
            <Chip
              size="small"
              label={dashboard?.accountStatus || 'UNKNOWN'}
              sx={{
                bgcolor:
                  dashboard?.accountStatus === 'ACTIVE'
                    ? 'rgba(16,185,129,0.25)'
                    : 'rgba(245,158,11,0.25)',
                color: '#fff',
                border: '1px solid rgba(255,255,255,0.3)',
              }}
            />
            {dashboard?.dailyTransferLimit && (
              <Chip
                size="small"
                label={`Daily limit  ${Number(
                  dashboard.dailyTransferLimit
                ).toLocaleString()}`}
                sx={{
                  bgcolor: 'rgba(255,255,255,0.18)',
                  color: '#fff',
                  border: '1px solid rgba(255,255,255,0.25)',
                }}
              />
            )}
            {dashboard?.unreadNotifications > 0 && (
              <Chip
                size="small"
                component={RouterLink}
                to="/customer/notifications"
                clickable
                label={`${dashboard.unreadNotifications} unread notifications`}
                sx={{
                  bgcolor: 'rgba(255,255,255,0.18)',
                  color: '#fff',
                  border: '1px solid rgba(255,255,255,0.25)',
                }}
              />
            )}
          </Stack>
        </CardContent>
      </Card>

      {/* Quick actions */}
      <Stack
        direction="row"
        alignItems="center"
        justifyContent="space-between"
        sx={{ mb: 1.5 }}
      >
        <Typography variant="overline" color="text.secondary">
          Quick actions
        </Typography>
        <Button
          component={RouterLink}
          to="/customer/services"
          size="small"
          startIcon={<GridViewIcon />}
        >
          See all services
        </Button>
      </Stack>

      <Grid container spacing={2.5}>
        {quickActions.map((q) => (
          <Grid item xs={12} sm={6} md={4} key={q.title}>
            <Card
              sx={{
                height: '100%',
                transition: 'transform 160ms ease, box-shadow 160ms ease',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow:
                    '0 18px 40px -22px rgba(15,23,42,0.30), 0 6px 14px -10px rgba(15,23,42,0.18)',
                },
              }}
            >
              <CardActionArea
                component={RouterLink}
                to={q.to}
                sx={{ height: '100%', p: 1 }}
              >
                <CardContent>
                  <Stack direction="row" spacing={2} alignItems="center">
                    <Box
                      sx={{
                        width: 44,
                        height: 44,
                        borderRadius: 2,
                        bgcolor: alpha(theme.palette.primary.main, 0.12),
                        color: 'primary.main',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {q.icon}
                    </Box>
                    <Box>
                      <Typography sx={{ fontWeight: 700 }}>{q.title}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {q.desc}
                      </Typography>
                    </Box>
                  </Stack>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}

function HeroStat({ label, value }) {
  return (
    <Box>
      <Typography variant="caption" sx={{ opacity: 0.85 }}>
        {label}
      </Typography>
      <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
        {value}
      </Typography>
    </Box>
  );
}
