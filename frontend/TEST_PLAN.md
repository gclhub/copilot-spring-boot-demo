# Frontend UI Test Plan

## Overview
This document outlines a comprehensive testing strategy for the Spring Boot Demo Frontend application, covering unit tests (Jest), behavior-driven development tests (Cucumber), and end-to-end UI tests (Selenium).

## Table of Contents
1. [Testing Stack](#testing-stack)
2. [Test Coverage Goals](#test-coverage-goals)
3. [Unit Tests with Jest](#unit-tests-with-jest)
4. [Component Integration Tests](#component-integration-tests)
5. [BDD Tests with Cucumber](#bdd-tests-with-cucumber)
6. [E2E Tests with Selenium](#e2e-tests-with-selenium)
7. [Test Data Management](#test-data-management)
8. [CI/CD Integration](#cicd-integration)

---

## Testing Stack

### Core Testing Libraries
- **Jest**: Unit and component testing framework
- **React Testing Library**: Component testing utilities
- **Cucumber.js**: BDD test framework
- **Selenium WebDriver**: Browser automation
- **Axios Mock Adapter**: API mocking

### Additional Tools
- **@testing-library/user-event**: User interaction simulation
- **@testing-library/jest-dom**: Custom Jest matchers
- **cucumber-html-reporter**: BDD test reporting
- **jest-coverage**: Code coverage reporting

---

## Test Coverage Goals

### Minimum Coverage Targets
- **Statements**: 80%
- **Branches**: 75%
- **Functions**: 80%
- **Lines**: 80%

### Critical Paths (100% Coverage Required)
- Order creation workflow
- Customer CRUD operations
- Product inventory management
- Form validation logic
- API error handling

---

## Unit Tests with Jest

### 1. API Service Tests (`src/services/api.test.js`)

#### Customer API Tests
```javascript
describe('Customer API', () => {
  test('getAllCustomers returns customer list', async () => {
    // Mock axios response
    // Call customerApi.getAllCustomers()
    // Verify correct endpoint called
    // Verify data transformation
  });

  test('createCustomer sends correct payload', async () => {
    // Mock axios post
    // Call customerApi.createCustomer()
    // Verify payload structure
    // Verify response handling
  });

  test('updateCustomer handles validation errors', async () => {
    // Mock 400 error response
    // Call customerApi.updateCustomer()
    // Verify error thrown
    // Verify error message format
  });

  test('deleteCustomer confirms before deletion', async () => {
    // Mock successful deletion
    // Verify DELETE request sent
    // Verify correct customer ID used
  });
});
```

#### Inventory API Tests
```javascript
describe('Inventory API', () => {
  test('getAllProducts returns product list with stock levels', async () => {});
  test('createProduct validates required fields', async () => {});
  test('updateProduct handles concurrent updates', async () => {});
  test('deleteProduct fails for products in active orders', async () => {});
});
```

#### Order API Tests
```javascript
describe('Order API', () => {
  test('createOrder validates cart contents', async () => {});
  test('getOrdersByCustomer filters correctly', async () => {});
  test('updateOrderStatus transitions are valid', async () => {});
  test('cancelOrder refunds inventory', async () => {});
});
```

### 2. Component Unit Tests

#### Customers Component (`src/components/Customers.test.js`)
```javascript
describe('Customers Component', () => {
  beforeEach(() => {
    // Mock API responses
    // Setup test data
  });

  test('renders customer list on load', async () => {
    // Render component
    // Wait for API call
    // Verify customers displayed
  });

  test('displays add customer form when button clicked', () => {
    // Render component
    // Click "Add New Customer" button
    // Verify form appears
  });

  test('validates required fields on form submission', async () => {
    // Render component with form visible
    // Submit empty form
    // Verify validation errors shown
  });

  test('creates new customer successfully', async () => {
    // Fill out form with valid data
    // Submit form
    // Verify API called with correct data
    // Verify success message shown
    // Verify form hidden
    // Verify customer list refreshed
  });

  test('edits existing customer', async () => {
    // Render component with customers
    // Click edit button
    // Verify form pre-filled
    // Change data
    // Submit
    // Verify update API called
  });

  test('deletes customer with confirmation', async () => {
    // Mock window.confirm
    // Render component
    // Click delete button
    // Verify confirmation dialog shown
    // Confirm deletion
    // Verify API called
    // Verify customer removed from list
  });

  test('searches customers by name', async () => {
    // Render with multiple customers
    // Type in search box
    // Verify filtered results shown
  });

  test('searches customers by email', async () => {});
  test('searches customers by phone', async () => {});

  test('displays customer details in modal', async () => {
    // Click "View Details" button
    // Verify modal opens
    // Verify customer data shown
    // Verify order history displayed
  });

  test('calculates total spent correctly', async () => {
    // Open customer details with orders
    // Verify total calculation
  });

  test('handles API errors gracefully', async () => {
    // Mock API error
    // Render component
    // Verify error message shown
    // Verify retry button works
  });

  test('disables email field when editing', () => {
    // Click edit on customer
    // Verify email field has disabled attribute
  });
});
```

#### Products Component (`src/components/Products.test.js`)
```javascript
describe('Products Component', () => {
  test('renders product grid on load', async () => {});
  test('displays stock status badges correctly', () => {
    // in-stock: green
    // low-stock: yellow (< 10)
    // out-of-stock: red (= 0)
  });
  test('adds new product with all fields', async () => {});
  test('validates price is positive number', async () => {});
  test('validates stock quantity is non-negative', async () => {});
  test('edits product and updates display', async () => {});
  test('deletes product with confirmation', async () => {});
  test('filters products by category', async () => {});
  test('shows error when creating duplicate SKU', async () => {});
});
```

#### Orders Component (`src/components/Orders.test.js`)
```javascript
describe('Orders Component', () => {
  test('renders order list on load', async () => {});
  test('displays new order form when button clicked', () => {});
  
  describe('Order Creation Workflow', () => {
    test('loads customer dropdown options', async () => {});
    test('loads product catalog', async () => {});
    test('adds product to cart', async () => {});
    test('updates cart quantity', async () => {});
    test('removes product from cart', async () => {});
    test('calculates subtotal correctly', async () => {});
    test('calculates tax (8%) correctly', async () => {});
    test('calculates shipping based on total', async () => {
      // Free shipping over $100
      // $10 shipping under $100
    });
    test('validates shipping address required', async () => {});
    test('prevents order with empty cart', async () => {});
    test('creates order successfully', async () => {});
  });

  describe('Order Management', () => {
    test('confirms order status changes to CONFIRMED', async () => {});
    test('cancels order and refunds inventory', async () => {});
    test('displays order details in modal', async () => {});
    test('shows order items in detail view', async () => {});
    test('filters orders by status', async () => {});
  });
});
```

#### App Component (`src/App.test.js`)
```javascript
describe('App Component', () => {
  test('renders navigation tabs', () => {});
  test('switches between tabs', () => {});
  test('maintains tab state on refresh', () => {});
  test('displays correct component for each tab', () => {});
});
```

### 3. Utility Function Tests

#### Form Validation (`src/utils/validation.test.js`)
```javascript
describe('Form Validation Utilities', () => {
  test('validates email format', () => {});
  test('validates phone number format', () => {});
  test('validates US ZIP code format', () => {});
  test('validates required fields', () => {});
  test('validates numeric fields', () => {});
  test('validates positive numbers', () => {});
});
```

---

## Component Integration Tests

### Integration Test Scenarios

#### Customer-Order Integration
```javascript
describe('Customer-Order Integration', () => {
  test('creates customer and places order', async () => {
    // 1. Create new customer
    // 2. Switch to Orders tab
    // 3. Select newly created customer
    // 4. Add products to cart
    // 5. Complete order
    // 6. Switch back to Customers tab
    // 7. View customer details
    // 8. Verify order appears in history
  });

  test('customer deletion fails with active orders', async () => {
    // Create customer with orders
    // Attempt to delete
    // Verify error message
  });
});
```

#### Product-Order Integration
```javascript
describe('Product-Order Integration', () => {
  test('order creation reduces product stock', async () => {
    // Note initial stock level
    // Create order with product
    // Verify stock reduced
  });

  test('order cancellation restores product stock', async () => {
    // Create order
    // Note stock level
    // Cancel order
    // Verify stock restored
  });

  test('prevents ordering out-of-stock products', async () => {
    // Create product with 0 stock
    // Attempt to add to cart
    // Verify error or disabled state
  });
});
```

---

## BDD Tests with Cucumber

### Feature Files Location
`features/` directory structure:
```
features/
├── customers/
│   ├── create_customer.feature
│   ├── edit_customer.feature
│   ├── delete_customer.feature
│   └── search_customers.feature
├── products/
│   ├── inventory_management.feature
│   ├── stock_levels.feature
│   └── product_categories.feature
├── orders/
│   ├── place_order.feature
│   ├── order_status.feature
│   └── order_history.feature
└── step_definitions/
    ├── customer_steps.js
    ├── product_steps.js
    └── order_steps.js
```

### Sample Feature: Create Customer

**File**: `features/customers/create_customer.feature`

```gherkin
Feature: Create New Customer
  As an administrator
  I want to create new customer records
  So that I can track customer information and orders

  Background:
    Given I am on the Customers page
    And the backend services are running

  Scenario: Successfully create a customer with all required fields
    When I click the "Add New Customer" button
    Then I should see the customer creation form
    When I fill in the following customer details:
      | Field      | Value                  |
      | First Name | John                   |
      | Last Name  | Smith                  |
      | Email      | john.smith@example.com |
      | Phone      | (555) 123-4567         |
    And I click the "Create Customer" button
    Then I should see a success message
    And the new customer should appear in the customer list
    And the form should be hidden

  Scenario: Validation error for missing required fields
    When I click the "Add New Customer" button
    And I click the "Create Customer" button without filling any fields
    Then I should see validation errors for required fields
    And the customer should not be created

  Scenario: Validation error for invalid email format
    When I click the "Add New Customer" button
    And I fill in "Email" with "invalid-email"
    And I fill in other required fields correctly
    And I click the "Create Customer" button
    Then I should see an error message for invalid email format

  Scenario: Create customer with complete address information
    When I click the "Add New Customer" button
    And I fill in all customer fields including:
      | Field      | Value                  |
      | First Name | Jane                   |
      | Last Name  | Doe                    |
      | Email      | jane.doe@example.com   |
      | Phone      | (555) 987-6543         |
      | Address    | 123 Main Street        |
      | City       | New York               |
      | State      | NY                     |
      | ZIP Code   | 10001                  |
      | Country    | USA                    |
    And I click the "Create Customer" button
    Then the customer should be created successfully
    And all address fields should be saved correctly

  Scenario: Cancel customer creation
    When I click the "Add New Customer" button
    And I fill in some customer details
    And I click the "Cancel" button
    Then the form should be hidden
    And no new customer should be created
```

### Sample Feature: Place Order

**File**: `features/orders/place_order.feature`

```gherkin
Feature: Place Customer Order
  As an administrator
  I want to create orders for customers
  So that I can track sales and manage inventory

  Background:
    Given I am on the Orders page
    And the following customers exist:
      | First Name | Last Name | Email                  |
      | Alice      | Johnson   | alice.j@example.com    |
      | Bob        | Williams  | bob.w@example.com      |
    And the following products exist:
      | Name      | Price | Stock | Category    |
      | Laptop    | 999   | 10    | Electronics |
      | Mouse     | 25    | 50    | Electronics |
      | Keyboard  | 75    | 30    | Electronics |

  Scenario: Successfully place an order with multiple items
    When I click the "Create New Order" button
    And I select customer "Alice Johnson"
    And I add the following products to cart:
      | Product  | Quantity |
      | Laptop   | 1        |
      | Mouse    | 2        |
      | Keyboard | 1        |
    Then the cart should display 3 items
    And the subtotal should be "$1124.00"
    And the tax should be "$89.92"
    And the total should be "$1213.92"
    When I fill in the shipping address:
      | Field   | Value           |
      | Address | 456 Oak Avenue  |
      | City    | Boston          |
      | State   | MA              |
      | ZIP     | 02101           |
    And I click "Place Order"
    Then the order should be created successfully
    And I should see a confirmation message
    And the order should appear in the order list with status "PENDING"

  Scenario: Free shipping applied for orders over $100
    When I create an order with a total of "$150.00"
    Then the shipping cost should be "$0.00"

  Scenario: Shipping cost applied for orders under $100
    When I create an order with a total of "$75.00"
    Then the shipping cost should be "$10.00"

  Scenario: Cannot place order with empty cart
    When I click the "Create New Order" button
    And I select a customer
    And I fill in the shipping address
    And I click "Place Order" without adding products
    Then I should see an error message "Cart is empty"
    And the order should not be created

  Scenario: Update cart quantities
    Given I have added "Laptop" to cart with quantity 1
    When I change the quantity to 3
    Then the cart should show 3 Laptops
    And the subtotal should update to "$2997.00"

  Scenario: Remove item from cart
    Given I have added multiple products to cart
    When I click remove on "Mouse"
    Then "Mouse" should be removed from the cart
    And the cart totals should update accordingly

  Scenario: Stock reduced after order placement
    Given "Laptop" has stock of 10
    When I place an order for 3 Laptops
    Then "Laptop" stock should be reduced to 7
```

### Sample Feature: Edit Customer

**File**: `features/customers/edit_customer.feature`

```gherkin
Feature: Edit Customer Information
  As an administrator
  I want to update customer information
  So that customer records remain accurate

  Background:
    Given I am on the Customers page
    And a customer exists with details:
      | Field      | Value                |
      | First Name | Sarah                |
      | Last Name  | Connor               |
      | Email      | sarah.c@example.com  |
      | Phone      | (555) 111-2222       |
      | City       | Los Angeles          |

  Scenario: Successfully edit customer details
    When I click "Edit" on the customer
    Then the form should be pre-filled with customer data
    And the email field should be disabled
    When I change "Phone" to "(555) 999-8888"
    And I change "City" to "San Francisco"
    And I click "Update Customer"
    Then I should see a success message
    And the customer card should show the updated information

  Scenario: Cancel editing without saving changes
    When I click "Edit" on the customer
    And I change some fields
    And I click "Cancel"
    Then the form should close
    And the original customer data should remain unchanged

  Scenario: Validation prevents saving invalid data
    When I click "Edit" on the customer
    And I clear the "First Name" field
    And I click "Update Customer"
    Then I should see a validation error
    And the customer should not be updated
```

### Sample Feature: Inventory Stock Levels

**File**: `features/products/stock_levels.feature`

```gherkin
Feature: Product Stock Level Indicators
  As an administrator
  I want to see visual stock level indicators
  So that I can quickly identify inventory issues

  Background:
    Given I am on the Products page

  Scenario: Display in-stock badge for adequate inventory
    Given a product exists with stock quantity 50
    Then the product should display a green "In Stock" badge

  Scenario: Display low-stock warning for inventory below threshold
    Given a product exists with stock quantity 5
    Then the product should display a yellow "Low Stock" badge

  Scenario: Display out-of-stock indicator for zero inventory
    Given a product exists with stock quantity 0
    Then the product should display a red "Out of Stock" badge
    And the product should not be selectable in order creation

  Scenario: Stock badge updates after inventory change
    Given a product has stock quantity 25
    When I edit the product and change stock to 3
    Then the badge should change from green to yellow
```

### Step Definitions Example

**File**: `features/step_definitions/customer_steps.js`

```javascript
const { Given, When, Then } = require('@cucumber/cucumber');
const { By, until } = require('selenium-webdriver');
const assert = require('assert');

Given('I am on the Customers page', async function() {
  await this.driver.get('http://localhost:3000');
  await this.driver.findElement(By.xpath("//button[contains(text(), 'Customers')]")).click();
  await this.driver.wait(until.elementLocated(By.className('customers-container')), 5000);
});

When('I click the {string} button', async function(buttonText) {
  const button = await this.driver.findElement(By.xpath(`//button[contains(text(), '${buttonText}')]`));
  await button.click();
});

When('I fill in the following customer details:', async function(dataTable) {
  const rows = dataTable.hashes();
  for (const row of rows) {
    const field = row.Field;
    const value = row.Value;
    const input = await this.driver.findElement(By.xpath(`//label[contains(text(), '${field}')]/following-sibling::input`));
    await input.sendKeys(value);
  }
});

Then('I should see a success message', async function() {
  await this.driver.wait(until.alertIsPresent(), 3000);
  const alert = await this.driver.switchTo().alert();
  const alertText = await alert.getText();
  assert(alertText.includes('successfully'), 'Success message not shown');
  await alert.accept();
});

Then('the new customer should appear in the customer list', async function() {
  const customerCards = await this.driver.findElements(By.className('customer-card'));
  assert(customerCards.length > 0, 'No customers displayed');
});
```

---

## E2E Tests with Selenium

### Test Environment Setup

#### Selenium Configuration (`tests/e2e/config.js`)
```javascript
const { Builder } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');

const setupDriver = async () => {
  const options = new chrome.Options();
  options.addArguments('--headless');
  options.addArguments('--disable-gpu');
  options.addArguments('--window-size=1920,1080');
  
  const driver = await new Builder()
    .forBrowser('chrome')
    .setChromeOptions(options)
    .build();
  
  return driver;
};

module.exports = { setupDriver };
```

### E2E Test Suites

#### Customer Management E2E (`tests/e2e/customers.spec.js`)

```javascript
const { setupDriver } = require('./config');
const { By, until, Key } = require('selenium-webdriver');

describe('Customer Management E2E', () => {
  let driver;
  
  beforeAll(async () => {
    driver = await setupDriver();
  });
  
  afterAll(async () => {
    await driver.quit();
  });
  
  beforeEach(async () => {
    await driver.get('http://localhost:3000');
    await driver.findElement(By.xpath("//button[text()='Customers']")).click();
    await driver.wait(until.elementLocated(By.className('customers-container')), 5000);
  });

  test('Complete customer lifecycle: create, edit, view, delete', async () => {
    // CREATE
    await driver.findElement(By.xpath("//button[contains(text(), 'Add New Customer')]")).click();
    await driver.wait(until.elementLocated(By.className('customer-form')), 2000);
    
    await driver.findElement(By.xpath("//label[text()='First Name *']/following-sibling::input")).sendKeys('John');
    await driver.findElement(By.xpath("//label[text()='Last Name *']/following-sibling::input")).sendKeys('Doe');
    await driver.findElement(By.xpath("//label[text()='Email *']/following-sibling::input")).sendKeys('john.doe.test@example.com');
    await driver.findElement(By.xpath("//label[text()='Phone *']/following-sibling::input")).sendKeys('5551234567');
    await driver.findElement(By.xpath("//label[text()='City']/following-sibling::input")).sendKeys('Boston');
    
    await driver.findElement(By.xpath("//button[text()='Create Customer']")).click();
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    
    // Verify customer appears
    const customerCard = await driver.wait(
      until.elementLocated(By.xpath("//h4[contains(text(), 'John Doe')]")),
      5000
    );
    expect(await customerCard.isDisplayed()).toBe(true);
    
    // EDIT
    const editButton = await driver.findElement(
      By.xpath("//h4[contains(text(), 'John Doe')]/ancestor::div[@class='customer-card']//button[text()='Edit']")
    );
    await editButton.click();
    await driver.wait(until.elementLocated(By.className('customer-form')), 2000);
    
    const phoneInput = await driver.findElement(By.xpath("//label[text()='Phone *']/following-sibling::input"));
    await phoneInput.clear();
    await phoneInput.sendKeys('5559876543');
    
    await driver.findElement(By.xpath("//button[text()='Update Customer']")).click();
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    
    // VIEW DETAILS
    const viewButton = await driver.findElement(
      By.xpath("//h4[contains(text(), 'John Doe')]/ancestor::div[@class='customer-card']//button[text()='View Details']")
    );
    await viewButton.click();
    await driver.wait(until.elementLocated(By.className('modal-overlay')), 2000);
    
    const phoneDetail = await driver.findElement(By.xpath("//label[text()='Phone:']/following-sibling::span"));
    expect(await phoneDetail.getText()).toBe('5559876543');
    
    await driver.findElement(By.xpath("//button[text()='Close']")).click();
    
    // DELETE
    const deleteButton = await driver.findElement(
      By.xpath("//h4[contains(text(), 'John Doe')]/ancestor::div[@class='customer-card']//button[text()='Delete']")
    );
    await deleteButton.click();
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    
    // Verify customer removed
    await driver.sleep(1000);
    const customers = await driver.findElements(By.xpath("//h4[contains(text(), 'John Doe')]"));
    expect(customers.length).toBe(0);
  });

  test('Search functionality filters customers correctly', async () => {
    const searchBox = await driver.findElement(By.className('search-input'));
    await searchBox.sendKeys('alice');
    await driver.sleep(500);
    
    const visibleCards = await driver.findElements(By.className('customer-card'));
    const cardTexts = await Promise.all(visibleCards.map(card => card.getText()));
    
    cardTexts.forEach(text => {
      expect(text.toLowerCase()).toContain('alice');
    });
  });
});
```

#### Order Workflow E2E (`tests/e2e/orders.spec.js`)

```javascript
describe('Order Creation Workflow E2E', () => {
  let driver;
  
  beforeAll(async () => {
    driver = await setupDriver();
  });
  
  afterAll(async () => {
    await driver.quit();
  });
  
  test('Complete order workflow: select customer, add products, place order, confirm', async () => {
    await driver.get('http://localhost:3000');
    await driver.findElement(By.xpath("//button[text()='Orders']")).click();
    await driver.wait(until.elementLocated(By.className('orders-container')), 5000);
    
    // Start new order
    await driver.findElement(By.xpath("//button[contains(text(), 'Create New Order')]")).click();
    await driver.wait(until.elementLocated(By.className('order-form')), 2000);
    
    // Select customer
    const customerSelect = await driver.findElement(By.id('customer-select'));
    await customerSelect.click();
    const firstCustomer = await driver.findElement(By.xpath("//select[@id='customer-select']/option[2]"));
    await firstCustomer.click();
    
    // Add products to cart
    await driver.wait(until.elementLocated(By.className('product-grid')), 3000);
    const addToCartButtons = await driver.findElements(By.xpath("//button[contains(text(), 'Add to Cart')]"));
    
    // Add first product
    await addToCartButtons[0].click();
    await driver.sleep(500);
    
    // Add second product
    await addToCartButtons[1].click();
    await driver.sleep(500);
    
    // Verify cart count
    const cartItems = await driver.findElements(By.className('cart-item'));
    expect(cartItems.length).toBe(2);
    
    // Fill shipping info
    await driver.findElement(By.id('shippingAddress')).sendKeys('123 Test Street');
    await driver.findElement(By.id('shippingCity')).sendKeys('Test City');
    await driver.findElement(By.id('shippingState')).sendKeys('TS');
    await driver.findElement(By.id('shippingZip')).sendKeys('12345');
    
    // Place order
    await driver.findElement(By.xpath("//button[text()='Place Order']")).click();
    await driver.wait(until.alertIsPresent(), 5000);
    await driver.switchTo().alert().accept();
    
    // Verify order appears in list
    await driver.wait(until.elementLocated(By.className('order-card')), 5000);
    const orders = await driver.findElements(By.className('order-card'));
    expect(orders.length).toBeGreaterThan(0);
    
    // Confirm order
    const confirmButton = await driver.findElement(By.xpath("//button[text()='Confirm']"));
    await confirmButton.click();
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    
    // Verify status changed
    const statusBadge = await driver.findElement(By.className('status-confirmed'));
    expect(await statusBadge.isDisplayed()).toBe(true);
  });

  test('Cart calculations are correct with tax and shipping', async () => {
    // Navigate to orders
    // Create order with known product prices
    // Verify subtotal
    // Verify tax (8%)
    // Verify shipping ($10 or $0 based on total)
    // Verify grand total
  });
});
```

#### Product Management E2E (`tests/e2e/products.spec.js`)

```javascript
describe('Product Management E2E', () => {
  let driver;
  
  beforeAll(async () => {
    driver = await setupDriver();
  });
  
  afterAll(async () => {
    await driver.quit();
  });

  test('Create product and verify stock badge color', async () => {
    await driver.get('http://localhost:3000');
    await driver.findElement(By.xpath("//button[text()='Products']")).click();
    await driver.wait(until.elementLocated(By.className('products-container')), 5000);
    
    // Create product with high stock
    await driver.findElement(By.xpath("//button[contains(text(), 'Add New Product')]")).click();
    await driver.findElement(By.id('productName')).sendKeys('Test Widget');
    await driver.findElement(By.id('productPrice')).sendKeys('29.99');
    await driver.findElement(By.id('productStock')).sendKeys('50');
    await driver.findElement(By.id('productCategory')).sendKeys('Test');
    await driver.findElement(By.xpath("//button[text()='Add Product']")).click();
    
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    
    // Verify green badge
    const stockBadge = await driver.findElement(
      By.xpath("//h3[text()='Test Widget']/ancestor::div[@class='product-card']//span[contains(@class, 'stock-badge')]")
    );
    const badgeClass = await stockBadge.getAttribute('class');
    expect(badgeClass).toContain('in-stock');
    
    // Edit to low stock
    const editButton = await driver.findElement(
      By.xpath("//h3[text()='Test Widget']/ancestor::div[@class='product-card']//button[text()='Edit']")
    );
    await editButton.click();
    
    const stockInput = await driver.findElement(By.id('productStock'));
    await stockInput.clear();
    await stockInput.sendKeys('5');
    await driver.findElement(By.xpath("//button[text()='Update Product']")).click();
    
    await driver.wait(until.alertIsPresent(), 3000);
    await driver.switchTo().alert().accept();
    
    // Verify yellow badge
    const newBadge = await driver.findElement(
      By.xpath("//h3[text()='Test Widget']/ancestor::div[@class='product-card']//span[contains(@class, 'stock-badge')]")
    );
    const newBadgeClass = await newBadge.getAttribute('class');
    expect(newBadgeClass).toContain('low-stock');
  });
});
```

#### Cross-Component Integration E2E (`tests/e2e/integration.spec.js`)

```javascript
describe('Cross-Component Integration E2E', () => {
  test('Full business flow: Create customer, add products, place order, view order in customer history', async () => {
    const driver = await setupDriver();
    
    try {
      // 1. Create Customer
      await driver.get('http://localhost:3000');
      await driver.findElement(By.xpath("//button[text()='Customers']")).click();
      await driver.wait(until.elementLocated(By.className('customers-container')), 5000);
      
      await driver.findElement(By.xpath("//button[contains(text(), 'Add New Customer')]")).click();
      await driver.findElement(By.xpath("//label[text()='First Name *']/following-sibling::input")).sendKeys('Integration');
      await driver.findElement(By.xpath("//label[text()='Last Name *']/following-sibling::input")).sendKeys('Test');
      await driver.findElement(By.xpath("//label[text()='Email *']/following-sibling::input")).sendKeys(`integration.${Date.now()}@test.com`);
      await driver.findElement(By.xpath("//label[text()='Phone *']/following-sibling::input")).sendKeys('5555555555');
      await driver.findElement(By.xpath("//button[text()='Create Customer']")).click();
      
      await driver.wait(until.alertIsPresent(), 3000);
      await driver.switchTo().alert().accept();
      await driver.sleep(1000);
      
      // 2. Create Products
      await driver.findElement(By.xpath("//button[text()='Products']")).click();
      await driver.wait(until.elementLocated(By.className('products-container')), 5000);
      
      await driver.findElement(By.xpath("//button[contains(text(), 'Add New Product')]")).click();
      await driver.findElement(By.id('productName')).sendKeys('Integration Product');
      await driver.findElement(By.id('productPrice')).sendKeys('99.99');
      await driver.findElement(By.id('productStock')).sendKeys('100');
      await driver.findElement(By.id('productCategory')).sendKeys('Test');
      await driver.findElement(By.xpath("//button[text()='Add Product']")).click();
      
      await driver.wait(until.alertIsPresent(), 3000);
      await driver.switchTo().alert().accept();
      await driver.sleep(1000);
      
      // 3. Create Order
      await driver.findElement(By.xpath("//button[text()='Orders']")).click();
      await driver.wait(until.elementLocated(By.className('orders-container')), 5000);
      
      await driver.findElement(By.xpath("//button[contains(text(), 'Create New Order')]")).click();
      await driver.wait(until.elementLocated(By.className('order-form')), 2000);
      
      // Select the Integration Test customer
      const customerSelect = await driver.findElement(By.id('customer-select'));
      const customers = await customerSelect.findElements(By.tagName('option'));
      for (let option of customers) {
        const text = await option.getText();
        if (text.includes('Integration Test')) {
          await option.click();
          break;
        }
      }
      
      await driver.sleep(1000);
      
      // Add Integration Product to cart
      const addButton = await driver.findElement(
        By.xpath("//h4[contains(text(), 'Integration Product')]/ancestor::div[@class='product-item']//button[contains(text(), 'Add to Cart')]")
      );
      await addButton.click();
      
      // Fill shipping
      await driver.findElement(By.id('shippingAddress')).sendKeys('123 Integration St');
      await driver.findElement(By.id('shippingCity')).sendKeys('Test City');
      await driver.findElement(By.id('shippingState')).sendKeys('TC');
      await driver.findElement(By.id('shippingZip')).sendKeys('12345');
      
      await driver.findElement(By.xpath("//button[text()='Place Order']")).click();
      await driver.wait(until.alertIsPresent(), 5000);
      await driver.switchTo().alert().accept();
      await driver.sleep(2000);
      
      // 4. Verify order in customer history
      await driver.findElement(By.xpath("//button[text()='Customers']")).click();
      await driver.wait(until.elementLocated(By.className('customers-container')), 5000);
      
      const viewDetailsButton = await driver.findElement(
        By.xpath("//h4[contains(text(), 'Integration Test')]/ancestor::div[@class='customer-card']//button[text()='View Details']")
      );
      await viewDetailsButton.click();
      await driver.wait(until.elementLocated(By.className('modal-overlay')), 3000);
      
      // Verify order appears in history
      const orderRow = await driver.findElement(By.xpath("//td[contains(text(), 'Integration Product')]"));
      expect(await orderRow.isDisplayed()).toBe(true);
      
      // Verify total
      const totalCell = await driver.findElement(By.xpath("//strong[contains(text(), '$')]"));
      const totalText = await totalCell.getText();
      expect(totalText).toContain('99.99');
      
    } finally {
      await driver.quit();
    }
  });
});
```

---

## Test Data Management

### Test Data Strategy

#### Mock Data Files (`tests/fixtures/`)

**customers.json**
```json
[
  {
    "id": 1,
    "firstName": "Test",
    "lastName": "Customer",
    "email": "test.customer@example.com",
    "phone": "(555) 000-0001",
    "address": "123 Test St",
    "city": "Test City",
    "state": "TC",
    "zipCode": "12345",
    "country": "USA"
  }
]
```

**products.json**
```json
[
  {
    "id": 1,
    "name": "Test Product",
    "description": "Product for testing",
    "price": 49.99,
    "category": "Test",
    "stock": 100,
    "sku": "TEST-001"
  }
]
```

**orders.json**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-2024-001",
    "customerId": 1,
    "status": "PENDING",
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 49.99
      }
    ],
    "subtotal": 99.98,
    "tax": 8.00,
    "shipping": 0.00,
    "totalAmount": 107.98
  }
]
```

### Database Seeding Script

**File**: `tests/utils/seed-database.js`
```javascript
const axios = require('axios');
const customerData = require('../fixtures/customers.json');
const productData = require('../fixtures/products.json');

async function seedDatabase() {
  try {
    // Clear existing data
    console.log('Clearing database...');
    // Implementation depends on backend API
    
    // Seed customers
    console.log('Seeding customers...');
    for (const customer of customerData) {
      await axios.post('http://localhost:8081/api/customers', customer);
    }
    
    // Seed products
    console.log('Seeding products...');
    for (const product of productData) {
      await axios.post('http://localhost:8082/api/products', product);
    }
    
    console.log('Database seeded successfully!');
  } catch (error) {
    console.error('Error seeding database:', error);
    throw error;
  }
}

module.exports = { seedDatabase };
```

---

## CI/CD Integration

### GitHub Actions Workflow

**File**: `.github/workflows/frontend-tests.yml`

```yaml
name: Frontend Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
          
      - name: Run Jest tests
        run: |
          cd frontend
          npm test -- --coverage --watchAll=false
          
      - name: Upload coverage reports
        uses: codecov/codecov-action@v3
        with:
          files: ./frontend/coverage/lcov.info
          flags: frontend

  e2e-tests:
    runs-on: ubuntu-latest
    services:
      customer-service:
        image: customer-service:latest
        ports:
          - 8081:8081
      inventory-service:
        image: inventory-service:latest
        ports:
          - 8082:8082
      order-service:
        image: order-service:latest
        ports:
          - 8083:8083
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
          
      - name: Start frontend
        run: |
          cd frontend
          npm start &
          npx wait-on http://localhost:3000
          
      - name: Run Selenium tests
        run: |
          cd frontend
          npm run test:e2e
          
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: e2e-test-results
          path: frontend/test-results/

  cucumber-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
          
      - name: Run Cucumber tests
        run: |
          cd frontend
          npm run test:bdd
          
      - name: Generate Cucumber report
        if: always()
        run: |
          cd frontend
          npm run report:bdd
          
      - name: Upload Cucumber report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: cucumber-report
          path: frontend/cucumber-report.html
```

### Package.json Test Scripts

Add to `frontend/package.json`:

```json
{
  "scripts": {
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "test:e2e": "jest --config jest.e2e.config.js",
    "test:bdd": "cucumber-js",
    "test:all": "npm run test && npm run test:e2e && npm run test:bdd",
    "report:bdd": "node ./tests/utils/generate-cucumber-report.js"
  }
}
```

### Jest Configuration Files

**File**: `frontend/jest.config.js`
```javascript
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/tests/setup.js'],
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
  },
  collectCoverageFrom: [
    'src/**/*.{js,jsx}',
    '!src/index.js',
    '!src/reportWebVitals.js',
  ],
  coverageThresholds: {
    global: {
      statements: 80,
      branches: 75,
      functions: 80,
      lines: 80,
    },
  },
  testMatch: [
    '**/__tests__/**/*.[jt]s?(x)',
    '**/?(*.)+(spec|test).[jt]s?(x)',
    '!**/*.e2e.test.js',
  ],
};
```

**File**: `frontend/jest.e2e.config.js`
```javascript
module.exports = {
  testEnvironment: 'node',
  testMatch: ['**/tests/e2e/**/*.spec.js'],
  setupFilesAfterEnv: ['<rootDir>/tests/e2e/setup.js'],
  testTimeout: 30000,
};
```

---

## Test Execution Plan

### Phase 1: Unit Tests (Week 1)
1. API service tests
2. Component unit tests (Customers, Products, Orders)
3. Form validation tests
4. Utility function tests

### Phase 2: Integration Tests (Week 2)
1. Customer-Order integration
2. Product-Order integration
3. Multi-component workflows

### Phase 3: BDD Tests (Week 3)
1. Customer management features
2. Product management features
3. Order workflow features
4. Step definitions implementation

### Phase 4: E2E Tests (Week 4)
1. Complete user journeys
2. Cross-browser testing (Chrome, Firefox, Safari)
3. Performance testing
4. Accessibility testing

### Phase 5: CI/CD Integration (Week 5)
1. GitHub Actions workflow setup
2. Automated test execution
3. Coverage reporting
4. Test result artifacts

---

## Copilot Prompt Templates

### For Unit Tests
```
Create Jest unit tests for the [Component Name] component that cover:
- Rendering with default props
- User interactions (button clicks, form submissions)
- API call mocking and responses
- Error handling scenarios
- Loading states
Use React Testing Library and follow the test structure defined in TEST_PLAN.md
```

### For BDD Tests
```
Create a Cucumber feature file for [Feature Name] following the examples in TEST_PLAN.md.
Include scenarios for:
- Happy path
- Validation errors
- Edge cases
Then create the corresponding step definitions in JavaScript using Selenium WebDriver.
```

### For E2E Tests
```
Create a Selenium WebDriver E2E test for the complete [workflow name] that:
- Navigates through multiple pages
- Performs CRUD operations
- Verifies data persistence
- Validates UI state changes
Follow the patterns in TEST_PLAN.md section [E2E Tests with Selenium]
```

---

## Success Metrics

### Test Coverage Metrics
- Overall coverage: ≥80%
- Critical paths: 100%
- API layer: ≥90%
- Components: ≥85%

### Test Execution Metrics
- Unit tests: <5 seconds
- Integration tests: <30 seconds
- E2E tests: <5 minutes
- Full suite: <10 minutes

### Quality Metrics
- Test flakiness: <2%
- Bug escape rate: <5%
- Mean time to detect: <24 hours
- Test maintenance time: <10% of development time

---

## Maintenance Guidelines

### Test Review Checklist
- [ ] Tests are independent and can run in any order
- [ ] No hardcoded delays (use wait conditions)
- [ ] Mock data is isolated and realistic
- [ ] Test names clearly describe what is being tested
- [ ] Assertions are specific and meaningful
- [ ] Cleanup is performed after each test
- [ ] Tests follow DRY principles with helper functions

### When to Update Tests
- Feature changes or additions
- Bug fixes that exposed test gaps
- Refactoring that changes component structure
- API contract changes
- UI/UX improvements

---

## Appendix

### Required Dependencies

```json
{
  "devDependencies": {
    "@testing-library/react": "^14.0.0",
    "@testing-library/jest-dom": "^6.1.0",
    "@testing-library/user-event": "^14.5.0",
    "@cucumber/cucumber": "^10.0.0",
    "axios-mock-adapter": "^1.22.0",
    "cucumber-html-reporter": "^7.1.0",
    "jest": "^29.7.0",
    "jest-environment-jsdom": "^29.7.0",
    "selenium-webdriver": "^4.15.0",
    "chromedriver": "^119.0.0"
  }
}
```

### Useful Resources
- [Jest Documentation](https://jestjs.io/)
- [React Testing Library](https://testing-library.com/react)
- [Cucumber.js](https://cucumber.io/docs/cucumber/)
- [Selenium WebDriver](https://www.selenium.dev/documentation/)
- [Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
