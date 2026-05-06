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

test.describe("librarian flow (seeded admin)", () => {
  test("admin sees the librarian dropdown and lands on /librarian/catalog", async ({
    page,
  }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    await expect(
      page.getByRole("button", { name: /librarian/i }),
    ).toBeVisible();
    await page.getByRole("button", { name: /librarian/i }).click();
    await page.getByRole("menuitem", { name: /^catalog$/i }).click();
    await page.waitForURL(/\/librarian\/catalog/);

    await expect(
      page.getByRole("heading", { name: /^catalog$/i }),
    ).toBeVisible();
    await expect(page.getByTestId("new-book")).toBeVisible();
  });

  test("admin creates and deletes a book", async ({ page }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    const stamp = Date.now();
    const isbn = `e2e-${stamp}`;
    const title = `Phase 6 e2e ${stamp}`;

    await page.goto("/librarian/catalog/new");
    await page.getByLabel(/^title$/i).fill(title);
    await page.getByLabel(/^author$/i).fill("Playwright");
    await page.getByLabel(/^isbn$/i).fill(isbn);
    await page.getByLabel(/^total copies$/i).fill("2");
    await page.getByLabel(/^available copies$/i).fill("2");
    await page.getByRole("button", { name: /create book/i }).click();

    // Land back on the librarian catalog list.
    await page.waitForURL(/\/librarian\/catalog($|\?)/);

    // The new title shows up; search to scope the lookup if the catalog is large.
    await page.goto(
      `/librarian/catalog?title=${encodeURIComponent(title)}`,
    );
    await expect(page.getByText(title).first()).toBeVisible();

    // Delete the row we just created.
    const deleteButton = page.locator('[data-testid^="delete-book-"]').first();
    await deleteButton.click();
    await page.getByRole("button", { name: /delete book/i }).click();

    // Toast appears with "Deleted" and the title leaves the list.
    await expect(page.getByText(/Deleted/i).first()).toBeVisible();
    await expect(page.getByText(title)).toHaveCount(0);
  });

  test("returns lookup surfaces an error for a bogus loan id", async ({
    page,
  }) => {
    await loginAs(page, ADMIN_EMAIL, ADMIN_PASSWORD);
    await page.waitForURL(/\/catalog$/);

    await page.goto("/librarian/returns?id=999999");
    await expect(page.getByText(/no loan found with id/i)).toBeVisible();
  });
});
