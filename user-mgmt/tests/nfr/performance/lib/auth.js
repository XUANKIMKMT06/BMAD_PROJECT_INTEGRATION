import http from 'k6/http';
import { config } from './config.js';

const jsonHeaders = { 'Content-Type': 'application/json' };

export function authenticate(username, password) {
  const response = http.post(
    `${config.baseUrl}/api/v1/auth/authenticate`,
    JSON.stringify({ username, password }),
    { headers: jsonHeaders, tags: { name: 'POST /api/v1/auth/authenticate' } },
  );

  if (response.status !== 200) {
    return null;
  }

  return response.json('token');
}

export function registerUser(name, username, password) {
  const response = http.post(
    `${config.baseUrl}/api/v1/auth/register`,
    JSON.stringify({ name, username, password }),
    { headers: jsonHeaders, tags: { name: 'POST /api/v1/auth/register' } },
  );

  if (response.status !== 200) {
    return null;
  }

  return response.json('token');
}

export function bearerHeaders(token) {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}

export function uniqueUsername(vu, iteration) {
  return `perf-${vu}-${iteration}-${Date.now()}@mail.co`;
}
