import { afterEach, beforeAll, describe, expect, it, vi } from "vitest";

import {
  daysUntil,
  formatDateOnly,
  formatDateTime,
  formatMoney,
  formatRelative,
} from "./format";

beforeAll(() => {
  vi.useFakeTimers();
  vi.setSystemTime(new Date("2026-05-06T12:00:00Z"));
});

afterEach(() => {
  vi.setSystemTime(new Date("2026-05-06T12:00:00Z"));
});

describe("formatDateOnly", () => {
  it("formats an ISO date as 'EEE, d MMM yyyy'", () => {
    expect(formatDateOnly("2026-05-06")).toMatch(/Wed, 6 May 2026/);
  });
  it("returns em-dash for null/undefined/empty", () => {
    expect(formatDateOnly(undefined)).toBe("—");
    expect(formatDateOnly(null)).toBe("—");
    expect(formatDateOnly("")).toBe("—");
  });
});

describe("formatDateTime", () => {
  it("formats an ISO datetime including time", () => {
    expect(formatDateTime("2026-05-06T14:30:00Z")).toMatch(/6 May 2026/);
    expect(formatDateTime("2026-05-06T14:30:00Z")).toMatch(/\d{2}:\d{2}/);
  });
});

describe("formatRelative", () => {
  it("returns 'in N days' when in the future", () => {
    expect(formatRelative("2026-05-09T12:00:00Z")).toMatch(/^in \d+ days?$/);
  });
  it("returns 'N days ago' when in the past", () => {
    expect(formatRelative("2026-05-01T12:00:00Z")).toMatch(/days? ago$/);
  });
});

describe("formatMoney", () => {
  it("formats with the EUR symbol", () => {
    const out = formatMoney(12.5);
    expect(out).toContain("12.50");
    expect(out).toContain("€");
  });
  it("returns em-dash for null/undefined/NaN", () => {
    expect(formatMoney(undefined)).toBe("—");
    expect(formatMoney(null)).toBe("—");
    expect(formatMoney(Number.NaN)).toBe("—");
  });
});

describe("daysUntil", () => {
  it("returns positive integer for future", () => {
    expect(daysUntil("2026-05-10")).toBe(4);
  });
  it("returns zero for today", () => {
    expect(daysUntil("2026-05-06")).toBe(0);
  });
  it("returns negative for past", () => {
    expect(daysUntil("2026-05-01")).toBe(-5);
  });
  it("returns null for missing", () => {
    expect(daysUntil(null)).toBeNull();
  });
});
