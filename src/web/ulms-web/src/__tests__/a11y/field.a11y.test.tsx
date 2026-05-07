import { describe, expect, it } from "vitest";
import { render } from "@testing-library/react";
import axe from "axe-core";

import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";

async function runAxe(node: HTMLElement) {
  const results = await axe.run(node, {
    runOnly: { type: "tag", values: ["wcag2a", "wcag2aa"] },
  });
  return results.violations;
}

describe("Field primitive a11y", () => {
  it("a labelled, hint-described, error-flagged field has no axe violations", async () => {
    const { container } = render(
      <form>
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="email">Email</FieldLabel>
            <Input
              id="email"
              name="email"
              type="email"
              aria-describedby="email-hint"
              aria-invalid="true"
            />
            <FieldDescription id="email-hint">
              Use your university email.
            </FieldDescription>
            <FieldError>Email is required.</FieldError>
          </Field>
        </FieldGroup>
      </form>,
    );
    const violations = await runAxe(container);
    expect(violations, JSON.stringify(violations, null, 2)).toEqual([]);
  });
});
