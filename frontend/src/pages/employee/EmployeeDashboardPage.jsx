import { Link as RouterLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Chip,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';
import GroupAddIcon from '@mui/icons-material/GroupAdd';
import BadgeIcon from '@mui/icons-material/Badge';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import RequestQuoteIcon from '@mui/icons-material/RequestQuote';
import PointOfSaleIcon from '@mui/icons-material/PointOfSale';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import AssignmentIcon from '@mui/icons-material/Assignment';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import {
  selectAuth,
  selectIsAccountant,
  selectIsCardOfficer,
  selectIsCashier,
  selectIsLoanOfficer,
  selectIsSuperAdmin,
} from '../../features/auth/authSlice.js';

export default function EmployeeDashboardPage() {
  const theme = useTheme();
  const { user } = useSelector(selectAuth);
  const isSuperAdmin = useSelector(selectIsSuperAdmin);
  const isAccountant = useSelector(selectIsAccountant);
  const isCashier = useSelector(selectIsCashier);
  const isCardOfficer = useSelector(selectIsCardOfficer);
  const isLoanOfficer = useSelector(selectIsLoanOfficer);

  const tiles = [
    isAccountant && {
      icon: <GroupAddIcon />,
      title: 'Pending Customers',
      desc: 'Approve or reject new customer applications',
      to: '/employee/customers/pending',
    },
    isSuperAdmin && {
      icon: <BadgeIcon />,
      title: 'Employee Management',
      desc: 'Create, edit and disable employee accounts',
      to: '/employee/employees',
    },
    isCardOfficer && {
      icon: <CreditCardIcon />,
      title: 'Card Queue',
      desc: 'Review pending card applications',
      to: '/employee/cards/queue',
    },
    isLoanOfficer && {
      icon: <RequestQuoteIcon />,
      title: 'Loan Queue',
      desc: 'Review pending loan applications',
      to: '/employee/loans/queue',
    },
    isCashier && {
      icon: <PointOfSaleIcon />,
      title: 'Cashier',
      desc: 'Deposit / withdraw on customer accounts',
      to: '/employee/cashier',
    },
    {
      icon: <SupportAgentIcon />,
      title: 'Support Tickets',
      desc: 'Handle customer support requests',
      to: '/employee/tickets',
    },
    isSuperAdmin && {
      icon: <AssignmentIcon />,
      title: 'Audit Log',
      desc: 'Read-only system audit trail',
      to: '/employee/audit',
    },
  ].filter(Boolean);

  const roles = (user?.roles || [])
    .filter((r) =>
      [
        'ROLE_SUPER_ADMIN',
        'ROLE_ADMIN',
        'ROLE_ACCOUNTANT',
        'ROLE_CASHIER',
        'ROLE_CARD_OFFICER',
        'ROLE_LOAN_OFFICER',
      ].includes(r)
    )
    .map((r) => r.replace('ROLE_', '').replace('_', ' '));

  return (
    <Box>
      <Card
        sx={{
          color: '#fff',
          backgroundImage: theme.custom.employeeGradient,
          border: 'none',
          mb: 3,
        }}
      >
        <CardContent sx={{ p: { xs: 3, md: 4 } }}>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <VerifiedUserIcon />
            <Typography variant="overline" sx={{ opacity: 0.85 }}>
              Staff console
            </Typography>
          </Stack>
          <Typography variant="h4" sx={{ fontWeight: 800, mt: 0.5 }}>
            Welcome, {user?.fullName || ''}
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 2, flexWrap: 'wrap' }}>
            {roles.map((r) => (
              <Chip
                key={r}
                label={r}
                size="small"
                sx={{
                  bgcolor: 'rgba(255,255,255,0.18)',
                  color: '#fff',
                  border: '1px solid rgba(255,255,255,0.30)',
                }}
              />
            ))}
          </Stack>
          <Typography sx={{ mt: 2, opacity: 0.92, maxWidth: 720 }}>
            Pick an area to begin. Only sections matching your role are shown
            below; the side links in the header give you the same options at any
            time.
          </Typography>
        </CardContent>
      </Card>

      <Typography variant="overline" color="text.secondary">
        What would you like to do?
      </Typography>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        {tiles.map((t) => (
          <Grid item xs={12} sm={6} md={4} key={t.title}>
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
                to={t.to}
                sx={{ height: '100%' }}
              >
                <CardContent>
                  <Stack direction="row" spacing={2} alignItems="flex-start">
                    <Box
                      sx={{
                        width: 44,
                        height: 44,
                        borderRadius: 2,
                        bgcolor: alpha(theme.palette.secondary.main, 0.12),
                        color: 'secondary.main',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {t.icon}
                    </Box>
                    <Box>
                      <Typography sx={{ fontWeight: 700 }}>{t.title}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {t.desc}
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
