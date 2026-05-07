import { describe, expect, it } from "vitest";
import { render } from "@testing-library/react";
import axe from "axe-core";

import { ErrorAlert } from "@/components/common/error-alert";
import { EmptyState } from "@/components/common/empty-state";

async function runAxe(node: HTMLElement) {
  const results = await axe.run(node, {
    runOnly: { type: "tag", values: ["wcag2a", "wcag2aa"] },
  });
  return results.violations;
}

describe("ErrorAlert + EmptyState a11y", () => {
  it("ErrorAlert renders without WCAG2A/AA violations", async () => {
    const { container } = render(
      <ErrorAlert title="Something went wrong" message="Try again later." />,
    );
    const violations = await runAxe(container);
    expect(violations, JSON.stringify(violations, null, 2)).toEqual([]);
  });

  it("EmptyState renders without WCAG2A/AA violations", async () => {
    const { container } = render(
      <EmptyState
        title="No results"
        description="Try a different search."
      />,
    );
    const violations = await runAxe(container);
    expect(violations, JSON.stringify(violations, null, 2)).toEqual([]);
  });
});
