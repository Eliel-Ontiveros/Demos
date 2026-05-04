import http from "k6/http";
import { sleep, check, group } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.2/index.js";

// ── Custom metrics ───────────────────────────────────────────────
const postDuration = new Trend("post_duration", true);
const getDuration = new Trend("get_duration", true);
const errorRate = new Rate("error_rate");
const requestCount = new Counter("request_count");

// ── Load configuration ───────────────────────────────────────────
export const options = {
    scenarios: {
        light_post: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "30s", target: 10 },
                { duration: "1m", target: 50 },
                { duration: "1m", target: 100 },
                { duration: "30s", target: 0 },
            ],
            exec: "testPost",
        },
        light_get: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "30s", target: 10 },
                { duration: "1m", target: 50 },
                { duration: "1m", target: 100 },
                { duration: "30s", target: 0 },
            ],
            exec: "testGet",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.05"],
        http_req_duration: ["p(95)<2000"],
        // Sub-métricas por framework (POST)
        "post_duration{framework:Micronaut}": ["p(95)<500"],
        "post_duration{framework:WebFlux}": ["p(95)<500"],
        "post_duration{framework:Gin}": ["p(95)<500"],
        "post_duration{framework:Axum}": ["p(95)<500"],
        // Sub-métricas por framework (GET)
        "get_duration{framework:Micronaut}": ["p(95)<500"],
        "get_duration{framework:WebFlux}": ["p(95)<500"],
        "get_duration{framework:Gin}": ["p(95)<500"],
        "get_duration{framework:Axum}": ["p(95)<500"],
    },
};

// ── Frameworks under test ────────────────────────────────────────
const FRAMEWORKS = [
    { name: "Micronaut", baseUrl: "http://localhost:8080" },
    { name: "WebFlux", baseUrl: "http://localhost:8082" },
    { name: "Gin", baseUrl: "http://localhost:8083" },
    { name: "Axum", baseUrl: "http://localhost:8084" },
];

// ── POST scenario ────────────────────────────────────────────────
export function testPost() {
    const fw = FRAMEWORKS[__VU % FRAMEWORKS.length];

    group(`${fw.name} - POST /api/records`, () => {
        const payload = JSON.stringify({
            name: `k6-test-${__VU}-${__ITER}`,
            value: `value-${Date.now()}`,
        });

        const res = http.post(`${fw.baseUrl}/api/records`, payload, {
            headers: {
                "Content-Type": "application/json",
                "X-Tenant-ID": "tenant1",
            },
            tags: { framework: fw.name, endpoint: "POST" },
        });

        postDuration.add(res.timings.duration, { framework: fw.name });
        requestCount.add(1, { framework: fw.name });

        // En testPost() — cambiar el check de status:
        const ok = check(res, {
            "POST status 2xx": (r) => r.status === 200 || r.status === 201,
            "POST has primary": (r) => {
                try { return JSON.parse(r.body).primary !== undefined; }
                catch { return false; }
            },
        });

        errorRate.add(!ok);
    });

    sleep(0.5);
}

// ── GET scenario ─────────────────────────────────────────────────
export function testGet() {
    const fw = FRAMEWORKS[__VU % FRAMEWORKS.length];

    group(`${fw.name} - GET /api/records`, () => {
        const res = http.get(`${fw.baseUrl}/api/records?limit=100&offset=0`, {
            headers: { "X-Tenant-ID": "tenant1" },
            tags: { framework: fw.name, endpoint: "GET" },
        });

        getDuration.add(res.timings.duration, { framework: fw.name });
        requestCount.add(1, { framework: fw.name });

        const ok = check(res, {
            [`${fw.name} GET status 200`]: (r) => r.status === 200,
            [`${fw.name} GET < 500ms`]: (r) => r.timings.duration < 500,
            [`${fw.name} GET has primary`]: (r) => {
                try { return JSON.parse(r.body).primary !== undefined; }
                catch { return false; }
            },
        });

        errorRate.add(!ok);
    });

    sleep(1);
}

// ── Summary ──────────────────────────────────────────────────────
export function handleSummary(data) {
    return {
        "stdout": textSummary(data, { indent: " ", enableColors: true }),
        "summary.json": JSON.stringify(data, null, 2),
    };
}
