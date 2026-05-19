import http from 'k6/http';
import { check, fail } from 'k6';
import { Counter } from 'k6/metrics';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const txRate = Number(__ENV.TX_RATE || 500);
const duration = __ENV.DURATION || '2m';
const tpsTolerance = Number(__ENV.TPS_TOLERANCE || 0.99);

const preAllocatedVUs = Number(__ENV.PREALLOCATED_VUS || 500);
const maxVUs = Number(__ENV.MAX_VUS || 2000);

function durationToSeconds(value) {
  const match = String(value).match(/^(\d+)(s|m|h)$/);
  if (!match) {
    return null;
  }
  const amount = Number(match[1]);
  const unit = match[2];
  if (unit === 's') return amount;
  if (unit === 'm') return amount * 60;
  if (unit === 'h') return amount * 3600;
  return null;
}

const successfulTransactions = new Counter('successful_transactions');
const rejectedTransactions = new Counter('rejected_transactions');

const purchaseTokens = new SharedArray('purchase_tokens', () => {
  try {
    return JSON.parse(open('./purchase_tokens.json'));
  } catch (_e) {
    return [];
  }
});

const productIds = new SharedArray('product_ids', () => {
  try {
    return JSON.parse(open('./product_ids.json'));
  } catch (_e) {
    return [];
  }
});

const durationSeconds = durationToSeconds(duration);
const requiredUsers =
  durationSeconds === null
    ? Number(__ENV.REQUIRED_USERS || 0)
    : Number(__ENV.REQUIRED_USERS || txRate * durationSeconds + txRate);

if (purchaseTokens.length === 0 || productIds.length === 0) {
  fail(
    'Missing purchase dataset. Generate scripts/benchmark/purchase_tokens.json and scripts/benchmark/product_ids.json first.'
  );
}

if (requiredUsers > 0 && purchaseTokens.length < requiredUsers) {
  fail(
    `Insufficient purchase users for strict TPS run. Required >= ${requiredUsers}, found ${purchaseTokens.length}.`
  );
}

export const options = {
  scenarios: {
    purchase_transactions: {
      executor: 'constant-arrival-rate',
      exec: 'purchaseTransaction',
      rate: txRate,
      timeUnit: '1s',
      duration,
      preAllocatedVUs,
      maxVUs,
    },
  },
  thresholds: {
    http_req_failed: ['rate==0'],
    http_req_duration: ['p(95)<500'],
    // Strict transaction benchmark with small tolerance for scheduler rounding.
    successful_transactions: [`rate>=${txRate * tpsTolerance}`],
    rejected_transactions: ['count==0'],
  },
};

export function purchaseTransaction() {
  const iteration = exec.scenario.iterationInTest;
  const token = purchaseTokens[iteration];
  const productId = productIds[iteration % productIds.length];

  if (!token) {
    rejectedTransactions.add(1);
    return;
  }

  const res = http.post(`${baseUrl}/api/v1/flash-sales/${productId}/purchase`, null, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { endpoint: 'purchase-transaction' },
  });

  const committed = res.status === 200;
  if (committed) {
    successfulTransactions.add(1);
  } else {
    rejectedTransactions.add(1);
  }

  check(res, {
    'purchase committed (status 200)': (r) => r.status === 200,
  });
}

