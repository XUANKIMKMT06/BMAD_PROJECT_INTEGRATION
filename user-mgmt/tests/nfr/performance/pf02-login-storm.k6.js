import http from 'k6/http';
import { check, sleep } from 'k6';
import { config } from './lib/config.js';
import { pf02LoginStorm } from './lib/thresholds.js';
import { createHandleSummary } from './lib/summary.js';

export const options = {
  scenarios: {
    login_storm: {
      executor: 'ramping-vus',
      startVUs: 10,
      stages: [
        { duration: '1m', target: 50 },
        { duration: '4m', target: 50 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: pf02LoginStorm,
};

export const handleSummary = createHandleSummary('pf02-login-storm');
export default function () {
  const response = http.post(
    `${config.baseUrl}/api/v1/auth/authenticate`,
    JSON.stringify({ username: config.adminUser, password: config.adminPassword }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'POST /api/v1/auth/authenticate' } },
  );

  check(response, {
    'status is 200': (r) => r.status === 200,
    'token returned': (r) => r.json('token') !== undefined,
  });

  sleep(0.5);
}
