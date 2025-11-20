import axios from 'axios';

// Base URLs for microservices
const CUSTOMER_SERVICE_URL = process.env.REACT_APP_CUSTOMER_SERVICE_URL || 'http://localhost:8081/api';
const INVENTORY_SERVICE_URL = process.env.REACT_APP_INVENTORY_SERVICE_URL || 'http://localhost:8082/api';
const ORDER_SERVICE_URL = process.env.REACT_APP_ORDER_SERVICE_URL || 'http://localhost:8083/api';

// Customer Service API
export const customerApi = {
  getAllCustomers: () => axios.get(`${CUSTOMER_SERVICE_URL}/customers`),
  getCustomerById: (id) => axios.get(`${CUSTOMER_SERVICE_URL}/customers/${id}`),
  createCustomer: (customer) => axios.post(`${CUSTOMER_SERVICE_URL}/customers`, customer),
  updateCustomer: (id, customer) => axios.put(`${CUSTOMER_SERVICE_URL}/customers/${id}`, customer),
  deleteCustomer: (id) => axios.delete(`${CUSTOMER_SERVICE_URL}/customers/${id}`)
};

// Inventory Service API
export const inventoryApi = {
  getAllProducts: () => axios.get(`${INVENTORY_SERVICE_URL}/products`),
  getProductById: (id) => axios.get(`${INVENTORY_SERVICE_URL}/products/${id}`),
  getProductsByCategory: (category) => axios.get(`${INVENTORY_SERVICE_URL}/products/category/${category}`),
  createProduct: (product) => axios.post(`${INVENTORY_SERVICE_URL}/products`, product),
  updateProduct: (id, product) => axios.put(`${INVENTORY_SERVICE_URL}/products/${id}`, product),
  deleteProduct: (id) => axios.delete(`${INVENTORY_SERVICE_URL}/products/${id}`)
};

// Order Service API
export const orderApi = {
  getAllOrders: () => axios.get(`${ORDER_SERVICE_URL}/orders`),
  getOrderById: (id) => axios.get(`${ORDER_SERVICE_URL}/orders/${id}`),
  getOrdersByCustomer: (customerId) => axios.get(`${ORDER_SERVICE_URL}/orders/customer/${customerId}`),
  createOrder: (order) => axios.post(`${ORDER_SERVICE_URL}/orders`, order),
  updateOrderStatus: (id, status) => axios.patch(`${ORDER_SERVICE_URL}/orders/${id}/status?status=${status}`),
  deleteOrder: (id) => axios.delete(`${ORDER_SERVICE_URL}/orders/${id}`)
};
