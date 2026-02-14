import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test.beforeEach(async ({ request }) => {
    try {
      await request.get('http://localhost:8080/', { timeout: 2000 });
    } catch {
      throw new Error(
        'Backend not running on :8080. Start with: docker compose up -d database && cd backend && mvn spring-boot:run'
      );
    }
  });

  test('user can register and login', async ({ page }) => {
    // Navigate to login page (root - login and register are on same page)
    await page.goto('/');

    // Click "Register" to reveal the registration form
    await page.getByRole('button', { name: 'Register' }).first().click();

    // Fill registration form (unique username to avoid conflicts on retries)
    // Use pressSequentially so React controlled inputs receive onChange events and update state
    const uniqueId = Date.now().toString(36);
    const username = `testuser-${uniqueId}`;
    await page.getByPlaceholder('Username').pressSequentially(username);
    await page.getByPlaceholder('Email').pressSequentially(`${username}@example.com`);
    await page.getByPlaceholder('Password').pressSequentially('password123');

    // Wait for registration API and submit form
    const registerResponse = page.waitForResponse((r) =>
      r.url().includes('/api/user/register') && r.request().method() === 'POST'
    );
    await page.getByRole('button', { name: 'Register' }).click();
    const response = await registerResponse;

    if (!response.ok()) {
      const body = await response.text();
      throw new Error(`Registration failed (${response.status()}): ${body}`);
    }

    // Reload to show login form, then log in manually
    await page.reload();
    await page.getByPlaceholder('Username').waitFor({ state: 'visible' });

    // Use evaluate to set React controlled inputs (fill/pressSequentially can miss onChange)
    await page.evaluate(
      ({ user, pass }) => {
        const setNativeValue = (el, value) => {
          const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
            window.HTMLInputElement.prototype,
            'value'
          ).set;
          nativeInputValueSetter.call(el, value);
          el.dispatchEvent(new Event('input', { bubbles: true }));
        };
        const userInput = document.querySelector('input[placeholder="Username"]');
        const passInput = document.querySelector('input[placeholder="Password"]');
        if (userInput) setNativeValue(userInput, user);
        if (passInput) setNativeValue(passInput, pass);
      },
      { user: username, pass: 'password123' }
    );

    // Wait for login API and submit
    const loginResponse = page.waitForResponse((r) =>
      r.url().includes('/api/user/login') && r.request().method() === 'POST'
    );
    await page.getByRole('button', { name: 'Login' }).click();
    const loginRes = await loginResponse;
    if (!loginRes.ok()) {
      const body = await loginRes.text().catch(() => '');
      throw new Error(`Login failed (${loginRes.status()}): ${body || loginRes.statusText()}`);
    }

    // Wait for redirect and authenticated state
    await expect(page.locator('[data-testid="user-menu"]')).toBeVisible({ timeout: 15000 });
    await expect(page).toHaveURL(/\/home/);
  });
});
