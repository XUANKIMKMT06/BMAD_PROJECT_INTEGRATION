import http from 'k6/http';
import { check, sleep } from 'k6';
import { authenticate, bearerHeaders } from './lib/auth.js';
import { config } from './lib/config.js';
import { pf03Home } from './lib/thresholds.js';
import { createHandleSummary } from './lib/summary.js';

export const options = {
  scenarios: {
    authenticated_home: {
      executor: 'constant-vus',
      vus: 50,
      duration: '5m',
    },
  },
  thresholds: pf03Home,
};

export const handleSummary = createHandleSummary('pf03-home');
export function setup() {
  const token = authenticate(config.adminUser, config.adminPassword);
  if (!token) {
    throw new Error(`Unable to authenticate user ${config.adminUser}`);
  }
  return { token };
}

export default function ({ token }) {
  const response = http.get(`${config.baseUrl}/home`, {
    headers: bearerHeaders(token),
    tags: { name: 'GET /home' },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'home page served': (r) => r.body && r.body.includes('html'),
  });

  sleep(0.5);
}
