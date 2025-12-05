# E-Commerce Microservices Demo - Angular Frontend

This Angular application provides a minimal GUI for experimenting with UI testing frameworks. It displays data from three Spring Boot microservices (Customers, Products, Orders) and includes scaffolding for multiple testing frameworks.

## Prerequisites

- **Node.js 20+** and **npm**
- Three microservices running:
  - Customer Service on port 8081
  - Inventory Service on port 8082
  - Order Service on port 8083

## Getting Started

### Installation

```bash
cd frontend
npm install
```

### Development Server

To start the Angular development server:

```bash
npm start
```

Navigate to `http://localhost:4200/`. The application will automatically reload when you modify source files.

## Features

### Components

- **Home** - Welcome page with navigation to all views
- **Customers** - Displays customer data from Customer Service (port 8081)
- **Products** - Displays product data from Inventory Service (port 8082)
- **Orders** - Displays order data from Order Service (port 8083)

### Technologies

- **Angular 21** - Latest version of Angular framework
- **Angular Material** - Material Design components for UI
- **RxJS** - Reactive programming with observables
- **TypeScript** - Strongly-typed JavaScript

## Testing Frameworks

This project includes scaffolding for four testing frameworks:

### 1. Jest (Unit Testing)

Configuration: `jest.config.js`, `setup-jest.ts`

```bash
npm run test:jest
```

### 2. TestCafe (E2E Testing)

Configuration: `.testcaferc.json`
Tests: `e2e/testcafe/*.testcafe.ts`

```bash
npm run test:testcafe
```

### 3. Cucumber (BDD Testing)

Configuration: `cucumber.js`
Features: `e2e/cucumber/features/*.feature`
Step Definitions: `e2e/cucumber/step_definitions/*.steps.ts`

```bash
npm run test:cucumber
```

### 4. Selenium WebDriver (E2E Testing)

Tests: `e2e/selenium/*.selenium.ts`

```bash
npm run test:selenium
```

**Note:** These are scaffolds only - actual test implementations are left for you to complete.

## Building

To build the project for production:

```bash
npm run build
```

Build artifacts will be stored in the `dist/` directory.

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/         # Angular components
│   │   │   ├── home/          # Home page component
│   │   │   ├── customers/     # Customers display component
│   │   │   ├── products/      # Products display component
│   │   │   └── orders/        # Orders display component
│   │   ├── services/          # API service layer
│   │   │   ├── customer.ts    # Customer service API client
│   │   │   ├── product.ts     # Product service API client
│   │   │   └── order.ts       # Order service API client
│   │   ├── app.ts             # Root component
│   │   ├── app.routes.ts      # Application routing
│   │   └── app.config.ts      # Application configuration
│   ├── index.html             # Main HTML file
│   └── styles.css             # Global styles
├── e2e/                       # End-to-end tests
│   ├── testcafe/             # TestCafe tests
│   ├── cucumber/             # Cucumber BDD tests
│   └── selenium/             # Selenium tests
├── jest.config.js            # Jest configuration
├── cucumber.js               # Cucumber configuration
└── .testcaferc.json          # TestCafe configuration
```

## API Endpoints

The frontend communicates with these microservice endpoints:

- **Customer Service**: `http://localhost:8081/api/customers`
- **Inventory Service**: `http://localhost:8082/api/products`
- **Order Service**: `http://localhost:8083/api/orders`

## Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [Angular Material Documentation](https://material.angular.io/)
- [Jest Documentation](https://jestjs.io/)
- [TestCafe Documentation](https://testcafe.io/)
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Selenium WebDriver Documentation](https://www.selenium.dev/documentation/)
