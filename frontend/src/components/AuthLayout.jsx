import { Box, Card, Container, Stack, Typography } from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import BoltIcon from '@mui/icons-material/Bolt';
import LockIcon from '@mui/icons-material/Lock';
import { useTheme } from '@mui/material/styles';

/**
 * Two-pane auth shell: a vibrant gradient hero on the left with the brand and
 * three trust bullets, and the actual form card on the right.  The hero
 * collapses gracefully on small screens so the form is always reachable.
 */
export default function AuthLayout({
  variant = 'customer',
  eyebrow,
  title,
  subtitle,
  children,
}) {
  const theme = useTheme();
  const gradient =
    variant === 'employee'
      ? theme.custom.employeeGradient
      : theme.custom.customerGradient;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1.05fr 1fr' },
        background: '#f1f5f9',
      }}
    >
      {/* Hero */}
      <Box
        sx={{
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          justifyContent: 'space-between',
          color: '#fff',
          p: 6,
          background: gradient,
          position: 'relative',
          overflow: 'hidden',
          '&::after': {
            content: '""',
            position: 'absolute',
            inset: 0,
            background:
              'radial-gradient(800px 400px at 80% 0%, rgba(255,255,255,0.18), transparent 60%),'
              + 'radial-gradient(600px 300px at 0% 100%, rgba(255,255,255,0.10), transparent 60%)',
            pointerEvents: 'none',
          },
        }}
      >
        <Stack direction="row" alignItems="center" spacing={1.5} sx={{ zIndex: 1 }}>
          <AccountBalanceIcon sx={{ fontSize: 36 }} />
          <Box>
            <Typography variant="h6" sx={{ lineHeight: 1, fontWeight: 800 }}>
              ATM Platform
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.85 }}>
              {variant === 'employee'
                ? 'Staff console'
                : 'Banking, simplified.'}
            </Typography>
          </Box>
        </Stack>

        <Box sx={{ zIndex: 1, mt: { md: -8 } }}>
          <Typography variant="h3" sx={{ fontWeight: 800, lineHeight: 1.1 }}>
            {variant === 'employee'
              ? 'Approvals, audit, and operations  in one place.'
              : 'A modern bank account, in minutes.'}
          </Typography>
          <Typography sx={{ mt: 2, maxWidth: 420, opacity: 0.92 }}>
            {variant === 'employee'
              ? 'Review customer applications, approve cards and loans, deposit funds, and inspect the audit log  all behind your secure employee credentials.'
              : 'Open an account, send money to anyone, apply for cards and loans, and track everything from one beautifully simple dashboard.'}
          </Typography>

          <Stack spacing={2} sx={{ mt: 5, maxWidth: 420 }}>
            <FeatureRow
              icon={<VerifiedUserIcon />}
              title="Bank-grade security"
              body="JWT auth, role-based access, audit trail on every action."
            />
            <FeatureRow
              icon={<BoltIcon />}
              title="Instant operations"
              body="Live transfers, real-time approvals, async email notifications."
            />
            <FeatureRow
              icon={<LockIcon />}
              title="Privacy by default"
              body="Balances are masked until you choose to reveal them."
            />
          </Stack>
        </Box>

        <Typography variant="caption" sx={{ opacity: 0.8, zIndex: 1 }}>
          &copy; {new Date().getFullYear()} ATM Platform - demo build
        </Typography>
      </Box>

      {/* Form */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          py: { xs: 6, md: 0 },
          px: { xs: 2, md: 4 },
        }}
      >
        <Container maxWidth="xs" disableGutters>
          <Card sx={{ p: { xs: 3, sm: 4 } }}>
            {eyebrow && (
              <Typography
                variant="overline"
                color={variant === 'employee' ? 'secondary' : 'primary'}
              >
                {eyebrow}
              </Typography>
            )}
            <Typography variant="h5" sx={{ mt: 0.5 }}>
              {title}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {subtitle}
              </Typography>
            )}
            <Box sx={{ mt: 3 }}>{children}</Box>
          </Card>
        </Container>
      </Box>
    </Box>
  );
}

function FeatureRow({ icon, title, body }) {
  return (
    <Stack direction="row" spacing={2} alignItems="flex-start">
      <Box
        sx={{
          width: 38,
          height: 38,
          borderRadius: '12px',
          bgcolor: 'rgba(255,255,255,0.18)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
        }}
      >
        {icon}
      </Box>
      <Box>
        <Typography sx={{ fontWeight: 700 }}>{title}</Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          {body}
        </Typography>
      </Box>
    </Stack>
  );
}
