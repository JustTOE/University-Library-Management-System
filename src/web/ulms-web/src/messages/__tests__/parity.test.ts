import { describe, expect, it } from "vitest";

import en from "../en.json";
import ro from "../ro.json";

type AnyRecord = Record<string, unknown>;

function collectKeys(obj: unknown, prefix = ""): string[] {
  if (obj === null || typeof obj !== "object") {
    return prefix ? [prefix] : [];
  }
  const out: string[] = [];
  for (const [key, value] of Object.entries(obj as AnyRecord)) {
    const nextPrefix = prefix ? `${prefix}.${key}` : key;
    if (value !== null && typeof value === "object" && !Array.isArray(value)) {
      out.push(...collectKeys(value, nextPrefix));
    } else {
      out.push(nextPrefix);
    }
  }
  return out.sort();
}

describe("message catalog parity", () => {
  it("en.json and ro.json expose identical key trees", () => {
    const enKeys = collectKeys(en);
    const roKeys = collectKeys(ro);
    expect(roKeys).toEqual(enKeys);
  });

  it("every value is a non-empty string", () => {
    function checkAll(obj: unknown, locale: string, prefix = "") {
      if (obj === null || typeof obj !== "object") return;
      for (const [key, value] of Object.entries(obj as AnyRecord)) {
        const path = prefix ? `${prefix}.${key}` : key;
        if (value !== null && typeof value === "object") {
          checkAll(value, locale, path);
        } else {
          expect(typeof value, `${locale}:${path} must be a string`).toBe("string");
          expect(String(value).trim().length, `${locale}:${path} must be non-empty`).toBeGreaterThan(0);
        }
      }
    }
    checkAll(en, "en");
    checkAll(ro, "ro");
  });
});
