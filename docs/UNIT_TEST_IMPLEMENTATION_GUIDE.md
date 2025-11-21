# Unit Test Implementation Guide - Microservices

## Overview

This guide provides step-by-step instructions for implementing comprehensive unit tests for the three microservices in this project. Current test coverage is **0%**. The target is **80%+ coverage** for each service.

## Testing Stack

- **JUnit 5** (Jupiter) - managed by Spring Boot 3.2.0
- **Mockito** - for mocking dependencies
- **Spring Test** - @WebMvcTest, @DataJpaTest, @SpringBootTest
- **MockMvc** - for controller testing
- **MockRestServiceServer** - for REST client testing
- **AssertJ** - for fluent assertions

## Testing Conventions

### Test Structure Pattern (AAA)
All tests must follow the Arrange-Act-Assert pattern:
1. **Arrange**: Set up test data and mock behaviors
2. **Act**: Execute the method under test
3. **Assert**: Verify the expected results

### Test Naming Convention
Use the format: `should[ExpectedBehavior]When[Condition]()`

Examples:
- `shouldReturnCustomerWhenIdExists()`
- `shouldThrowExceptionWhenEmailDuplicate()`
- `shouldReserveStockWhenQuantityAvailable()`

### Required Annotations
- `@DisplayName` on all test classes and test methods
- `@Test` on each test method
- Appropriate Spring test annotations (@WebMvcTest, @ExtendWith, @RestClientTest)

---

## Phase 1: Customer Service Tests

**Priority: START HERE** (Simplest service - use as template for others)

### Step 1: Create Controller Tests

**File**: `customer-service/src/test/java/com/fisglobal/customer/controller/CustomerControllerTest.java`

**Setup:**
1. Use `@WebMvcTest(CustomerController.class)` annotation
2. Inject `MockMvc` with `@Autowired`
3. Inject `ObjectMapper` with `@Autowired`
4. Mock `CustomerService` with `@MockBean`

**Implement these 6 test methods:**

1. **shouldReturnAllCustomers()**
   - Mock `customerService.getAllCustomers()` to return a list of customers
   - Perform GET request to `/api/customers`
   - Assert status is 200 OK
   - Assert response is a JSON array
   - Assert customer data is correct

2. **shouldReturnCustomerWhenValidId()**
   - Mock `customerService.getCustomerById(1L)` to return Optional.of(customer)
   - Perform GET request to `/api/customers/1`
   - Assert status is 200 OK
   - Assert customer fields are correct in response

3. **shouldReturn404WhenInvalidId()**
   - Mock `customerService.getCustomerById(999L)` to return Optional.empty()
   - Perform GET request to `/api/customers/999`
   - Assert status is 404 NOT FOUND

4. **shouldCreateCustomerWhenValidData()**
   - Mock `customerService.createCustomer(any())` to return customer with ID
   - Perform POST request to `/api/customers` with JSON body
   - Assert status is 201 CREATED
   - Assert returned customer has ID and correct data

5. **shouldUpdateCustomerWhenValidData()**
   - Mock `customerService.updateCustomer(eq(1L), any())` to return updated customer
   - Perform PUT request to `/api/customers/1` with JSON body
   - Assert status is 200 OK
   - Assert updated fields are correct

6. **shouldDeleteCustomerWhenValidId()**
   - No mocking needed (void method)
   - Perform DELETE request to `/api/customers/1`
   - Assert status is 204 NO CONTENT

### Step 2: Create Service Tests

**File**: `customer-service/src/test/java/com/fisglobal/customer/service/CustomerServiceTest.java`

**Setup:**
1. Use `@ExtendWith(MockitoExtension.class)` annotation
2. Mock `CustomerRepository` with `@Mock`
3. Inject `CustomerService` with `@InjectMocks`

**Implement these 8 test methods:**

1. **shouldReturnAllCustomers()**
   - Mock `customerRepository.findAll()` to return list
   - Call `customerService.getAllCustomers()`
   - Assert list size and content
   - Verify repository.findAll() was called once

2. **shouldReturnCustomerWhenValidId()**
   - Mock `customerRepository.findById(1L)` to return Optional.of(customer)
   - Call `customerService.getCustomerById(1L)`
   - Assert Optional is present
   - Assert customer data is correct

3. **shouldReturnEmptyWhenInvalidId()**
   - Mock `customerRepository.findById(999L)` to return Optional.empty()
   - Call `customerService.getCustomerById(999L)`
   - Assert Optional is empty

4. **shouldCreateCustomerWhenValidData()**
   - Mock `customerRepository.save(any())` to return customer with ID
   - Call `customerService.createCustomer(customer)`
   - Assert returned customer has ID
   - Verify repository.save() was called

5. **shouldUpdateCustomerWhenValidData()**
   - Mock `customerRepository.findById(1L)` to return existing customer
   - Mock `customerRepository.save(any())` to return updated customer
   - Call `customerService.updateCustomer(1L, updatedData)`
   - Assert fields are updated
   - Verify repository methods called

6. **shouldDeleteCustomer()**
   - Mock `customerRepository.deleteById(1L)` to do nothing
   - Call `customerService.deleteCustomer(1L)`
   - Verify deleteById was called once

7. **shouldFindCustomerByEmail()**
   - Mock `customerRepository.findByEmail(email)` to return Optional.of(customer)
   - Call `customerService.findByEmail(email)`
   - Assert Optional is present with correct customer

8. **shouldThrowExceptionWhenEmailAlreadyExists()** (if applicable)
   - Mock `customerRepository.existsByEmail(email)` to return true
   - Call `customerService.createCustomer(customer)`
   - Assert exception is thrown with appropriate message

### Step 3: Verify Customer Service Tests

Run tests:
```bash
cd customer-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean test
```

Ensure:
- All tests pass (green)
- No compilation errors
- Tests follow naming conventions
- @DisplayName annotations present

---

## Phase 2: Inventory Service Tests

**Priority: SECOND** (Stock management logic is critical)

### Step 1: Create Controller Tests

**File**: `inventory-service/src/test/java/com/fisglobal/inventory/controller/ProductControllerTest.java`

**Setup:**
1. Use `@WebMvcTest(ProductController.class)` annotation
2. Inject `MockMvc` and `ObjectMapper`
3. Mock `ProductService` with `@MockBean`

**Implement these 8 test methods:**

1. **shouldReturnAllProducts()**
   - Similar to customer service GET all

2. **shouldReturnProductWhenValidId()**
   - Similar to customer service GET by ID

3. **shouldReturn404WhenInvalidId()**
   - Similar to customer service 404 test

4. **shouldCreateProductWhenValidData()**
   - Similar to customer service POST test

5. **shouldUpdateProductWhenValidData()**
   - Similar to customer service PUT test

6. **shouldDeleteProductWhenValidId()**
   - Similar to customer service DELETE test

7. **shouldReserveStockWhenSufficientQuantity()**
   - Mock `productService.reserveStock(1L, 10)` to return product with reduced stock
   - Perform POST to `/api/products/1/reserve?quantity=10`
   - Assert status is 200 OK
   - Assert stock was reduced correctly

8. **shouldReturn400WhenInsufficientStock()**
   - Mock `productService.reserveStock(1L, 1000)` to throw exception
   - Perform POST to `/api/products/1/reserve?quantity=1000`
   - Assert status is 400 BAD REQUEST

### Step 2: Create Service Tests

**File**: `inventory-service/src/test/java/com/fisglobal/inventory/service/ProductServiceTest.java`

**Setup:**
1. Use `@ExtendWith(MockitoExtension.class)` annotation
2. Mock `ProductRepository` with `@Mock`
3. Inject `ProductService` with `@InjectMocks`

**Implement these 10 test methods:**

1-6. **Basic CRUD operations** (similar to CustomerService):
   - shouldReturnAllProducts()
   - shouldReturnProductWhenValidId()
   - shouldReturnEmptyWhenInvalidId()
   - shouldCreateProductWhenValidData()
   - shouldUpdateProductWhenValidData()
   - shouldDeleteProduct()

7. **shouldReserveStockWhenSufficientQuantity()**
   - Mock `productRepository.findById(1L)` to return product with stock 50
   - Mock `productRepository.save(any())` to return product
   - Call `productService.reserveStock(1L, 10)`
   - Assert product stock is now 40
   - Verify save was called

8. **shouldThrowExceptionWhenInsufficientStock()**
   - Mock `productRepository.findById(1L)` to return product with stock 5
   - Call `productService.reserveStock(1L, 10)`
   - Assert IllegalStateException is thrown
   - Assert exception message contains "Insufficient stock"

9. **shouldRestoreStockWhenCalled()**
   - Mock `productRepository.findById(1L)` to return product with stock 40
   - Mock `productRepository.save(any())` to return product
   - Call `productService.restoreStock(1L, 10)`
   - Assert product stock is now 50
   - Verify save was called

10. **shouldFindLowStockProducts()**
    - Mock `productRepository.findByStockQuantityLessThanEqual(10)` to return low stock list
    - Call `productService.getLowStockProducts(10)`
    - Assert list contains correct products
    - Verify repository method called

### Step 3: Verify Inventory Service Tests

Run tests:
```bash
cd inventory-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean test
```

---

## Phase 3: Order Service Tests

**Priority: THIRD** (Most complex - includes REST client integration)

### Step 1: Create Controller Tests

**File**: `order-service/src/test/java/com/fisglobal/order/controller/OrderControllerTest.java`

**Setup:**
1. Use `@WebMvcTest(OrderController.class)` annotation
2. Inject `MockMvc` and `ObjectMapper`
3. Mock `OrderService` with `@MockBean`

**Implement these 7 test methods:**

1. **shouldReturnAllOrders()**
2. **shouldReturnOrderWhenValidId()**
3. **shouldReturn404WhenInvalidId()**
4. **shouldCreateOrderWhenValidData()**
5. **shouldUpdateOrderStatusWhenValid()**
6. **shouldCancelOrderWhenValidId()**
7. **shouldReturn400WhenInvalidOrderRequest()**

### Step 2: Create Service Tests

**File**: `order-service/src/test/java/com/fisglobal/order/service/OrderServiceTest.java`

**Setup:**
1. Use `@ExtendWith(MockitoExtension.class)` annotation
2. Mock `OrderRepository` with `@Mock`
3. Mock `OrderItemRepository` with `@Mock`
4. Mock `CustomerClient` with `@Mock`
5. Mock `InventoryClient` with `@Mock`
6. Inject `OrderService` with `@InjectMocks`

**Implement these 12 test methods:**

1. **shouldReturnAllOrders()**
   - Mock repository.findAll()
   - Call service.getAllOrders()
   - Assert and verify

2. **shouldReturnOrderWhenValidId()**
   - Mock repository.findById()
   - Call service.getOrderById()
   - Assert and verify

3. **shouldCreateOrderWhenValidCustomerAndStock()**
   - Mock `customerClient.getCustomerById(1L)` to return CustomerDTO
   - Mock `inventoryClient.getProductById(1L)` to return ProductDTO with stock 50
   - Mock `inventoryClient.reserveStock(1L, 2)` to return success
   - Mock `orderRepository.save(any())` to return order
   - Create order request with customerId=1, productId=1, quantity=2
   - Call `orderService.createOrder(request)`
   - Assert order created successfully
   - Verify customerClient.getCustomerById() called
   - Verify inventoryClient.reserveStock() called
   - Verify orderRepository.save() called

4. **shouldThrowExceptionWhenCustomerNotFound()**
   - Mock `customerClient.getCustomerById(999L)` to throw exception
   - Create order request with customerId=999
   - Call `orderService.createOrder(request)`
   - Assert exception is thrown
   - Verify inventoryClient was NOT called

5. **shouldThrowExceptionWhenProductNotFound()**
   - Mock `customerClient.getCustomerById(1L)` to return customer
   - Mock `inventoryClient.getProductById(999L)` to throw exception
   - Create order request with productId=999
   - Call `orderService.createOrder(request)`
   - Assert exception is thrown

6. **shouldThrowExceptionWhenInsufficientStock()**
   - Mock customerClient to return customer
   - Mock inventoryClient.getProductById() to return product with stock 5
   - Mock inventoryClient.reserveStock() to throw InsufficientStockException
   - Create order request with quantity=10
   - Call `orderService.createOrder(request)`
   - Assert exception is thrown

7. **shouldRestoreStockWhenOrderCancelled()**
   - Create existing order with 2 items (productId 1, qty 2) and (productId 2, qty 1)
   - Mock `orderRepository.findById(1L)` to return order
   - Mock `orderRepository.save(any())` to return order
   - Call `orderService.cancelOrder(1L)`
   - Verify `inventoryClient.restoreStock(1L, 2)` called
   - Verify `inventoryClient.restoreStock(2L, 1)` called
   - Verify order status updated to CANCELLED

8. **shouldUpdateOrderStatusWhenValid()**
   - Mock repository.findById() to return order
   - Mock repository.save() to return order
   - Call `orderService.updateOrderStatus(1L, "CONFIRMED")`
   - Assert status updated
   - Verify save called

9. **shouldFindOrdersByCustomerId()**
   - Mock `orderRepository.findByCustomerId(1L)` to return list
   - Call `orderService.getOrdersByCustomerId(1L)`
   - Assert list correct

10. **shouldFindOrdersByStatus()**
    - Mock `orderRepository.findByStatus("PENDING")` to return list
    - Call `orderService.getOrdersByStatus("PENDING")`
    - Assert list correct

11. **shouldHandleCustomerServiceUnavailable()**
    - Mock `customerClient.getCustomerById()` to throw ServiceUnavailableException
    - Call `orderService.createOrder(request)`
    - Assert appropriate exception thrown
    - Verify no stock was reserved

12. **shouldHandleInventoryServiceUnavailable()**
    - Mock customerClient to return customer
    - Mock `inventoryClient.getProductById()` to throw ServiceUnavailableException
    - Call `orderService.createOrder(request)`
    - Assert appropriate exception thrown

### Step 3: Create REST Client Tests

**File**: `order-service/src/test/java/com/fisglobal/order/client/CustomerClientTest.java`

**Setup:**
1. Use `@RestClientTest(CustomerClient.class)` annotation
2. Inject `CustomerClient` with `@Autowired`
3. Inject `MockRestServiceServer` with `@Autowired`

**Implement these 3 test methods:**

1. **shouldReturnCustomerWhenValidId()**
   - Use `mockServer.expect(requestTo("http://localhost:8081/api/customers/1"))`
   - Mock response with `withSuccess(jsonResponse, MediaType.APPLICATION_JSON)`
   - Call `customerClient.getCustomerById(1L)`
   - Assert CustomerDTO returned correctly
   - Call `mockServer.verify()` to ensure request was made

2. **shouldThrowExceptionWhenCustomerNotFound()**
   - Mock server to respond with `withResourceNotFound()`
   - Call `customerClient.getCustomerById(999L)`
   - Assert exception is thrown
   - Verify server interaction

3. **shouldThrowExceptionWhenServiceUnavailable()**
   - Mock server to respond with `withServerError()`
   - Call `customerClient.getCustomerById(1L)`
   - Assert exception is thrown
   - Verify server interaction

**File**: `order-service/src/test/java/com/fisglobal/order/client/InventoryClientTest.java`

**Setup:**
1. Use `@RestClientTest(InventoryClient.class)` annotation
2. Inject `InventoryClient` with `@Autowired`
3. Inject `MockRestServiceServer` with `@Autowired`

**Implement these 4 test methods:**

1. **shouldReturnProductWhenValidId()**
   - Mock GET request to `http://localhost:8082/api/products/1`
   - Mock 200 OK response with product JSON
   - Call `inventoryClient.getProductById(1L)`
   - Assert ProductDTO returned correctly
   - Verify server interaction

2. **shouldReserveStockSuccessfully()**
   - Mock POST request to `http://localhost:8082/api/products/1/reserve?quantity=10`
   - Mock 200 OK response
   - Call `inventoryClient.reserveStock(1L, 10)`
   - Assert result is true/successful
   - Verify server interaction

3. **shouldRestoreStockSuccessfully()**
   - Mock POST request to `http://localhost:8082/api/products/1/restore?quantity=10`
   - Mock 200 OK response
   - Call `inventoryClient.restoreStock(1L, 10)`
   - Assert result is successful
   - Verify server interaction

4. **shouldThrowExceptionWhenServiceUnavailable()**
   - Mock server error (503)
   - Call any client method
   - Assert exception is thrown
   - Verify server interaction

### Step 4: Verify Order Service Tests

Run tests:
```bash
cd order-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean test
```

---

## Final Verification

### Run All Tests

Execute from project root:
```bash
./test-all.sh
```

Or manually:
```bash
# Customer Service
cd customer-service && JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean test && cd ..

# Inventory Service
cd inventory-service && JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean test && cd ..

# Order Service
cd order-service && JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean test && cd ..
```

### Add Coverage Reporting (Optional)

Add JaCoCo plugin to each service's `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Generate coverage reports:
```bash
cd customer-service
mvn clean test jacoco:report
# View: target/site/jacoco/index.html

cd ../inventory-service
mvn clean test jacoco:report

cd ../order-service
mvn clean test jacoco:report
```

---

## Quality Checklist

Before considering the implementation complete, verify:

- [ ] All test classes use `@DisplayName` annotation
- [ ] All test methods use `@DisplayName` annotation
- [ ] All test methods follow naming convention: `should[Expected]When[Condition]()`
- [ ] All tests follow AAA pattern (Arrange, Act, Assert)
- [ ] All external dependencies are mocked (repositories, REST clients)
- [ ] Both success and failure scenarios are tested
- [ ] HTTP status codes are verified in controller tests
- [ ] Mock interactions are verified with `verify()` calls
- [ ] AssertJ assertions are used (prefer `assertThat()` over `assertEquals()`)
- [ ] All tests pass without errors
- [ ] Test coverage is 80%+ for each service
- [ ] No compilation warnings or errors

---

## Test File Summary

### Customer Service (2 files, 14 tests)
- `CustomerControllerTest.java` - 6 tests
- `CustomerServiceTest.java` - 8 tests

### Inventory Service (2 files, 18 tests)
- `ProductControllerTest.java` - 8 tests
- `ProductServiceTest.java` - 10 tests

### Order Service (4 files, 26 tests)
- `OrderControllerTest.java` - 7 tests
- `OrderServiceTest.java` - 12 tests
- `CustomerClientTest.java` - 3 tests
- `InventoryClientTest.java` - 4 tests

**Total: 8 test files, 58 test methods**

---

## Implementation Timeline

- **Week 1**: Customer Service (template for others)
- **Week 2**: Inventory Service (stock logic)
- **Week 3**: Order Service - Controllers & Basic Service
- **Week 4**: Order Service - REST Clients & Integration Tests

---

## Notes

- Start with Customer Service - it's the simplest and serves as a template
- Focus on controller and service layers first
- Add REST client tests last (Order Service only)
- Run tests frequently during development
- Use GitHub Copilot to generate test boilerplate from method signatures
- Refer to existing controller/service patterns in the codebase
- All services use H2 in-memory databases for testing
- No need to start actual services for unit tests (use mocks)
