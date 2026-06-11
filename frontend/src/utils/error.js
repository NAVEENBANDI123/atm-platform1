export function extractError(error, fallback = 'Something went wrong') {
  const data = error?.response?.data;
  if (!data) {
    return error?.message || fallback;
  }
  if (Array.isArray(data.errors) && data.errors.length > 0) {
    return data.errors.join(', ');
  }
  return data.message || fallback;
}
