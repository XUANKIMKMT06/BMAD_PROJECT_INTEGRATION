export const config = {
  baseUrl: __ENV.BASE_URL || 'http://localhost:8080',
  adminUser: __ENV.ADMIN_USER || 'alice@mail.co',
  adminPassword: __ENV.ADMIN_PASSWORD || 'pass',
  userPassword: __ENV.USER_PASSWORD || 'Password1',
  artifactDir: __ENV.PERF_ARTIFACT_DIR || '../_bmad-output/test-artifacts/performance',
};