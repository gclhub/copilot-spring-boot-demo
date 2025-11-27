# E-Commerce Frontend

A minimal React-based GUI frontend for the E-Commerce microservices demo, with support for UI testing using Jest, Cucumber, and Selenium.

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Overview

This frontend application provides a simple interface to interact with the three microservices:
- **Customer Service** (Port 8081) - View and manage customers
- **Inventory Service** (Port 8082) - View and manage products
- **Order Service** (Port 8083) - View and manage orders

## Testing Framework Support

This application is configured to support three popular testing frameworks:

### 1. Jest (Unit Testing)
Jest is pre-configured with Create React App for unit and integration testing.

### 2. Cucumber (BDD Testing)
Cucumber is configured for behavior-driven development testing.
- Run: `npm run test:cucumber`
- Features: `tests/cucumber/features/*.feature`
- Step definitions: `tests/cucumber/step_definitions/*.js`

### 3. Selenium (Browser Automation)
Selenium WebDriver is configured for end-to-end browser testing.
- Run: `npm run test:selenium`
- Test files: `tests/selenium/*.js`

## Prerequisites

- **Node.js 14+** and **npm**
- **Backend microservices** running on ports 8081, 8082, 8083

## Getting Started

### 1. Install Dependencies

```bash
npm install
```

### 2. Start the Backend Services

Make sure all three microservices are running:

```bash
# From the project root directory
../start-all-services.sh
```

### 3. Start the Frontend

```bash
npm start
```

The application will open at `http://localhost:3000`

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

The page will reload when you make changes.\
You may also see any lint errors in the console.

### `npm test`

Launches the test runner in the interactive watch mode.\
See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

### `npm run build`

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.\
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

### `npm run eject`

**Note: this is a one-way operation. Once you `eject`, you can't go back!**

If you aren't satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

Instead, it will copy all the configuration files and the transitive dependencies (webpack, Babel, ESLint, etc) right into your project so you have full control over them. All of the commands except `eject` will still work, but they will point to the copied scripts so you can tweak them. At this point you're on your own.

You don't have to ever use `eject`. The curated feature set is suitable for small and middle deployments, and you shouldn't feel obligated to use this feature. However we understand that this tool wouldn't be useful if you couldn't customize it when you are ready for it.

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).

### Code Splitting

This section has moved here: [https://facebook.github.io/create-react-app/docs/code-splitting](https://facebook.github.io/create-react-app/docs/code-splitting)

### Analyzing the Bundle Size

This section has moved here: [https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size](https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size)

### Making a Progressive Web App

This section has moved here: [https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app](https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app)

### Advanced Configuration

This section has moved here: [https://facebook.github.io/create-react-app/docs/advanced-configuration](https://facebook.github.io/create-react-app/docs/advanced-configuration)

### Deployment

This section has moved here: [https://facebook.github.io/create-react-app/docs/deployment](https://facebook.github.io/create-react-app/docs/deployment)

### `npm run build` fails to minify

This section has moved here: [https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify](https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify)
