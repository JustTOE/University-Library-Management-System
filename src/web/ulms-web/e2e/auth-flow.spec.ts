import { test, expect, type Page } from "@playwright/test";

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const ADMIN_EMAIL = "admin@ulms.local";
const ADMIN_PASSWORD = "admin-change-me-now";

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
});

async function loginAs(page: Page, email: string, password: string) {
  await page.goto("/login");
  await page.getByLabel(/^email$/i).fill(email);
  await page.getByLabel(/^password$/i).fill(password);
  await page.getByRole("button", { name: /sign in/i }).click();
}

test.describe("auth flow (seeded admin)", () => {
  test("login → /catalog → logout → /login → re-login", async ({ page }) => {
    // Reach the login page directly.
    await page.goto("/login");
    await expect(page.getByRole("heading", { name: /sign in/i })).toBeVisible();

    // Fill and submit.
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);

    // Land on /catalog.
    await page.waitForURL(/\/catalog$/);
    await expect(
      page.getByRole("heading", { name: /^catalog$/i }),
    ).toBeVisible();

    // Open the avatar menu, click Log out.
    await page.getByRole("button", { name: /account menu/i }).click();
    await page.getByRole("button", { name: /log out/i }).click();

    // Back on /login.
    await page.waitForURL(/\/login(?:\?|$)/);
    await expect(page.getByRole("heading", { name: /sign in/i })).toBeVisible();

    // Log in again to confirm the flow is repeatable.
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);
    await expect(
      page.getByRole("heading", { name: /^catalog$/i }),
    ).toBeVisible();
  });

  test("wrong password shows the backend error message", async ({ page }) => {
    await loginAs(page, ADMIN_EMAIL, "definitely-not-the-password");
    await expect(page.getByText(/invalid email or password/i)).toBeVisible();
    await expect(page).toHaveURL(/\/login(?:\?|$)/);
  });

  test("/catalog without auth redirects to /login", async ({ page }) => {
    await page.context().clearCookies();
    await page.goto("/catalog");
    await page.waitForURL(/\/login\?redirectTo=%2Fcatalog/);
  });
});
