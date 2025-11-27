import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { customerApi, inventoryApi, orderApi } from './api';

// Create a mock adapter for axios
const mock = new MockAdapter(axios);

describe('Customer API', () => {
  afterEach(() => {
    mock.reset();
  });

  describe('getAllCustomers', () => {
    it('should return customer list', async () => {
      const mockCustomers = [
        {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@example.com',
          phone: '(555) 123-4567',
          city: 'New York'
        },
        {
          id: 2,
          firstName: 'Jane',
          lastName: 'Smith',
          email: 'jane.smith@example.com',
          phone: '(555) 987-6543',
          city: 'Boston'
        }
      ];

      mock.onGet('http://localhost:8081/api/customers').reply(200, mockCustomers);

      const response = await customerApi.getAllCustomers();
      
      expect(response.data).toEqual(mockCustomers);
      expect(response.data).toHaveLength(2);
      expect(response.data[0].firstName).toBe('John');
    });

    it('should handle errors when fetching customers', async () => {
      mock.onGet('http://localhost:8081/api/customers').reply(500);

      await expect(customerApi.getAllCustomers()).rejects.toThrow();
    });
  });

  describe('getCustomerById', () => {
    it('should return a single customer', async () => {
      const mockCustomer = {
        id: 1,
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phone: '(555) 123-4567',
        address: '123 Main St',
        city: 'New York',
        state: 'NY',
        zipCode: '10001',
        country: 'USA'
      };

      mock.onGet('http://localhost:8081/api/customers/1').reply(200, mockCustomer);

      const response = await customerApi.getCustomerById(1);
      
      expect(response.data).toEqual(mockCustomer);
      expect(response.data.id).toBe(1);
      expect(response.data.email).toBe('john.doe@example.com');
    });

    it('should handle 404 when customer not found', async () => {
      mock.onGet('http://localhost:8081/api/customers/999').reply(404);

      await expect(customerApi.getCustomerById(999)).rejects.toThrow();
    });
  });

  describe('createCustomer', () => {
    it('should create a new customer with correct payload', async () => {
      const newCustomer = {
        firstName: 'Alice',
        lastName: 'Johnson',
        email: 'alice.j@example.com',
        phone: '(555) 111-2222',
        address: '456 Oak Ave',
        city: 'Chicago',
        state: 'IL',
        zipCode: '60601',
        country: 'USA'
      };

      const createdCustomer = { id: 3, ...newCustomer };

      mock.onPost('http://localhost:8081/api/customers').reply(201, createdCustomer);

      const response = await customerApi.createCustomer(newCustomer);
      
      expect(response.data).toEqual(createdCustomer);
      expect(response.data.id).toBe(3);
      expect(response.status).toBe(201);
    });

    it('should handle validation errors', async () => {
      const invalidCustomer = {
        firstName: '',
        lastName: 'Doe',
        email: 'invalid-email',
        phone: ''
      };

      mock.onPost('http://localhost:8081/api/customers').reply(400, {
        message: 'Validation failed'
      });

      await expect(customerApi.createCustomer(invalidCustomer)).rejects.toThrow();
    });
  });

  describe('updateCustomer', () => {
    it('should update an existing customer', async () => {
      const updatedData = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phone: '(555) 999-8888',
        address: '789 New St',
        city: 'Los Angeles',
        state: 'CA',
        zipCode: '90001',
        country: 'USA'
      };

      const updatedCustomer = { id: 1, ...updatedData };

      mock.onPut('http://localhost:8081/api/customers/1').reply(200, updatedCustomer);

      const response = await customerApi.updateCustomer(1, updatedData);
      
      expect(response.data).toEqual(updatedCustomer);
      expect(response.data.phone).toBe('(555) 999-8888');
      expect(response.data.city).toBe('Los Angeles');
    });

    it('should handle concurrent update conflicts', async () => {
      mock.onPut('http://localhost:8081/api/customers/1').reply(409, {
        message: 'Conflict: Customer was modified by another user'
      });

      await expect(customerApi.updateCustomer(1, {})).rejects.toThrow();
    });
  });

  describe('deleteCustomer', () => {
    it('should delete a customer', async () => {
      mock.onDelete('http://localhost:8081/api/customers/1').reply(204);

      const response = await customerApi.deleteCustomer(1);
      
      expect(response.status).toBe(204);
    });

    it('should handle deletion of customer with active orders', async () => {
      mock.onDelete('http://localhost:8081/api/customers/1').reply(400, {
        message: 'Cannot delete customer with active orders'
      });

      await expect(customerApi.deleteCustomer(1)).rejects.toThrow();
    });
  });
});

describe('Inventory API', () => {
  afterEach(() => {
    mock.reset();
  });

  describe('getAllProducts', () => {
    it('should return product list with stock levels', async () => {
      const mockProducts = [
        {
          id: 1,
          name: 'Laptop',
          description: 'High-performance laptop',
          price: 999.99,
          category: 'Electronics',
          stock: 50,
          sku: 'LAP-001'
        },
        {
          id: 2,
          name: 'Mouse',
          description: 'Wireless mouse',
          price: 29.99,
          category: 'Electronics',
          stock: 5,
          sku: 'MOU-001'
        }
      ];

      mock.onGet('http://localhost:8082/api/products').reply(200, mockProducts);

      const response = await inventoryApi.getAllProducts();
      
      expect(response.data).toEqual(mockProducts);
      expect(response.data).toHaveLength(2);
      expect(response.data[0].stock).toBe(50);
      expect(response.data[1].stock).toBe(5);
    });
  });

  describe('createProduct', () => {
    it('should create a new product with all fields', async () => {
      const newProduct = {
        name: 'Keyboard',
        description: 'Mechanical keyboard',
        price: 149.99,
        category: 'Electronics',
        stock: 30,
        sku: 'KEY-001'
      };

      const createdProduct = { id: 3, ...newProduct };

      mock.onPost('http://localhost:8082/api/products').reply(201, createdProduct);

      const response = await inventoryApi.createProduct(newProduct);
      
      expect(response.data).toEqual(createdProduct);
      expect(response.status).toBe(201);
    });

    it('should validate required fields', async () => {
      const invalidProduct = {
        name: '',
        price: -10
      };

      mock.onPost('http://localhost:8082/api/products').reply(400, {
        message: 'Validation failed: name is required and price must be positive'
      });

      await expect(inventoryApi.createProduct(invalidProduct)).rejects.toThrow();
    });
  });

  describe('updateProduct', () => {
    it('should update product details', async () => {
      const updatedData = {
        name: 'Laptop Pro',
        description: 'Updated description',
        price: 1099.99,
        category: 'Electronics',
        stock: 45,
        sku: 'LAP-001'
      };

      const updatedProduct = { id: 1, ...updatedData };

      mock.onPut('http://localhost:8082/api/products/1').reply(200, updatedProduct);

      const response = await inventoryApi.updateProduct(1, updatedData);
      
      expect(response.data.price).toBe(1099.99);
      expect(response.data.stock).toBe(45);
    });
  });

  describe('deleteProduct', () => {
    it('should delete a product', async () => {
      mock.onDelete('http://localhost:8082/api/products/1').reply(204);

      const response = await inventoryApi.deleteProduct(1);
      
      expect(response.status).toBe(204);
    });

    it('should fail when product is in active orders', async () => {
      mock.onDelete('http://localhost:8082/api/products/1').reply(400, {
        message: 'Cannot delete product that is part of active orders'
      });

      await expect(inventoryApi.deleteProduct(1)).rejects.toThrow();
    });
  });
});

describe('Order API', () => {
  afterEach(() => {
    mock.reset();
  });

  describe('getAllOrders', () => {
    it('should return order list', async () => {
      const mockOrders = [
        {
          id: 1,
          orderNumber: 'ORD-2024-001',
          customerId: 1,
          status: 'PENDING',
          totalAmount: 1099.98,
          createdAt: '2024-01-15T10:30:00'
        },
        {
          id: 2,
          orderNumber: 'ORD-2024-002',
          customerId: 2,
          status: 'CONFIRMED',
          totalAmount: 549.99,
          createdAt: '2024-01-16T14:45:00'
        }
      ];

      mock.onGet('http://localhost:8083/api/orders').reply(200, mockOrders);

      const response = await orderApi.getAllOrders();
      
      expect(response.data).toEqual(mockOrders);
      expect(response.data).toHaveLength(2);
    });
  });

  describe('createOrder', () => {
    it('should create order with valid cart contents', async () => {
      const newOrder = {
        customerId: 1,
        items: [
          { productId: 1, quantity: 2, price: 999.99 },
          { productId: 2, quantity: 1, price: 29.99 }
        ],
        shippingAddress: '123 Main St',
        shippingCity: 'New York',
        shippingState: 'NY',
        shippingZip: '10001',
        subtotal: 2029.97,
        tax: 162.40,
        shipping: 0,
        totalAmount: 2192.37
      };

      const createdOrder = {
        id: 1,
        orderNumber: 'ORD-2024-003',
        status: 'PENDING',
        ...newOrder,
        createdAt: '2024-01-17T09:00:00'
      };

      mock.onPost('http://localhost:8083/api/orders').reply(201, createdOrder);

      const response = await orderApi.createOrder(newOrder);
      
      expect(response.data.id).toBe(1);
      expect(response.data.status).toBe('PENDING');
      expect(response.status).toBe(201);
    });

    it('should validate cart is not empty', async () => {
      const invalidOrder = {
        customerId: 1,
        items: [],
        totalAmount: 0
      };

      mock.onPost('http://localhost:8083/api/orders').reply(400, {
        message: 'Cart cannot be empty'
      });

      await expect(orderApi.createOrder(invalidOrder)).rejects.toThrow();
    });
  });

  describe('getOrdersByCustomer', () => {
    it('should filter orders by customer ID', async () => {
      const customerOrders = [
        {
          id: 1,
          orderNumber: 'ORD-2024-001',
          customerId: 1,
          status: 'CONFIRMED',
          totalAmount: 999.99
        },
        {
          id: 3,
          orderNumber: 'ORD-2024-003',
          customerId: 1,
          status: 'PENDING',
          totalAmount: 549.99
        }
      ];

      mock.onGet('http://localhost:8083/api/orders/customer/1').reply(200, customerOrders);

      const response = await orderApi.getOrdersByCustomer(1);
      
      expect(response.data).toEqual(customerOrders);
      expect(response.data.every(order => order.customerId === 1)).toBe(true);
    });
  });

  describe('updateOrderStatus', () => {
    it('should update order status to CONFIRMED', async () => {
      const updatedOrder = {
        id: 1,
        orderNumber: 'ORD-2024-001',
        status: 'CONFIRMED',
        totalAmount: 999.99
      };

      mock.onPut('http://localhost:8083/api/orders/1/status').reply(200, updatedOrder);

      const response = await orderApi.updateOrderStatus(1, 'CONFIRMED');
      
      expect(response.data.status).toBe('CONFIRMED');
    });

    it('should validate status transitions', async () => {
      mock.onPut('http://localhost:8083/api/orders/1/status').reply(400, {
        message: 'Invalid status transition'
      });

      await expect(orderApi.updateOrderStatus(1, 'INVALID')).rejects.toThrow();
    });
  });

  describe('cancelOrder', () => {
    it('should cancel order and refund inventory', async () => {
      mock.onPut('http://localhost:8083/api/orders/1/cancel').reply(200, {
        id: 1,
        status: 'CANCELLED',
        message: 'Order cancelled and inventory restored'
      });

      const response = await orderApi.cancelOrder(1);
      
      expect(response.data.status).toBe('CANCELLED');
    });

    it('should not cancel already shipped orders', async () => {
      mock.onPut('http://localhost:8083/api/orders/1/cancel').reply(400, {
        message: 'Cannot cancel order that has been shipped'
      });

      await expect(orderApi.cancelOrder(1)).rejects.toThrow();
    });
  });
});
