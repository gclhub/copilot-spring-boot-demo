# Customer Service

## Overview
The Customer Service is a microservice responsible for managing customer information in the e-commerce application.

## Port
8081

## Database
H2 in-memory database: `customerdb`

## Endpoints

### Get All Customers
```
GET /api/customers
```

### Get Customer by ID
```
GET /api/customers/{id}
```

### Get Customer by Email
```
GET /api/customers/email/{email}
```

### Create Customer
```
POST /api/customers
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "555-1234",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA"
}
```

### Update Customer
```
PUT /api/customers/{id}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "555-1234",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA"
}
```

### Delete Customer
```
DELETE /api/customers/{id}
```

## Running the Service

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Test
```bash
mvn test
```

## Sample Data
The service initializes with 3 sample customers:
- John Doe (john.doe@example.com)
- Jane Smith (jane.smith@example.com)
- Bob Johnson (bob.johnson@example.com)

## Configuration
See `src/main/resources/application.properties` for configuration options.

## H2 Console
Access at: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:customerdb`
- Username: `sa`
- Password: (empty)
