#!/usr/bin/env node

import { createRequire } from "node:module";
import { parseArgs } from "node:util";

const require = createRequire(import.meta.url);
const engine = require("../lib/engine.js");

const { values } = parseArgs({
  options: {
    seed:          { type: "string" },
    pool:          { type: "string" },
    winners:       { type: "string" },
    "expected-hash": { type: "string" },
    help:          { type: "boolean", short: "h" },
  },
});

if (values.help || !values.seed || !values.pool || !values.winners) {
  console.log(`
pickr-verify — verify a winwithpickr giveaway result

Usage:
  pickr-verify --seed <hex> --pool <id,id,...> --winners <n> [--expected-hash <hash>]

Options:
  --seed           64-char hex seed from the result page
  --pool           Comma-separated user IDs (sorted)
  --winners        Number of winners
  --expected-hash  Expected pool hash (optional — verifies pool integrity)
  -h, --help       Show this help
`);
  process.exit(0);
}

const poolIds = values.pool.split(",").map(s => s.trim());
const winnerCount = parseInt(values.winners, 10);

const pickr = engine.com.winwithpickr.core;
const result = pickr.verifyPick(values.seed, poolIds, winnerCount);

console.log(`Seed:      ${values.seed}`);
console.log(`Pool size: ${poolIds.length}`);
console.log(`Winners:   ${result.winners.join(", ")}`);
console.log(`Pool hash: ${result.poolHash}`);

if (values["expected-hash"]) {
  const match = result.poolHash === values["expected-hash"];
  console.log(`Expected:  ${values["expected-hash"]}`);
  console.log(`Match:     ${match ? "YES" : "NO — pool may have been tampered with"}`);
  process.exit(match ? 0 : 1);
}
