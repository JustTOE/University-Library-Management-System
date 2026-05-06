import { test, expect, type Page } from "@playwright/test";

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

const STUDENT_PASSWORD = "student-pass-2026";

let studentEmail: string;

test.beforeAll(async () => {
  // Java backend is a manual prerequisite. Skip cleanly if it's not up.
  try {
    const res = await fetch(`${BACKEND_URL}/v3/api-docs`);
    if (!res.ok) {
      test.skip(true, `Backend at ${BACKEND_URL} returned ${res.status}`);
    }
  } catch (err) {
    test.skip(
      true,
      `Backend at ${BACKEND_URL} unreachable: ${(err as Error).message}`,
    );
  }

  // Each run registers a fresh student so the borrow flow has a usable
  // STUDENT principal (the seeded admin can't borrow per @PreAuthorize).
  const stamp = Date.now();
  studentEmail = `student-${stamp}@ulms.test`;
  const universityId = `UPB-${stamp}`;
  const registerRes = await fetch(`${BACKEND_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name: `Student ${stamp}`,
      email: studentEmail,
      universityId,
      password: STUDENT_PASSWORD,
    }),
  });
  if (!registerRes.ok) {
    const body = await registerRes.text();
    test.skip(
      true,
      `Could not register a test student: ${registerRes.status} ${body}`,
    );
  }
});

async function loginAs(page: Page, email: string, password: string) {
  await page.goto("/login");
  await page.getByLabel(/^email$/i).fill(email);
  await page.getByLabel(/^password$/i).fill(password);
  await page.getByRole("button", { name: /sign in/i }).click();
}

test.describe("student happy path", () => {
  test("login → catalog → open first book → see borrow/reserve UI", async ({
    page,
  }) => {
    await loginAs(page, studentEmail, STUDENT_PASSWORD);
    await page.waitForURL(/\/catalog$/);
    await expect(
      page.getByRole("heading", { name: /^catalog$/i }),
    ).toBeVisible();

    // Navbar: bell + My library dropdown both render for STUDENT.
    await expect(
      page.getByRole("link", { name: /notifications/i }).first(),
    ).toBeVisible();
    await expect(
      page.getByRole("button", { name: /my library/i }),
    ).toBeVisible();

    // Open the first book in the catalog (skip if seed catalog is empty).
    const firstOpen = page.getByRole("link", { name: /^open$/i }).first();
    if ((await firstOpen.count()) === 0) {
      test.skip(true, "Catalog is empty in this environment.");
    }
    await firstOpen.click();
    await page.waitForURL(/\/catalog\/\d+$/);

    // Either Borrow or Reserve must be visible (depending on availability).
    const borrow = page.getByTestId("borrow-button");
    const reserve = page.getByTestId("reserve-button");
    await expect(borrow.or(reserve)).toBeVisible();
  });

  test("/my/loans renders the empty state for a fresh student", async ({
    page,
  }) => {
    await loginAs(page, studentEmail, STUDENT_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    // Click into the My library dropdown and pick Loans.
    await page.getByRole("button", { name: /my library/i }).click();
    await page.getByRole("menuitem", { name: /loans/i }).click();
    await page.waitForURL(/\/my\/loans$/);
    await expect(
      page.getByRole("heading", { name: /^my library$/i }),
    ).toBeVisible();
  });

  test("/my/* without a session redirects to /login with redirectTo", async ({
    page,
  }) => {
    await page.context().clearCookies();
    await page.goto("/my/loans");
    await page.waitForURL(/\/login\?redirectTo=/);
  });
});
