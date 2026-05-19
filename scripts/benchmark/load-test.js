import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

const browseRate = Number(__ENV.BROWSE_RATE || 400);
const loginRate = Number(__ENV.LOGIN_RATE || 100);
const purchaseRate = Number(__ENV.PURCHASE_RATE || 0);
const duration = __ENV.DURATION || '2m';

const preAllocatedVUs = Number(__ENV.PREALLOCATED_VUS || 300);
const maxVUs = Number(__ENV.MAX_VUS || 1000);

const loginCredentials = new SharedArray('login_credentials', () => {
  try {
    return JSON.parse(open('./login_credentials.json'));
  } catch (_e) {
    return [];
  }
});

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

const scenarios = {
  browse_active_products: {
    executor: 'constant-arrival-rate',
    exec: 'browseActiveProducts',
    rate: browseRate,
    timeUnit: '1s',
    duration,
    preAllocatedVUs,
    maxVUs,
  },
  login_flow: {
    executor: 'constant-arrival-rate',
    exec: 'loginFlow',
    rate: loginRate,
    timeUnit: '1s',
    duration,
    preAllocatedVUs,
    maxVUs,
  },
};

if (purchaseRate > 0) {
  scenarios.purchase_flow = {
    executor: 'constant-arrival-rate',
    exec: 'purchaseFlow',
    rate: purchaseRate,
    timeUnit: '1s',
    duration,
    preAllocatedVUs,
    maxVUs,
  };
}

export const options = {
  scenarios,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
    checks: ['rate>0.99'],
  },
};

export function browseActiveProducts() {
  const res = http.get(`${baseUrl}/api/v1/flash-sales/active`, {
    tags: { endpoint: 'active-products' },
  });

  check(res, {
    'active-products status is 200': (r) => r.status === 200,
    'active-products response is json array': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body));
      } catch (_e) {
        return false;
      }
    },
  });
}

export function loginFlow() {
  if (loginCredentials.length === 0) {
    return;
  }

  const credential = loginCredentials[__ITER % loginCredentials.length];
  const res = http.post(
    `${baseUrl}/api/v1/auth/login`,
    JSON.stringify({ identifier: credential.identifier, password: credential.password }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { endpoint: 'login' },
    }
  );

  check(res, {
    'login status is 200': (r) => r.status === 200,
    'login response has token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return typeof body.token === 'string' && body.token.length > 10;
      } catch (_e) {
        return false;
      }
    },
  });
}

export function purchaseFlow() {
  if (purchaseRate === 0 || purchaseTokens.length === 0 || productIds.length === 0) {
    return;
  }

  const token = purchaseTokens[__ITER % purchaseTokens.length];
  const productId = productIds[__ITER % productIds.length];

  const res = http.post(`${baseUrl}/api/v1/flash-sales/${productId}/purchase`, null, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { endpoint: 'purchase' },
  });

  check(res, {
    'purchase status is 200': (r) => r.status === 200,
  });
}

