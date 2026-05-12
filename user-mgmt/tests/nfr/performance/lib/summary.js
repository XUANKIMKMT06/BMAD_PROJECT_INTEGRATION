import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
import { config } from './config.js';

export function createHandleSummary(scenarioId) {
  return function handleSummary(data) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const fileName = `${scenarioId}-${timestamp}.json`;

    return {
      stdout: textSummary(data, { indent: ' ', enableColors: true }),
      [`${config.artifactDir}/${fileName}`]: JSON.stringify(data, null, 2),
    };
  };
}
