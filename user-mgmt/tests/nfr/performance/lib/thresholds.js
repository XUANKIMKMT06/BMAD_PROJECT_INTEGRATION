export const errorRate = {
  http_req_failed: ['rate<0.01'],
};

export const smoke = {
  ...errorRate,
  'http_req_duration{name:POST /api/v1/auth/authenticate}': ['p(95)<800'],
  'http_req_duration{name:GET /dashboard/users}': ['p(95)<800'],
  'http_req_duration{name:GET /}': ['p(95)<800'],
  'http_req_duration{name:GET /css/style.css}': ['p(95)<800'],
};

export const pf01AdminList = {
  ...errorRate,
  'http_req_duration{name:GET /dashboard/users}': ['p(95)<300'],
};

export const pf02LoginStorm = {
  ...errorRate,
  'http_req_duration{name:POST /api/v1/auth/authenticate}': ['p(95)<500', 'p(99)<1000'],
};

export const pf03Home = {
  ...errorRate,
  'http_req_duration{name:GET /home}': ['p(95)<400'],
};

export const pf04MixedAdmin = {
  ...errorRate,
  http_req_duration: ['p(95)<500'],
};
