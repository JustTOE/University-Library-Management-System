import { test, expect, type Page } from "@playwright/test";

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const ADMIN_EMAIL = "admin@ulms.local";
const ADMIN_PASSWORD = "admin-change-me-now";

test.beforeAll(async () => {
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
});

async function loginAs(page: Page, email: string, password: string) {
  await page.goto("/login");
  await page.getByLabel(/^email$/i).fill(email);
  await page.getByLabel(/^password$/i).fill(password);
  await page.getByRole("button", { name: /sign in/i }).click();
}

test.describe("admin flow (seeded admin, UC12)", () => {
  test("admin sees the admin dropdown and lands on /admin/users", async ({
    page,
  }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    await expect(page.getByRole("button", { name: /^admin$/i })).toBeVisible();
    await page.getByRole("button", { name: /^admin$/i }).click();
    await page.getByRole("menuitem", { name: /^users$/i }).click();
    await page.waitForURL(/\/admin\/users/);

    await expect(page.getByRole("heading", { name: /^users$/i })).toBeVisible();
    await expect(page.getByTestId("new-user")).toBeVisible();
  });

  test("admin creates and deactivates a user", async ({ page }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    const stamp = Date.now();
    const email = `e2e-${stamp}@ulms.local`;
    const universityId = `e2e-uid-${stamp}`;
    const fullName = `E2E Student ${stamp}`;

    await page.goto("/admin/users/new");
    await page.getByLabel(/^full name$/i).fill(fullName);
    await page.getByLabel(/^email$/i).fill(email);
    await page.getByLabel(/^role$/i).selectOption("STUDENT");
    await page.getByLabel(/^university id$/i).fill(universityId);
    await page.getByLabel(/^password/i).fill("P@ssw0rd-e2e-1234");
    await page.getByRole("button", { name: /create user/i }).click();

    await page.waitForURL(/\/admin\/users($|\?)/);
    await page.goto(`/admin/users?q=${encodeURIComponent(email)}`);
    await expect(page.getByText(fullName)).toBeVisible();

    // Deactivate the row we just created.
    const toggle = page.locator('[data-testid^="toggle-active-"]').first();
    await toggle.click();
    await page.getByRole("button", { name: /^deactivate$/i }).last().click();

    await expect(page.getByText(/User deactivated\./i).first()).toBeVisible();
    await expect(page.getByText(/^Inactive$/).first()).toBeVisible();
  });

  test("user-detail tabs render for the seeded admin", async ({ page }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    await page.goto("/admin/users");
    // Click View on the first row (the seeded admin should be in the list).
    const viewLink = page.getByRole("link", { name: /^view$/i }).first();
    await viewLink.click();

    await expect(page.getByRole("tab", { name: /profile/i })).toBeVisible();
    await page.getByRole("tab", { name: /loans/i }).click();
    await expect(page.getByText(/no loans yet/i)).toBeVisible();
    await page.getByRole("tab", { name: /fines/i }).click();
    await expect(page.getByText(/no fines on record/i)).toBeVisible();
  });
});
