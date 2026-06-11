import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0d47a1' },
    secondary: { main: '#00897b' },
    background: { default: '#f4f6f8' },
  },
  shape: { borderRadius: 10 },
});
