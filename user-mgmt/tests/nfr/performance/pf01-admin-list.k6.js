import http from 'k6/http';
import { check, sleep } from 'k6';
import { authenticate, bearerHeaders } from './lib/auth.js';
import { config } from './lib/config.js';
import { pf01AdminList } from './lib/thresholds.js';
import { createHandleSummary } from './lib/summary.js';

export const options = {
  scenarios: {
    admin_list: {
      executor: 'constant-vus',
      vus: 30,
      duration: '5m',
    },
  },
  thresholds: pf01AdminList,
};

export const handleSummary = createHandleSummary('pf01-admin-list');
export function setup() {
  const adminToken = authenticate(config.adminUser, config.adminPassword);
  if (!adminToken) {
    throw new Error(`Unable to authenticate admin user ${config.adminUser}`);
  }
  return { adminToken };
}

export default function ({ adminToken }) {
  const response = http.get(`${config.baseUrl}/dashboard/users`, {
    headers: bearerHeaders(adminToken),
    tags: { name: 'GET /dashboard/users' },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'body is array': (r) => Array.isArray(r.json()),
  });

  sleep(0.5);
}
