# Testing Guide

This guide explains how to write and run tests for the E-Commerce frontend using Jest, Cucumber, and Selenium.

## Testing Frameworks Overview

The frontend supports three complementary testing frameworks:

1. **Jest** - Unit and integration testing for React components
2. **Cucumber** - Behavior-driven development (BDD) testing
3. **Selenium** - End-to-end browser automation testing

## Jest Testing

### Overview
Jest is a JavaScript testing framework that works seamlessly with React. It's ideal for:
- Unit testing React components
- Testing component behavior and state
- Snapshot testing
- Mocking API calls

### Running Jest Tests

```bash
# Run all tests in watch mode
npm test

# Run all tests once (CI mode)
CI=true npm test

# Run tests with coverage
npm test -- --coverage
```

### Writing Jest Tests

Create test files with `.test.js` or `.spec.js` suffix next to your components:

```javascript
// src/components/Customers.test.js
import { render, screen, waitFor } from '@testing-library/react';
import Customers from './Customers';
import { customerApi } from '../services/api';

// Mock the API
jest.mock('../services/api');

test('displays customers when loaded', async () => {
  const mockCustomers = [
    { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@example.com' }
  ];
  
  customerApi.getAllCustomers.mockResolvedValue({ data: mockCustomers });
  
  render(<Customers />);
  
  await waitFor(() => {
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });
});
```

### Best Practices
- Mock external dependencies (APIs, services)
- Use `waitFor` for asynchronous operations
- Test user interactions with `fireEvent` or `userEvent`
- Keep tests focused on behavior, not implementation

## Cucumber Testing

### Overview
Cucumber enables BDD testing using Gherkin syntax. It's ideal for:
- Writing tests in natural language
- Collaboration between technical and non-technical team members
- Defining executable specifications

### Running Cucumber Tests

```bash
# Run all Cucumber tests
npm run test:cucumber

# Run specific feature file
npx cucumber-js tests/cucumber/features/customers.feature
```

### Writing Cucumber Tests

#### 1. Write Feature Files (Gherkin)

Feature files describe behavior in plain English:

```gherkin
# tests/cucumber/features/products.feature
Feature: Product Management
  As a user
  I want to view products
  So that I can browse the inventory

  Scenario: View all products
    Given I am on the products page
    When the page loads
    Then I should see a list of products
    And each product should display name, price, and stock

  Scenario: Filter products by category
    Given I am on the products page
    When I select the "Electronics" category
    Then I should only see products in the "Electronics" category
```

#### 2. Implement Step Definitions

Step definitions connect Gherkin steps to code:

```javascript
// tests/cucumber/step_definitions/product_steps.js
const { Given, When, Then } = require('@cucumber/cucumber');
const { Builder, By, until } = require('selenium-webdriver');

let driver;

Given('I am on the products page', async function () {
  driver = await new Builder().forBrowser('chrome').build();
  await driver.get('http://localhost:3000');
  await driver.findElement(By.xpath("//button[text()='Products']")).click();
});

When('the page loads', async function () {
  await driver.wait(until.elementLocated(By.className('product-list')), 5000);
});

Then('I should see a list of products', async function () {
  const products = await driver.findElements(By.css('.product-list tr'));
  if (products.length === 0) {
    throw new Error('No products found');
  }
});
```

### Configuration

Cucumber configuration is in `cucumber.js`:

```javascript
module.exports = {
  default: {
    require: ['tests/cucumber/step_definitions/**/*.js'],
    format: ['progress', 'html:tests/cucumber/reports/cucumber-report.html'],
    paths: ['tests/cucumber/features/**/*.feature'],
    publishQuiet: true
  }
};
```

## Selenium Testing

### Overview
Selenium WebDriver enables browser automation. It's ideal for:
- End-to-end testing across real browsers
- Testing complex user workflows
- Cross-browser compatibility testing

### Running Selenium Tests

```bash
# Run all Selenium tests
npm run test:selenium

# Run specific test file
node tests/selenium/customer-test.js
```

### Writing Selenium Tests

```javascript
// tests/selenium/customer-test.js
const { Builder, By, until, Key } = require('selenium-webdriver');

async function testCustomerFlow() {
  let driver = await new Builder().forBrowser('chrome').build();
  
  try {
    // Navigate to application
    await driver.get('http://localhost:3000');
    
    // Click on Customers tab
    await driver.findElement(By.xpath("//button[text()='Customers']")).click();
    
    // Wait for customers to load
    await driver.wait(until.elementLocated(By.className('customer-list')), 5000);
    
    // Verify customers are displayed
    const customers = await driver.findElements(By.css('.customer-list tr'));
    console.log(`Found ${customers.length} customers`);
    
    // Click on first customer
    if (customers.length > 0) {
      await customers[0].click();
      // Add assertions for customer details
    }
    
    console.log('✓ Customer flow test passed');
    
  } catch (error) {
    console.error('✗ Test failed:', error);
    throw error;
  } finally {
    await driver.quit();
  }
}

testCustomerFlow();
```

### Setup Requirements

For Selenium to work, you need:
1. A WebDriver (ChromeDriver for Chrome, GeckoDriver for Firefox)
2. The corresponding browser installed

```bash
# Install ChromeDriver (if not already installed)
npm install --save-dev chromedriver

# Or use a service like selenium-standalone
npm install --save-dev selenium-standalone
npx selenium-standalone install
npx selenium-standalone start
```

### Common Selenium Commands

```javascript
// Finding elements
driver.findElement(By.id('element-id'))
driver.findElement(By.className('class-name'))
driver.findElement(By.css('css-selector'))
driver.findElement(By.xpath('//xpath/expression'))

// Interactions
element.click()
element.sendKeys('text to type')
element.sendKeys(Key.ENTER)
element.clear()

// Waiting
driver.wait(until.elementLocated(By.id('element-id')), 5000)
driver.wait(until.elementIsVisible(element), 5000)
driver.wait(until.titleContains('Expected Title'), 5000)

// Assertions
const text = await element.getText()
const isDisplayed = await element.isDisplayed()
const value = await element.getAttribute('value')
```

## Test Organization

```
frontend/
├── src/
│   ├── components/
│   │   ├── Customers.js
│   │   └── Customers.test.js      # Jest tests
│   └── App.test.js                # Jest tests
├── tests/
│   ├── cucumber/
│   │   ├── features/              # Gherkin feature files
│   │   │   └── customers.feature
│   │   ├── step_definitions/      # Step implementations
│   │   │   └── customer_steps.js
│   │   └── reports/               # Test reports
│   └── selenium/                  # Selenium test scripts
│       └── run-tests.js
└── cucumber.js                    # Cucumber configuration
```

## Running All Tests

To run all testing suites:

```bash
# Terminal 1: Start backend services
cd ..
./start-all-services.sh

# Terminal 2: Start frontend
cd frontend
npm start

# Terminal 3: Run tests
npm test                    # Jest
npm run test:cucumber       # Cucumber
npm run test:selenium       # Selenium
```

## Continuous Integration

For CI/CD pipelines, use:

```bash
# Build frontend
npm run build

# Run Jest in CI mode
CI=true npm test

# Run Cucumber
npm run test:cucumber

# Run Selenium (requires browser and driver)
npm run test:selenium
```

## Tips and Best Practices

### General
- Write tests before fixing bugs (TDD)
- Keep tests independent and isolated
- Use descriptive test names
- Test edge cases and error conditions

### Jest
- Use `describe` blocks to group related tests
- Mock external dependencies
- Use `beforeEach` and `afterEach` for setup/teardown

### Cucumber
- Write scenarios from user's perspective
- Keep scenarios short and focused
- Use Background for common setup steps
- Reuse step definitions across features

### Selenium
- Use explicit waits, avoid implicit waits
- Use page object pattern for maintainability
- Take screenshots on failure for debugging
- Run tests in headless mode for faster execution

## Debugging

### Jest
```bash
# Run tests in debug mode
node --inspect-brk node_modules/.bin/jest --runInBand
```

### Cucumber
```bash
# Run with verbose output
npx cucumber-js --format progress-bar
```

### Selenium
```javascript
// Take screenshot on error
try {
  // test code
} catch (error) {
  await driver.takeScreenshot().then(
    function(image) {
      require('fs').writeFileSync('error.png', image, 'base64');
    }
  );
  throw error;
}
```

## Resources

- [Jest Documentation](https://jestjs.io/)
- [React Testing Library](https://testing-library.com/react)
- [Cucumber.js Documentation](https://github.com/cucumber/cucumber-js)
- [Selenium WebDriver Documentation](https://www.selenium.dev/documentation/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
