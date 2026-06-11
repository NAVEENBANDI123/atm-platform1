import { Link as RouterLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Grid,
  Typography,
} from '@mui/material';
import {
  selectAuth,
  selectIsAccountant,
  selectIsCardOfficer,
  selectIsCashier,
  selectIsLoanOfficer,
  selectIsSuperAdmin,
} from '../../features/auth/authSlice.js';

export default function EmployeeDashboardPage() {
  const { user } = useSelector(selectAuth);
  const isSuperAdmin = useSelector(selectIsSuperAdmin);
  const isAccountant = useSelector(selectIsAccountant);
  const isCashier = useSelector(selectIsCashier);
  const isCardOfficer = useSelector(selectIsCardOfficer);
  const isLoanOfficer = useSelector(selectIsLoanOfficer);

  const tiles = [
    isAccountant && {
      title: 'Pending Customers',
      desc: 'Approve or reject new customer applications',
      to: '/employee/customers/pending',
    },
    isSuperAdmin && {
      title: 'Employee Management',
      desc: 'Create, edit and disable employee accounts',
      to: '/employee/employees',
    },
    isCardOfficer && {
      title: 'Card Queue',
      desc: 'Review pending card applications',
      to: '/employee/cards/queue',
    },
    isLoanOfficer && {
      title: 'Loan Queue',
      desc: 'Review pending loan applications',
      to: '/employee/loans/queue',
    },
    isCashier && {
      title: 'Cashier',
      desc: 'Deposit / withdraw on customer accounts',
      to: '/employee/cashier',
    },
    {
      title: 'Tickets',
      desc: 'Customer support tickets',
      to: '/employee/tickets',
    },
    isSuperAdmin && {
      title: 'Audit Log',
      desc: 'Read-only audit trail',
      to: '/employee/audit',
    },
  ].filter(Boolean);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 1 }}>
        Welcome, {user?.fullName}
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Roles: {(user?.roles || []).join(', ')}
      </Typography>
      <Grid container spacing={3}>
        {tiles.map((t) => (
          <Grid item xs={12} sm={6} md={4} key={t.title}>
            <Card variant="outlined">
              <CardActionArea component={RouterLink} to={t.to}>
                <CardContent>
                  <Typography variant="h6">{t.title}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {t.desc}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
