import { createTheme } from '@mui/material/styles';

/**
 * Modern banking palette:
 *   - primary: deep indigo (customer portal)
 *   - secondary: warm teal (employee portal)
 *   - background: very light cool grey
 *
 * Hero gradients used by the login pages are exposed as CSS strings on the
 * theme object so any page can grab them without re-declaring colours.
 */
const customerGradient =
  'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #06b6d4 100%)';
const employeeGradient =
  'linear-gradient(135deg, #0f766e 0%, #0d9488 50%, #22d3ee 100%)';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#1e3a8a', light: '#3b82f6', dark: '#172554' },
    secondary: { main: '#0d9488', light: '#22d3ee', dark: '#134e4a' },
    success: { main: '#059669' },
    warning: { main: '#d97706' },
    error: { main: '#dc2626' },
    info: { main: '#2563eb' },
    background: { default: '#f1f5f9', paper: '#ffffff' },
    text: { primary: '#0f172a', secondary: '#475569' },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily:
      '"Inter","Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
    overline: { letterSpacing: '0.12em', fontWeight: 700 },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 10, paddingInline: 18, paddingBlock: 8 },
        containedPrimary: {
          boxShadow: '0 6px 16px -8px rgba(30, 58, 138, 0.55)',
        },
      },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          borderRadius: 16,
          border: '1px solid rgba(15,23,42,0.06)',
          boxShadow:
            '0 10px 30px -18px rgba(15,23,42,0.18), 0 4px 12px -8px rgba(15,23,42,0.08)',
        },
      },
    },
    MuiAppBar: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        colorPrimary: { backgroundImage: customerGradient },
        colorSecondary: { backgroundImage: employeeGradient },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600 },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined' },
    },
  },
});

theme.custom = {
  customerGradient,
  employeeGradient,
};
