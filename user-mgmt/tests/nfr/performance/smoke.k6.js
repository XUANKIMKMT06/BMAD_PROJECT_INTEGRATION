import http from 'k6/http';
import { check, sleep } from 'k6';
import { authenticate, bearerHeaders } from './lib/auth.js';
import { config } from './lib/config.js';
import { smoke as smokeThresholds } from './lib/thresholds.js';
import { createHandleSummary } from './lib/summary.js';

export const options = {
  vus: 5,
  duration: '60s',
  thresholds: smokeThresholds,
};

export const handleSummary = createHandleSummary('smoke');
export function setup() {
  const adminToken = authenticate(config.adminUser, config.adminPassword);
  if (!adminToken) {
    throw new Error(`Unable to authenticate admin user ${config.adminUser}`);
  }
  return { adminToken };
}

export default function ({ adminToken }) {
  const landing = http.get(`${config.baseUrl}/`, { tags: { name: 'GET /' } });
  check(landing, { 'landing status 200': (r) => r.status === 200 });

  const stylesheet = http.get(`${config.baseUrl}/css/style.css`, {
    tags: { name: 'GET /css/style.css' },
  });
  check(stylesheet, { 'stylesheet status 200': (r) => r.status === 200 });

  const auth = http.post(
    `${config.baseUrl}/api/v1/auth/authenticate`,
    JSON.stringify({ username: config.adminUser, password: config.adminPassword }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'POST /api/v1/auth/authenticate' } },
  );
  check(auth, { 'auth status 200': (r) => r.status === 200 });

  const users = http.get(`${config.baseUrl}/dashboard/users`, {
    headers: bearerHeaders(adminToken),
    tags: { name: 'GET /dashboard/users' },
  });
  check(users, {
    'admin list status 200': (r) => r.status === 200,
    'admin list returns array': (r) => Array.isArray(r.json()),
  });

  sleep(1);
}
