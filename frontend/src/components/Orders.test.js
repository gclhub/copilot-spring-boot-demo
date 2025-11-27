import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Orders from './Orders';
import { orderApi, customerApi, inventoryApi } from '../services/api';

jest.mock('../services/api');

const mockCustomers = [
  {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com'
  },
  {
    id: 2,
    firstName: 'Jane',
    lastName: 'Smith',
    email: 'jane.smith@example.com'
  }
];

const mockProducts = [
  {
    id: 1,
    name: 'Laptop',
    description: 'High-performance laptop',
    price: 999.99,
    category: 'Electronics',
    stockQuantity: 50,
    sku: 'LAP-001',
    active: true
  },
  {
    id: 2,
    name: 'Mouse',
    description: 'Wireless mouse',
    price: 29.99,
    category: 'Electronics',
    stockQuantity: 50,
    sku: 'MOU-001',
    active: true
  },
  {
    id: 3,
    name: 'Keyboard',
    description: 'Mechanical keyboard',
    price: 75.00,
    category: 'Electronics',
    stockQuantity: 30,
    sku: 'KEY-001',
    active: true
  }
];

const mockOrders = [
  {
    id: 1,
    orderNumber: 'ORD-2024-001',
    customerId: 1,
    customerName: 'John Doe',
    status: 'PENDING',
    items: [
      { productId: 1, productName: 'Laptop', productSku: 'LAP-001', quantity: 1, unitPrice: 999.99, subtotal: 999.99 }
    ],
    subtotal: 999.99,
    tax: 80.00,
    shipping: 0.00,
    totalAmount: 1079.99,
    shippingAddress: '123 Main St',
    shippingCity: 'New York',
    shippingState: 'NY',
    shippingZip: '10001',
    shippingCountry: 'USA',
    createdAt: '2024-01-20T10:00:00'
  },
  {
    id: 2,
    orderNumber: 'ORD-2024-002',
    customerId: 2,
    customerName: 'Jane Smith',
    status: 'CONFIRMED',
    items: [
      { productId: 2, productName: 'Mouse', productSku: 'MOU-001', quantity: 2, unitPrice: 29.99, subtotal: 59.98 }
    ],
    subtotal: 59.98,
    tax: 4.80,
    shipping: 10.00,
    totalAmount: 74.78,
    shippingAddress: '456 Oak Ave',
    shippingCity: 'Boston',
    shippingState: 'MA',
    shippingZip: '02101',
    shippingCountry: 'USA',
    createdAt: '2024-01-21T15:30:00'
  }
];

describe('Orders Component', () => {
  beforeEach(() => {
    orderApi.getAllOrders.mockResolvedValue({ data: mockOrders });
    customerApi.getAllCustomers.mockResolvedValue({ data: mockCustomers });
    inventoryApi.getAllProducts.mockResolvedValue({ data: mockProducts });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Initial Rendering', () => {
    it('renders order list on load', async () => {
      render(<Orders />);

      expect(screen.getByText('Loading orders...')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
      expect(screen.getByText('ORD-2024-002')).toBeInTheDocument();
    });

    it('displays order status badges', async () => {
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      const pendingBadge = screen.getByText('PENDING');
      const confirmedBadge = screen.getByText('CONFIRMED');

      expect(pendingBadge).toHaveClass('status-pending');
      expect(confirmedBadge).toHaveClass('status-confirmed');
    });

    it('displays order totals', async () => {
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('$1079.99')).toBeInTheDocument();
        expect(screen.getByText('$74.78')).toBeInTheDocument();
      });
    });
  });

  describe('Create Order Form', () => {
    it('displays new order form when button clicked', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));

      expect(screen.getByText('New Order')).toBeInTheDocument();
      expect(screen.getByLabelText('Select Customer *')).toBeInTheDocument();
    });

    it('loads customer dropdown options', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));

      const customerSelect = screen.getByLabelText('Select Customer *');
      expect(within(customerSelect).getByText('John Doe')).toBeInTheDocument();
      expect(within(customerSelect).getByText('Jane Smith')).toBeInTheDocument();
    });

    it('loads product catalog after selecting customer', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));

      const customerSelect = screen.getByLabelText('Select Customer *');
      await user.selectOptions(customerSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
        expect(screen.getByText('Mouse')).toBeInTheDocument();
        expect(screen.getByText('Keyboard')).toBeInTheDocument();
      });
    });
  });

  describe('Shopping Cart Operations', () => {
    it('adds product to cart', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await waitFor(() => {
        expect(screen.getByText('Shopping Cart (1)')).toBeInTheDocument();
      });

      const cartTable = screen.getByRole('table');
      expect(within(cartTable).getByText('Laptop')).toBeInTheDocument();
    });

    it('updates cart quantity', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await waitFor(() => {
        expect(screen.getByText('Shopping Cart (1)')).toBeInTheDocument();
      });

      const quantityInput = screen.getByRole('spinbutton');
      await user.clear(quantityInput);
      await user.type(quantityInput, '3');

      await waitFor(() => {
        expect(quantityInput).toHaveValue(3);
      });
    });

    it('removes product from cart', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await waitFor(() => {
        expect(screen.getByText('Shopping Cart (1)')).toBeInTheDocument();
      });

      const removeButton = screen.getByText('Remove');
      await user.click(removeButton);

      await waitFor(() => {
        expect(screen.getByText('Shopping Cart (0)')).toBeInTheDocument();
        expect(screen.getByText('Your cart is empty')).toBeInTheDocument();
      });
    });

    it('adds multiple products to cart', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      // Add Laptop
      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      // Add Mouse
      const mouseProduct = screen.getByText('Mouse').closest('.product-item');
      await user.click(within(mouseProduct).getByText('Add to Cart'));

      await waitFor(() => {
        expect(screen.getByText('Shopping Cart (2)')).toBeInTheDocument();
      });
    });
  });

  describe('Order Calculations', () => {
    it('calculates subtotal correctly', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      const mouseProduct = screen.getByText('Mouse').closest('.product-item');
      await user.click(within(mouseProduct).getByText('Add to Cart'));

      await waitFor(() => {
        // 999.99 + 29.99 = 1029.98
        expect(screen.getByText('$1029.98')).toBeInTheDocument();
      });
    });

    it('calculates tax at 8%', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await waitFor(() => {
        // 999.99 * 0.08 = 80.00
        const taxElements = screen.getAllByText(/\$80\.00/);
        expect(taxElements.length).toBeGreaterThan(0);
      });
    });

    it('applies free shipping for orders over $100', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await waitFor(() => {
        // Laptop is $999.99, should have free shipping
        const shippingElements = screen.getAllByText('Free');
        expect(shippingElements.length).toBeGreaterThan(0);
      });
    });

    it('applies $10 shipping for orders under $100', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Mouse')).toBeInTheDocument();
      });

      const mouseProduct = screen.getByText('Mouse').closest('.product-item');
      await user.click(within(mouseProduct).getByText('Add to Cart'));

      await waitFor(() => {
        // Mouse is $29.99, should have $10 shipping
        const shippingElements = screen.getAllByText(/\$10\.00/);
        expect(shippingElements.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Place Order', () => {
    it('creates order successfully with valid data', async () => {
      const user = userEvent.setup();
      const newOrder = {
        id: 3,
        orderNumber: 'ORD-2024-003',
        customerId: 1,
        status: 'PENDING',
        totalAmount: 1079.99
      };

      orderApi.createOrder.mockResolvedValue({ data: newOrder });
      orderApi.getAllOrders.mockResolvedValueOnce({ data: mockOrders })
        .mockResolvedValueOnce({ data: [...mockOrders, newOrder] });

      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      // Fill shipping info
      await user.type(screen.getByLabelText('Address *'), '123 Test St');
      await user.type(screen.getByLabelText('City *'), 'Test City');
      await user.type(screen.getByLabelText('State *'), 'TC');
      await user.type(screen.getByLabelText('ZIP Code *'), '12345');

      await user.click(screen.getByText('Place Order'));

      await waitFor(() => {
        expect(orderApi.createOrder).toHaveBeenCalledWith(expect.objectContaining({
          customerId: 1,
          items: expect.arrayContaining([
            expect.objectContaining({
              productId: 1,
              quantity: 1
            })
          ])
        }));
        expect(global.alert).toHaveBeenCalledWith('Order placed successfully!');
      });
    });

    it('validates shipping address is required', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await user.click(screen.getByText('Place Order'));

      const addressInput = screen.getByLabelText('Address *');
      expect(addressInput).toBeInvalid();
    });

    it('prevents order with empty cart', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await user.type(screen.getByLabelText('Address *'), '123 Test St');
      await user.type(screen.getByLabelText('City *'), 'Test City');
      await user.type(screen.getByLabelText('State *'), 'TC');
      await user.type(screen.getByLabelText('ZIP Code *'), '12345');

      await user.click(screen.getByText('Place Order'));

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(expect.stringContaining('add items to cart'));
      });

      expect(orderApi.createOrder).not.toHaveBeenCalled();
    });
  });

  describe('Order Status Management', () => {
    it('confirms order status changes to CONFIRMED', async () => {
      const user = userEvent.setup();
      const confirmedOrder = { ...mockOrders[0], status: 'CONFIRMED' };
      orderApi.updateOrderStatus.mockResolvedValue({ data: confirmedOrder });
      orderApi.getAllOrders.mockResolvedValueOnce({ data: mockOrders })
        .mockResolvedValueOnce({ data: [confirmedOrder, mockOrders[1]] });

      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
      });

      const orderCard = screen.getByText('ORD-2024-001').closest('.order-card');
      await user.click(within(orderCard).getByText('Confirm'));

      await waitFor(() => {
        expect(orderApi.updateOrderStatus).toHaveBeenCalledWith(1, 'CONFIRMED');
        expect(global.alert).toHaveBeenCalledWith('Order confirmed successfully!');
      });
    });

    it('cancels order and refunds inventory', async () => {
      const user = userEvent.setup();
      const cancelledOrder = { ...mockOrders[0], status: 'CANCELLED' };
      orderApi.cancelOrder.mockResolvedValue({ data: cancelledOrder });
      orderApi.getAllOrders.mockResolvedValueOnce({ data: mockOrders })
        .mockResolvedValueOnce({ data: [cancelledOrder, mockOrders[1]] });

      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
      });

      const orderCard = screen.getByText('ORD-2024-001').closest('.order-card');
      await user.click(within(orderCard).getByText('Cancel'));

      await waitFor(() => {
        expect(orderApi.cancelOrder).toHaveBeenCalledWith(1);
        expect(global.alert).toHaveBeenCalledWith('Order cancelled successfully!');
      });
    });
  });

  describe('Order Details Modal', () => {
    it('displays order details in modal', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
      });

      const orderCard = screen.getByText('ORD-2024-001').closest('.order-card');
      await user.click(within(orderCard).getByText('View Details'));

      await waitFor(() => {
        expect(screen.getByText('Order Details')).toBeInTheDocument();
        expect(screen.getByText('Customer: John Doe')).toBeInTheDocument();
      });
    });

    it('shows order items in detail view', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
      });

      const orderCard = screen.getByText('ORD-2024-001').closest('.order-card');
      await user.click(within(orderCard).getByText('View Details'));

      await waitFor(() => {
        const modal = screen.getByText('Order Details').closest('.modal-content');
        expect(within(modal).getByText('Laptop')).toBeInTheDocument();
      });
    });

    it('closes modal when close button clicked', async () => {
      const user = userEvent.setup();
      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
      });

      const orderCard = screen.getByText('ORD-2024-001').closest('.order-card');
      await user.click(within(orderCard).getByText('View Details'));

      await waitFor(() => {
        expect(screen.getByText('Order Details')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Close'));

      await waitFor(() => {
        expect(screen.queryByText('Order Details')).not.toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error message when API call fails', async () => {
      orderApi.getAllOrders.mockRejectedValue(new Error('Network error'));

      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText(/Failed to fetch data/)).toBeInTheDocument();
        expect(screen.getByText('Retry')).toBeInTheDocument();
      });
    });

    it('handles order creation errors', async () => {
      const user = userEvent.setup();
      orderApi.createOrder.mockRejectedValue({
        response: { data: { message: 'Insufficient inventory' } }
      });

      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('Order Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Create New Order'));
      await user.selectOptions(screen.getByLabelText('Select Customer *'), '1');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopProduct = screen.getByText('Laptop').closest('.product-item');
      await user.click(within(laptopProduct).getByText('Add to Cart'));

      await user.type(screen.getByLabelText('Address *'), '123 Test St');
      await user.type(screen.getByLabelText('City *'), 'Test City');
      await user.type(screen.getByLabelText('State *'), 'TC');
      await user.type(screen.getByLabelText('ZIP Code *'), '12345');

      await user.click(screen.getByText('Place Order'));

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(expect.stringContaining('Insufficient inventory'));
      });
    });
  });

  describe('Loading State', () => {
    it('displays loading indicator while fetching data', () => {
      orderApi.getAllOrders.mockImplementation(() => new Promise(() => {}));
      customerApi.getAllCustomers.mockImplementation(() => new Promise(() => {}));
      inventoryApi.getAllProducts.mockImplementation(() => new Promise(() => {}));

      render(<Orders />);

      expect(screen.getByText('Loading orders...')).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('displays message when no orders exist', async () => {
      orderApi.getAllOrders.mockResolvedValue({ data: [] });

      render(<Orders />);

      await waitFor(() => {
        expect(screen.getByText('No orders found.')).toBeInTheDocument();
      });
    });
  });
});
