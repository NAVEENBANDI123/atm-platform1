import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from '@mui/material';
import { notificationApi } from '../../features/account/accountApi.js';
import { extractError } from '../../utils/error.js';

export default function NotificationsPage() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await notificationApi.list(0, 50);
      setItems(data.data?.content || []);
    } catch (err) {
      setError(extractError(err, 'Could not load notifications'));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const markAll = async () => {
    try {
      await notificationApi.markAllRead();
      load();
    } catch (err) {
      setError(extractError(err, 'Could not mark as read'));
    }
  };

  return (
    <Box>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        sx={{ mb: 2 }}
      >
        <Typography variant="h5">Notifications</Typography>
        <Button onClick={markAll}>Mark all as read</Button>
      </Stack>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <Stack spacing={1}>
        {items.length === 0 && (
          <Alert severity="info">No notifications yet</Alert>
        )}
        {items.map((n) => (
          <Card key={n.id} variant="outlined">
            <CardContent>
              <Stack direction="row" justifyContent="space-between">
                <Typography variant="subtitle1">{n.title}</Typography>
                {!n.read && <Chip size="small" label="NEW" color="info" />}
              </Stack>
              <Typography variant="body2">{n.body}</Typography>
              <Typography variant="caption" color="text.secondary">
                {new Date(n.createdAt).toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Box>
  );
}
