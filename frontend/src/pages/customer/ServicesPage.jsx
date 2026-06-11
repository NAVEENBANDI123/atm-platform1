import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Grid,
  Typography,
} from '@mui/material';

const SECTIONS = [
  {
    title: 'Account Services',
    items: [
      { label: 'View Profile', to: '/customer/services/profile' },
      { label: 'Update Profile', to: '/customer/services/profile' },
      { label: 'Change Password', to: '/customer/services/change-password' },
      { label: 'Download Statement', to: '/customer/services/transactions' },
      { label: 'Mini Statement', to: '/customer/services/mini-statement' },
    ],
  },
  {
    title: 'Transaction Services',
    items: [
      { label: 'Fund Transfer', to: '/customer/services/transfer' },
      { label: 'Transaction History', to: '/customer/services/transactions' },
      { label: 'Beneficiary Management', to: '/customer/services/beneficiaries' },
    ],
  },
  {
    title: 'Card Services',
    items: [
      { label: 'Apply Debit Card', to: '/customer/services/cards' },
      { label: 'Apply Credit Card', to: '/customer/services/cards' },
      { label: 'Card Status Tracking', to: '/customer/services/cards' },
    ],
  },
  {
    title: 'Loan Services',
    items: [
      { label: 'Personal Loan', to: '/customer/services/loans' },
      { label: 'Education Loan', to: '/customer/services/loans' },
      { label: 'Vehicle Loan', to: '/customer/services/loans' },
      { label: 'Home Loan', to: '/customer/services/loans' },
    ],
  },
  {
    title: 'Other Services',
    items: [
      { label: 'Cheque Book Request', to: '/customer/services/tickets' },
      { label: 'Nominee Management', to: '/customer/services/nominees' },
      { label: 'Fixed Deposit Request', to: '/customer/services/fd' },
      { label: 'Recurring Deposit Request', to: '/customer/services/rd' },
      { label: 'Complaint / Ticket System', to: '/customer/services/tickets' },
    ],
  },
];

export default function ServicesPage() {
  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Services
      </Typography>
      <Grid container spacing={3}>
        {SECTIONS.map((section) => (
          <Grid item xs={12} md={6} key={section.title}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="overline" color="primary">
                  {section.title}
                </Typography>
                <Grid container spacing={1} sx={{ mt: 1 }}>
                  {section.items.map((item) => (
                    <Grid item xs={12} sm={6} key={item.label}>
                      <Card variant="outlined" sx={{ height: '100%' }}>
                        <CardActionArea component={RouterLink} to={item.to}>
                          <CardContent>
                            <Typography>{item.label}</Typography>
                          </CardContent>
                        </CardActionArea>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
