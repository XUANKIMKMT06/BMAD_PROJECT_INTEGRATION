import http from 'k6/http';
import { check, sleep } from 'k6';
import { authenticate, bearerHeaders, registerUser, uniqueUsername } from './lib/auth.js';
import { config } from './lib/config.js';
import { pf04MixedAdmin } from './lib/thresholds.js';
import { createHandleSummary } from './lib/summary.js';

export const options = {
  scenarios: {
    mixed_admin: {
      executor: 'constant-vus',
      vus: 30,
      duration: '5m',
    },
  },
  thresholds: pf04MixedAdmin,
};

export const handleSummary = createHandleSummary('pf04-mixed-admin');
export function setup() {
  const adminToken = authenticate(config.adminUser, config.adminPassword);
  if (!adminToken) {
    throw new Error(`Unable to authenticate admin user ${config.adminUser}`);
  }
  return { adminToken };
}

export default function ({ adminToken }) {
  const roll = Math.random();

  if (roll < 0.7) {
    const response = http.get(`${config.baseUrl}/dashboard/users`, {
      headers: bearerHeaders(adminToken),
      tags: { name: 'GET /dashboard/users' },
    });
    check(response, { 'list status 200': (r) => r.status === 200 });
  } else if (roll < 0.9) {
    const username = uniqueUsername(__VU, __ITER);
    const token = registerUser('Perf User', username, config.userPassword);
    check(token, { 'register issued token': (value) => value !== null });
  } else {
    const username = uniqueUsername(__VU, __ITER);
    registerUser('Perf Delete', username, config.userPassword);
    const response = http.del(`${config.baseUrl}/delete/${encodeURIComponent(username)}`, null, {
      headers: bearerHeaders(adminToken),
      tags: { name: 'DELETE /delete/{user}' },
    });
    check(response, { 'delete status 200': (r) => r.status === 200 });
  }

  sleep(0.5);
}
