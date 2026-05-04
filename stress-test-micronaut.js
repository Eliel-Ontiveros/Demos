/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║      BREAKING POINT STRESS TEST — MICRONAUT (Java)       ║
 * ║  Objetivo: Encontrar el límite máximo del framework       ║
 * ║  Puerto  : 8080                                           ║
 * ║  Ejecución: k6 run stress-test-micronaut.js               ║
 * ╚══════════════════════════════════════════════════════════╝
 *
 *  Fases (POST y GET corren simultáneamente):
 *  ┌─────────────────┬────────┬─────────────┐
 *  │ Fase             │ Tiempo │ req/s (cada)│
 *  ├─────────────────┼────────┼─────────────┤
 *  │ 1. Calentamiento │  30 s  │  5 → 20     │
 *  │ 2. Rampa suave   │   1 m  │ 20 → 100    │
 *  │ 3. Estrés medio  │   2 m  │ 100 → 300   │
 *  │ 4. Estrés alto   │   2 m  │ 300 → 600   │
 *  │ 5. Punto quiebre │   2 m  │ 600 → 1000  │
 *  │ 6. Pico máximo   │   1 m  │ 1000 (fijo) │
 *  │ 7. Enfriamiento  │  30 s  │ 1000 → 0    │
 *  └─────────────────┴────────┴─────────────┘
 *  Duración total ≈ 9 minutos  |  pico: 2000 req/s combinados
 */

import http from "k6/http";
import { check } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.2/index.js";

// ── Identificación del framework ─────────────────────────────────
const FRAMEWORK = "Micronaut";
const BASE_URL = "http://localhost:8080";

// ── Métricas personalizadas ──────────────────────────────────────
const postDuration = new Trend("post_duration", true);
const getDuration = new Trend("get_duration", true);
const errorRate = new Rate("error_rate");
const totalReqs = new Counter("total_requests");

// ── Configuración de carga ───────────────────────────────────────
export const options = {
    scenarios: {
        // Escenario de escritura (POST)
        stress_post: {
            executor: "ramping-arrival-rate",
            startRate: 5,
            timeUnit: "1s",
            preAllocatedVUs: 100,
            maxVUs: 2000,
            stages: [
                { duration: "30s", target: 20 }, // Calentamiento
                { duration: "1m", target: 100 }, // Rampa suave
                { duration: "2m", target: 300 }, // Estrés medio
                { duration: "2m", target: 500 }, // Estrés alto
                { duration: "2m", target: 500 }, // Punto de quiebre
                { duration: "1m", target: 500 }, // Pico sostenido
                { duration: "30s", target: 0 }, // Enfriamiento
            ],
            exec: "stressPost",
        },
        // Escenario de lectura (GET)
        stress_get: {
            executor: "ramping-arrival-rate",
            startRate: 5,
            timeUnit: "1s",
            preAllocatedVUs: 100,
            maxVUs: 2000,
            stages: [
                { duration: "30s", target: 20 },
                { duration: "1m", target: 100 },
                { duration: "2m", target: 300 },
                { duration: "2m", target: 500 },
                { duration: "2m", target: 500 },
                { duration: "1m", target: 500 },
                { duration: "30s", target: 0 },
            ],
            exec: "stressGet",
        },
    },

    thresholds: {
        "error_rate": ["rate<0.50"],
        "http_req_duration": ["p(95)<10000"],
        "post_duration": ["p(99)<15000"],
        "get_duration": ["p(99)<15000"],
    },
};

// ── Headers comunes ──────────────────────────────────────────────
const POST_HEADERS = { "Content-Type": "application/json", "X-Tenant-ID": "tenant1" };
const GET_HEADERS = { "X-Tenant-ID": "tenant1" };

// ── Escenario POST ───────────────────────────────────────────────
export function stressPost() {
    const payload = JSON.stringify({
        name: `stress-${__VU}-${__ITER}`,
        value: `load-${Date.now()}`,
    });

    const res = http.post(`${BASE_URL}/api/records`, payload, {
        headers: POST_HEADERS,
        timeout: "15s",
        tags: { framework: FRAMEWORK, op: "POST" },
    });

    postDuration.add(res.timings.duration, { framework: FRAMEWORK });
    totalReqs.add(1, { framework: FRAMEWORK });

    const ok = check(res, {
        "POST 2xx": (r) => r.status >= 200 && r.status < 300,
    });
    errorRate.add(!ok, { framework: FRAMEWORK });
}

// ── Escenario GET ────────────────────────────────────────────────
export function stressGet() {
    const offset = ((__VU * 17 + __ITER * 31) % 900) * 100;

    const res = http.get(`${BASE_URL}/api/records?limit=100&offset=${offset}`, {
        headers: GET_HEADERS,
        timeout: "15s",
        tags: { framework: FRAMEWORK, op: "GET" },
    });

    getDuration.add(res.timings.duration, { framework: FRAMEWORK });
    totalReqs.add(1, { framework: FRAMEWORK });

    const ok = check(res, {
        "GET 200": (r) => r.status === 200,
        "GET has data": (r) => {
            try { return JSON.parse(r.body).primary !== undefined; }
            catch { return false; }
        },
    });
    errorRate.add(!ok, { framework: FRAMEWORK });
}

// ── Resumen final ────────────────────────────────────────────────
export function handleSummary(data) {
    return {
        stdout: textSummary(data, { indent: " ", enableColors: true }),
        "stress-micronaut-results.json": JSON.stringify(data, null, 2),
    };
}
