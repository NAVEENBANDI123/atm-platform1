import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';

import PersonIcon from '@mui/icons-material/Person';
import EditIcon from '@mui/icons-material/Edit';
import LockResetIcon from '@mui/icons-material/LockReset';
import DownloadIcon from '@mui/icons-material/Download';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import SendIcon from '@mui/icons-material/Send';
import HistoryIcon from '@mui/icons-material/History';
import GroupIcon from '@mui/icons-material/Group';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import CardMembershipIcon from '@mui/icons-material/CardMembership';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import RequestQuoteIcon from '@mui/icons-material/RequestQuote';
import SchoolIcon from '@mui/icons-material/School';
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar';
import HomeIcon from '@mui/icons-material/Home';
import SavingsIcon from '@mui/icons-material/Savings';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import FamilyRestroomIcon from '@mui/icons-material/FamilyRestroom';
import DescriptionIcon from '@mui/icons-material/Description';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import CreditScoreIcon from '@mui/icons-material/CreditScore';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';

const SECTIONS = [
  {
    title: 'Account Services',
    sectionIcon: <AccountBalanceWalletIcon />,
    items: [
      { label: 'View Profile', icon: <PersonIcon />, to: '/customer/services/profile' },
      { label: 'Update Profile', icon: <EditIcon />, to: '/customer/services/profile' },
      { label: 'Change Password', icon: <LockResetIcon />, to: '/customer/services/change-password' },
      { label: 'Download Statement', icon: <DownloadIcon />, to: '/customer/services/transactions' },
      { label: 'Mini Statement', icon: <ReceiptLongIcon />, to: '/customer/services/mini-statement' },
    ],
  },
  {
    title: 'Transaction Services',
    sectionIcon: <SwapHorizIcon />,
    items: [
      { label: 'Fund Transfer', icon: <SendIcon />, to: '/customer/services/transfer' },
      { label: 'Transaction History', icon: <HistoryIcon />, to: '/customer/services/transactions' },
      { label: 'Beneficiary Management', icon: <GroupIcon />, to: '/customer/services/beneficiaries' },
    ],
  },
  {
    title: 'Card Services',
    sectionIcon: <CreditCardIcon />,
    items: [
      { label: 'Apply Debit Card', icon: <CreditCardIcon />, to: '/customer/services/cards' },
      { label: 'Apply Credit Card', icon: <CardMembershipIcon />, to: '/customer/services/cards' },
      { label: 'Card Status Tracking', icon: <HourglassEmptyIcon />, to: '/customer/services/cards' },
    ],
  },
  {
    title: 'Loan Services',
    sectionIcon: <CreditScoreIcon />,
    items: [
      { label: 'Personal Loan', icon: <RequestQuoteIcon />, to: '/customer/services/loans' },
      { label: 'Education Loan', icon: <SchoolIcon />, to: '/customer/services/loans' },
      { label: 'Vehicle Loan', icon: <DirectionsCarIcon />, to: '/customer/services/loans' },
      { label: 'Home Loan', icon: <HomeIcon />, to: '/customer/services/loans' },
    ],
  },
  {
    title: 'Other Services',
    sectionIcon: <MoreHorizIcon />,
    items: [
      { label: 'Cheque Book Request', icon: <DescriptionIcon />, to: '/customer/services/tickets' },
      { label: 'Nominee Management', icon: <FamilyRestroomIcon />, to: '/customer/services/nominees' },
      { label: 'Fixed Deposit', icon: <SavingsIcon />, to: '/customer/services/fd' },
      { label: 'Recurring Deposit', icon: <AutorenewIcon />, to: '/customer/services/rd' },
      { label: 'Complaint / Tickets', icon: <SupportAgentIcon />, to: '/customer/services/tickets' },
    ],
  },
];

export default function ServicesPage() {
  const theme = useTheme();
  const accent = theme.palette.primary.main;

  return (
    <Box>
      <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 1 }}>
        <AccountBalanceIcon color="primary" />
        <Typography variant="h5" sx={{ fontWeight: 800 }}>
          Services
        </Typography>
      </Stack>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Everything you can do with your account, organized by category.
      </Typography>

      <Stack spacing={3}>
        {SECTIONS.map((section) => (
          <Card key={section.title}>
            <CardContent>
              <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 2 }}>
                <Box
                  sx={{
                    width: 36,
                    height: 36,
                    borderRadius: 1.5,
                    bgcolor: alpha(accent, 0.12),
                    color: 'primary.main',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {section.sectionIcon}
                </Box>
                <Typography variant="overline" color="primary" sx={{ fontSize: 12 }}>
                  {section.title}
                </Typography>
              </Stack>

              <Grid container spacing={2}>
                {section.items.map((item) => (
                  <Grid item xs={12} sm={6} md={4} key={item.label}>
                    <Card
                      variant="outlined"
                      sx={{
                        height: '100%',
                        transition: 'all 160ms ease',
                        '&:hover': {
                          borderColor: accent,
                          boxShadow:
                            '0 14px 30px -22px rgba(15,23,42,0.25)',
                        },
                      }}
                    >
                      <CardActionArea
                        component={RouterLink}
                        to={item.to}
                        sx={{ height: '100%' }}
                      >
                        <CardContent>
                          <Stack direction="row" spacing={1.5} alignItems="center">
                            <Box
                              sx={{
                                width: 34,
                                height: 34,
                                borderRadius: 1.5,
                                bgcolor: alpha(accent, 0.10),
                                color: 'primary.main',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                              }}
                            >
                              {item.icon}
                            </Box>
                            <Typography sx={{ fontWeight: 600 }}>
                              {item.label}
                            </Typography>
                          </Stack>
                        </CardContent>
                      </CardActionArea>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Box>
  );
}
