import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Customers from './Customers';
import { customerApi, orderApi } from '../services/api';

// Mock the API modules
jest.mock('../services/api');

const mockCustomers = [
  {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    phone: '(555) 123-4567',
    address: '123 Main St',
    city: 'New York',
    state: 'NY',
    zipCode: '10001',
    country: 'USA',
    createdAt: '2024-01-15T10:30:00'
  },
  {
    id: 2,
    firstName: 'Jane',
    lastName: 'Smith',
    email: 'jane.smith@example.com',
    phone: '(555) 987-6543',
    address: '456 Oak Ave',
    city: 'Boston',
    state: 'MA',
    zipCode: '02101',
    country: 'USA',
    createdAt: '2024-01-16T14:45:00'
  },
  {
    id: 3,
    firstName: 'Alice',
    lastName: 'Johnson',
    email: 'alice.j@example.com',
    phone: '(555) 111-2222',
    city: 'Chicago',
    state: 'IL',
    country: 'USA',
    createdAt: '2024-01-17T09:00:00'
  }
];

const mockOrders = [
  {
    id: 1,
    orderNumber: 'ORD-2024-001',
    customerId: 1,
    status: 'CONFIRMED',
    totalAmount: 999.99,
    createdAt: '2024-01-20T10:00:00'
  },
  {
    id: 2,
    orderNumber: 'ORD-2024-002',
    customerId: 1,
    status: 'PENDING',
    totalAmount: 549.99,
    createdAt: '2024-01-21T15:30:00'
  }
];

describe('Customers Component', () => {
  beforeEach(() => {
    customerApi.getAllCustomers.mockResolvedValue({ data: mockCustomers });
    orderApi.getOrdersByCustomer.mockResolvedValue({ data: mockOrders });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Initial Rendering', () => {
    it('renders customer list on load', async () => {
      render(<Customers />);

      expect(screen.getByText('Loading customers...')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByText('Customer Management')).toBeInTheDocument();
      });

      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
    });

    it('displays customer count in header', async () => {
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Customer Directory (3)')).toBeInTheDocument();
      });
    });

    it('displays customer contact information', async () => {
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText(/john\.doe@example\.com/)).toBeInTheDocument();
        expect(screen.getByText(/\(555\) 123-4567/)).toBeInTheDocument();
        expect(screen.getByText(/New York, NY/)).toBeInTheDocument();
      });
    });
  });

  describe('Add Customer Form', () => {
    it('displays add customer form when button clicked', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Customer Management')).toBeInTheDocument();
      });

      const addButton = screen.getByText('+ Add New Customer');
      await user.click(addButton);

      expect(screen.getByText('Add New Customer')).toBeInTheDocument();
      expect(screen.getByRole('textbox', { name: /First Name/ })).toBeInTheDocument();
      expect(screen.getByRole('textbox', { name: /Last Name/ })).toBeInTheDocument();
      expect(screen.getByRole('textbox', { name: /Email/ })).toBeInTheDocument();
      expect(screen.getByRole('textbox', { name: /Phone/ })).toBeInTheDocument();
    });

    it('hides form when cancel button clicked', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Customer Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Customer'));
      expect(screen.getByText('Add New Customer')).toBeInTheDocument();

      await user.click(screen.getByText('Cancel', { selector: 'button.btn-cancel' }));
      expect(screen.queryByText('Add New Customer')).not.toBeInTheDocument();
    });
  });

  describe('Create Customer', () => {
    it('creates new customer successfully with all required fields', async () => {
      const user = userEvent.setup();
      const newCustomer = {
        firstName: 'Bob',
        lastName: 'Williams',
        email: 'bob.w@example.com',
        phone: '(555) 444-5555',
        address: '789 Pine St',
        city: 'Seattle',
        state: 'WA',
        zipCode: '98101',
        country: 'USA'
      };

      const createdCustomer = { id: 4, ...newCustomer, createdAt: '2024-01-22T10:00:00' };
      customerApi.createCustomer.mockResolvedValue({ data: createdCustomer });
      customerApi.getAllCustomers.mockResolvedValueOnce({ data: mockCustomers })
        .mockResolvedValueOnce({ data: [...mockCustomers, createdCustomer] });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Customer Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Customer'));

      await user.type(screen.getByRole('textbox', { name: /First Name/ }), newCustomer.firstName);
      await user.type(screen.getByRole('textbox', { name: /Last Name/ }), newCustomer.lastName);
      await user.type(screen.getByRole('textbox', { name: /Email/ }), newCustomer.email);
      await user.type(screen.getByRole('textbox', { name: /Phone/ }), newCustomer.phone);
      await user.type(screen.getByRole('textbox', { name: /Address/ }), newCustomer.address);
      await user.type(screen.getByRole('textbox', { name: /City/ }), newCustomer.city);
      await user.type(screen.getByRole('textbox', { name: /State/ }), newCustomer.state);
      await user.type(screen.getByRole('textbox', { name: /ZIP Code/ }), newCustomer.zipCode);

      await user.click(screen.getByText('Create Customer'));

      await waitFor(() => {
        expect(customerApi.createCustomer).toHaveBeenCalledWith(newCustomer);
        expect(global.alert).toHaveBeenCalledWith('Customer created successfully!');
      });

      expect(customerApi.getAllCustomers).toHaveBeenCalledTimes(2);
    });

    it('validates required fields on form submission', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Customer Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Customer'));
      await user.click(screen.getByText('Create Customer'));

      const firstNameInput = screen.getByRole('textbox', { name: /First Name/ });
      expect(firstNameInput).toBeInvalid();
    });

    it('handles API errors during creation', async () => {
      const user = userEvent.setup();
      customerApi.createCustomer.mockRejectedValue(new Error('Network error'));

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Customer Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Customer'));
      await user.type(screen.getByRole('textbox', { name: /First Name/ }), 'Test');
      await user.type(screen.getByRole('textbox', { name: /Last Name/ }), 'User');
      await user.type(screen.getByRole('textbox', { name: /Email/ }), 'test@example.com');
      await user.type(screen.getByRole('textbox', { name: /Phone/ }), '5551234567');

      await user.click(screen.getByText('Create Customer'));

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(expect.stringContaining('Failed to save customer'));
      });
    });
  });

  describe('Edit Customer', () => {
    it('pre-fills form with customer data when edit clicked', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      const editButton = within(johnCard).getByText('Edit');
      await user.click(editButton);

      expect(screen.getByText('Edit Customer')).toBeInTheDocument();
      expect(screen.getByRole('textbox', { name: /First Name/ })).toHaveValue('John');
      expect(screen.getByRole('textbox', { name: /Last Name/ })).toHaveValue('Doe');
      expect(screen.getByRole('textbox', { name: /Email/ })).toHaveValue('john.doe@example.com');
      expect(screen.getByRole('textbox', { name: /Phone/ })).toHaveValue('(555) 123-4567');
    });

    it('disables email field when editing', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      const editButton = within(johnCard).getByText('Edit');
      await user.click(editButton);

      const emailInput = screen.getByRole('textbox', { name: /Email/ });
      expect(emailInput).toBeDisabled();
    });

    it('updates customer successfully', async () => {
      const user = userEvent.setup();
      const updatedCustomer = {
        ...mockCustomers[0],
        phone: '(555) 999-8888',
        city: 'San Francisco'
      };

      customerApi.updateCustomer.mockResolvedValue({ data: updatedCustomer });
      customerApi.getAllCustomers.mockResolvedValueOnce({ data: mockCustomers })
        .mockResolvedValueOnce({ data: [updatedCustomer, mockCustomers[1], mockCustomers[2]] });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('Edit'));

      const phoneInput = screen.getByRole('textbox', { name: /Phone/ });
      await user.clear(phoneInput);
      await user.type(phoneInput, '(555) 999-8888');

      const cityInput = screen.getByRole('textbox', { name: /City/ });
      await user.clear(cityInput);
      await user.type(cityInput, 'San Francisco');

      await user.click(screen.getByText('Update Customer'));

      await waitFor(() => {
        expect(customerApi.updateCustomer).toHaveBeenCalledWith(1, expect.objectContaining({
          phone: '(555) 999-8888',
          city: 'San Francisco'
        }));
        expect(global.alert).toHaveBeenCalledWith('Customer updated successfully!');
      });
    });
  });

  describe('Delete Customer', () => {
    it('deletes customer with confirmation', async () => {
      const user = userEvent.setup();
      global.confirm.mockReturnValue(true);
      customerApi.deleteCustomer.mockResolvedValue({ status: 204 });
      customerApi.getAllCustomers.mockResolvedValueOnce({ data: mockCustomers })
        .mockResolvedValueOnce({ data: [mockCustomers[1], mockCustomers[2]] });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('Delete'));

      await waitFor(() => {
        expect(global.confirm).toHaveBeenCalledWith('Are you sure you want to delete this customer?');
        expect(customerApi.deleteCustomer).toHaveBeenCalledWith(1);
        expect(global.alert).toHaveBeenCalledWith('Customer deleted successfully!');
      });
    });

    it('cancels deletion when user declines confirmation', async () => {
      const user = userEvent.setup();
      global.confirm.mockReturnValue(false);

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('Delete'));

      expect(global.confirm).toHaveBeenCalled();
      expect(customerApi.deleteCustomer).not.toHaveBeenCalled();
    });

    it('handles deletion errors', async () => {
      const user = userEvent.setup();
      global.confirm.mockReturnValue(true);
      customerApi.deleteCustomer.mockRejectedValue({
        response: { data: { message: 'Cannot delete customer with active orders' } }
      });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('Delete'));

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(
          expect.stringContaining('Cannot delete customer with active orders')
        );
      });
    });
  });

  describe('Search Functionality', () => {
    it('filters customers by first name', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search customers...');
      await user.type(searchInput, 'jane');

      await waitFor(() => {
        // Jane Smith is from Boston
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        // John and Alice should not appear
        const customers = screen.queryAllByText(/Doe|Alice/);
        expect(customers.filter(el => el.textContent === 'John Doe').length).toBe(0);
      });
    });

    it('filters customers by email', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search customers...');
      await user.type(searchInput, 'alice.j@example.com');

      await waitFor(() => {
        expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
      });
    });

    it('filters customers by phone number', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search customers...');
      await user.type(searchInput, '123-4567');

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument();
      });
    });

    it('filters customers by city', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search customers...');
      await user.type(searchInput, 'chicago');

      await waitFor(() => {
        expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
      });
    });

    it('displays no results message when search has no matches', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search customers...');
      await user.type(searchInput, 'nonexistent');

      await waitFor(() => {
        expect(screen.getByText('No customers found matching your search.')).toBeInTheDocument();
      });
    });
  });

  describe('Customer Details Modal', () => {
    it('displays customer details in modal when view button clicked', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('View Details'));

      await waitFor(() => {
        expect(screen.getByText('Customer Details')).toBeInTheDocument();
        expect(screen.getByText(/john\.doe@example\.com/)).toBeInTheDocument();
        expect(screen.getByText(/123 Main St/)).toBeInTheDocument();
      });
    });

    it('displays order history in customer details', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('View Details'));

      await waitFor(() => {
        expect(orderApi.getOrdersByCustomer).toHaveBeenCalledWith(1);
        expect(screen.getByText('Order History (2)')).toBeInTheDocument();
        expect(screen.getByText('ORD-2024-001')).toBeInTheDocument();
        expect(screen.getByText('ORD-2024-002')).toBeInTheDocument();
      });
    });

    it('calculates total spent correctly', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('View Details'));

      await waitFor(() => {
        // Total of mockOrders: 999.99 + 549.99 = 1549.98
        expect(screen.getByText('$1549.98')).toBeInTheDocument();
      });
    });

    it('closes modal when close button clicked', async () => {
      const user = userEvent.setup();
      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnCard = screen.getByText('John Doe').closest('.customer-card');
      await user.click(within(johnCard).getByText('View Details'));

      await waitFor(() => {
        expect(screen.getByText('Customer Details')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Close'));

      await waitFor(() => {
        expect(screen.queryByText('Customer Details')).not.toBeInTheDocument();
      });
    });

    it('displays message when customer has no orders', async () => {
      const user = userEvent.setup();
      orderApi.getOrdersByCustomer.mockResolvedValue({ data: [] });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
      });

      const aliceCard = screen.getByText('Alice Johnson').closest('.customer-card');
      await user.click(within(aliceCard).getByText('View Details'));

      await waitFor(() => {
        expect(screen.getByText('No orders found for this customer.')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error message when API call fails', async () => {
      customerApi.getAllCustomers.mockRejectedValue(new Error('Network error'));

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText(/Failed to fetch customers/)).toBeInTheDocument();
        expect(screen.getByText('Retry')).toBeInTheDocument();
      });
    });

    it('retries fetching customers when retry button clicked', async () => {
      const user = userEvent.setup();
      customerApi.getAllCustomers.mockRejectedValueOnce(new Error('Network error'))
        .mockResolvedValueOnce({ data: mockCustomers });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('Retry')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Retry'));

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(customerApi.getAllCustomers).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Loading State', () => {
    it('displays loading indicator while fetching customers', () => {
      customerApi.getAllCustomers.mockImplementation(() => new Promise(() => {}));

      render(<Customers />);

      expect(screen.getByText('Loading customers...')).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('displays message when no customers exist', async () => {
      customerApi.getAllCustomers.mockResolvedValue({ data: [] });

      render(<Customers />);

      await waitFor(() => {
        expect(screen.getByText('No customers found.')).toBeInTheDocument();
      });
    });
  });
});
