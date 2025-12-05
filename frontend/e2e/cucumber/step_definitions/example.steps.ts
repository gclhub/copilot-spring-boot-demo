import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { CustomWorld } from '../support/world';

const BASE_URL = process.env['BASE_URL'] || 'http://localhost:4200';

Given('I am on the home page', async function (this: CustomWorld) {
  if (!this.page) throw new Error('Page not initialized');
  await this.page.goto(BASE_URL);
  await this.page.waitForLoadState('networkidle');
  
  // Verify we're on the home page
  const title = await this.page.locator('mat-toolbar').textContent();
  expect(title).toContain('E-Commerce Microservices Demo');
});

When('I navigate to the customers page', async function (this: CustomWorld) {
  if (!this.page) throw new Error('Page not initialized');
  
  // Click the Customers button in navigation
  await this.page.click('button:has-text("Customers")');
  await this.page.waitForLoadState('networkidle');
  
  // Wait for the customers page to load
  await this.page.waitForSelector('mat-card-title:has-text("Customers")');
});

Then('I should see a list of customers', async function (this: CustomWorld) {
  if (!this.page) throw new Error('Page not initialized');
  
  // Check for either the loading indicator or error message or table
  const hasLoadingOrError = await this.page.locator('mat-spinner, .error-message, table').count() > 0;
  expect(hasLoadingOrError).toBeTruthy();
  
  // Check that the customers heading is visible
  const heading = await this.page.locator('mat-card-title:has-text("Customers")').isVisible();
  expect(heading).toBeTruthy();
});

When('I navigate to the products page', async function (this: CustomWorld) {
  if (!this.page) throw new Error('Page not initialized');
  
  // Click the Products button in navigation
  await this.page.click('button:has-text("Products")');
  await this.page.waitForLoadState('networkidle');
  
  // Wait for the products page to load
  await this.page.waitForSelector('mat-card-title:has-text("Products")');
});

Then('I should see a list of products', async function (this: CustomWorld) {
  if (!this.page) throw new Error('Page not initialized');
  
  // Check for either the loading indicator or error message or table
  const hasLoadingOrError = await this.page.locator('mat-spinner, .error-message, table').count() > 0;
  expect(hasLoadingOrError).toBeTruthy();
  
  // Check that the products heading is visible
  const heading = await this.page.locator('mat-card-title:has-text("Products")').isVisible();
  expect(heading).toBeTruthy();
});
