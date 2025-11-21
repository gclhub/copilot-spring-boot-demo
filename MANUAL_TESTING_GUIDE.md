# Manual Testing Guide

This guide provides step-by-step instructions for launching and manually testing the E-Commerce application with its new GUI frontend.

## Prerequisites

Before you begin, ensure you have the following installed:
- **Java 17** or higher
- **Maven 3.6+**
- **Node.js 14+** and **npm**
- A web browser (Chrome, Firefox, Safari, or Edge)

## Quick Start (All-in-One)

If you're on macOS or Linux, you can use the provided scripts:

```bash
# From the project root directory
./start-all-services.sh
```

Then in a new terminal:

```bash
cd frontend
npm install
npm start
```

The application will automatically open at `http://localhost:3000` in your default browser.

## Manual Step-by-Step Launch Instructions

### Step 1: Start the Backend Microservices

You need to start all three backend microservices before launching the frontend.

#### Option A: Start Services Individually (Recommended for Manual Testing)

Open **three separate terminal windows** and run each service:

**Terminal 1 - Customer Service (Port 8081):**
```bash
cd customer-service
mvn spring-boot:run
```

Wait for the message: `Started CustomerServiceApplication in X seconds`

**Terminal 2 - Inventory Service (Port 8082):**
```bash
cd inventory-service
mvn spring-boot:run
```

Wait for the message: `Started InventoryServiceApplication in X seconds`

**Terminal 3 - Order Service (Port 8083):**
```bash
cd order-service
mvn spring-boot:run
```

Wait for the message: `Started OrderServiceApplication in X seconds`

#### Option B: Build JAR Files and Run (Faster Startup)

First, build all services:
```bash
./build-all-services.sh
```

Then start each service with the JAR files:

**Terminal 1 - Customer Service:**
```bash
java -jar customer-service/target/customer-service-1.0.0-SNAPSHOT.jar
```

**Terminal 2 - Inventory Service:**
```bash
java -jar inventory-service/target/inventory-service-1.0.0-SNAPSHOT.jar
```

**Terminal 3 - Order Service:**
```bash
java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar
```

### Step 2: Verify Backend Services

Before starting the frontend, verify that all services are running:

```bash
# Test Customer Service
curl http://localhost:8081/api/customers

# Test Inventory Service
curl http://localhost:8082/api/products

# Test Order Service
curl http://localhost:8083/api/orders
```

Each command should return JSON data. If you get a connection error, wait a few more seconds and try again.

### Step 3: Install Frontend Dependencies (First Time Only)

Open a **new terminal window** and navigate to the frontend directory:

```bash
cd frontend
npm install
```

This may take a few minutes. You only need to do this once, or when dependencies change.

### Step 4: Start the Frontend Application

From the `frontend` directory:

```bash
npm start
```

The development server will start and automatically open your default browser to `http://localhost:3000`.

If it doesn't open automatically, manually navigate to: **http://localhost:3000**

### Step 5: Verify Frontend is Running

You should see the E-Commerce Management System interface with:
- A header titled "E-Commerce Management System"
- Three navigation tabs: Customers, Products, Orders
- A footer showing "Testing frameworks: Jest | Cucumber | Selenium"

## Manual Testing Scenarios

### Test 1: View Customers

1. The **Customers** tab should be active by default (highlighted in cyan)
2. You should see a table with customer data including:
   - ID, Name, Email, Phone, City columns
   - At least 3 customers (John Doe, Jane Smith, Bob Johnson)
3. Verify all customer information displays correctly

**Expected Result:** Customer data loads and displays in a table format.

### Test 2: View Products

1. Click on the **Products** tab
2. Wait for the products to load (you should see a brief "Loading products..." message)
3. Verify the product table shows:
   - ID, Name, SKU, Category, Price, Stock columns
   - At least 6 products (Laptop, Wireless Mouse, USB-C Cable, etc.)
4. Check that prices are formatted correctly (e.g., $999.99)
5. Verify stock quantities are displayed

**Expected Result:** Product inventory displays with proper formatting.

### Test 3: View Orders

1. Click on the **Orders** tab
2. The table should display order information with columns:
   - ID, Order Number, Customer ID, Status, Total Amount, Date
3. Orders may or may not exist depending on whether any have been created
4. If no orders exist, you should see "No orders found."

**Expected Result:** Order list displays (may be empty initially).

### Test 4: Tab Navigation

1. Click through each tab multiple times: Customers → Products → Orders → Customers
2. Verify that:
   - The active tab is always highlighted
   - Data loads correctly each time you switch tabs
   - No errors appear in the browser console (press F12 to open Developer Tools)

**Expected Result:** Smooth navigation between tabs without errors.

### Test 5: Error Handling (Backend Stopped)

1. Stop one of the backend services (e.g., Customer Service - press Ctrl+C in its terminal)
2. In the frontend, navigate to the Customers tab
3. You should see an error message: "Failed to fetch customers. Make sure the customer service is running on port 8081."
4. Click the **Retry** button
5. Restart the Customer Service
6. Click **Retry** again

**Expected Result:** Error message displays when service is down, data loads after service restarts.

### Test 6: Browser Responsiveness

1. Resize your browser window to different widths
2. Verify the interface remains usable at different sizes
3. Try the application in different browsers (Chrome, Firefox, Safari, Edge)

**Expected Result:** Interface adapts to different screen sizes and browsers.

### Test 7: Console Verification

1. Open your browser's Developer Tools (F12 or Right-click → Inspect)
2. Go to the Console tab
3. Navigate through the application
4. Verify there are no red error messages (some warnings are okay)

**Expected Result:** No critical errors in the console.

## Stopping the Application

### Stop Frontend
In the terminal running the frontend (npm start), press: **Ctrl+C**

### Stop Backend Services
In each terminal running a backend service, press: **Ctrl+C**

Or use the stop script:
```bash
./stop-all-services.sh
```

## Troubleshooting

### Frontend Won't Start

**Problem:** `npm start` fails or shows errors

**Solutions:**
1. Delete `node_modules` and reinstall:
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   npm start
   ```

2. Clear npm cache:
   ```bash
   npm cache clean --force
   npm install
   ```

### Port Already in Use

**Problem:** "Port 3000 is already in use" or "Port 8081/8082/8083 is already in use"

**Solutions:**
1. Find and kill the process using the port:
   ```bash
   # On macOS/Linux
   lsof -ti:3000 | xargs kill -9
   lsof -ti:8081 | xargs kill -9
   
   # On Windows
   netstat -ano | findstr :3000
   taskkill /PID <PID> /F
   ```

2. Or use a different port for the frontend:
   ```bash
   PORT=3001 npm start
   ```

### Backend Service Won't Start

**Problem:** Maven build fails or service won't start

**Solutions:**
1. Ensure Java 17 is installed:
   ```bash
   java -version
   ```

2. Clean and rebuild:
   ```bash
   cd customer-service  # or inventory-service, order-service
   mvn clean install
   mvn spring-boot:run
   ```

3. Check if another instance is already running on the port

### Data Not Loading in Frontend

**Problem:** Tables show "Loading..." forever or display error messages

**Solutions:**
1. Verify all backend services are running:
   ```bash
   curl http://localhost:8081/api/customers
   curl http://localhost:8082/api/products
   curl http://localhost:8083/api/orders
   ```

2. Check browser console for CORS errors (F12 → Console)

3. Restart the backend service that's having issues

4. Clear browser cache and hard reload (Ctrl+Shift+R or Cmd+Shift+R)

### Browser Doesn't Open Automatically

**Problem:** `npm start` runs but browser doesn't open

**Solution:**
- Manually navigate to `http://localhost:3000` in your browser
- Or set the BROWSER environment variable:
  ```bash
  BROWSER=chrome npm start  # Use 'firefox', 'safari', etc.
  ```

## Additional Testing with API Clients

You can also test the backend APIs directly using tools like:

### Using cURL

```bash
# Get all customers
curl http://localhost:8081/api/customers

# Get all products
curl http://localhost:8082/api/products

# Create an order
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 2}
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
```

### Using Postman or Insomnia

1. Import the API endpoints
2. Create a new request for each endpoint
3. Test GET, POST, PUT, DELETE operations
4. Verify the frontend updates reflect API changes

## Performance Testing

### Load Testing the Frontend

1. Open multiple browser tabs with the application
2. Navigate between tabs in each window simultaneously
3. Monitor browser performance (F12 → Performance tab)

### Load Testing the Backend

Use tools like Apache Bench or wrk:

```bash
# Test customer service
ab -n 1000 -c 10 http://localhost:8081/api/customers

# Test inventory service
ab -n 1000 -c 10 http://localhost:8082/api/products
```

## Logs and Debugging

### Frontend Logs
- Browser console (F12 → Console)
- Network tab for API calls (F12 → Network)

### Backend Logs
- Terminal output where services are running
- Check for errors in Spring Boot startup logs
- Look for HTTP request/response logs

## Next Steps

After verifying the application works:

1. Explore the testing documentation in `frontend/TESTING.md`
2. Try writing your own Jest tests
3. Create Cucumber scenarios for user workflows
4. Set up Selenium for browser automation testing

## Summary Checklist

Before considering manual testing complete, verify:

- [ ] All three backend services start successfully
- [ ] Frontend starts and opens in browser
- [ ] Customers tab displays data correctly
- [ ] Products tab displays data correctly
- [ ] Orders tab displays (even if empty)
- [ ] Tab navigation works smoothly
- [ ] Error handling works when service is stopped
- [ ] No critical errors in browser console
- [ ] Application works in your primary browser
- [ ] Data refreshes correctly after navigation

## Support

For issues or questions:
1. Check the main `README.md` for project overview
2. Review `frontend/README.md` for frontend-specific information
3. See `frontend/TESTING.md` for testing documentation
4. Check `FRONTEND_IMPLEMENTATION.md` for technical details
