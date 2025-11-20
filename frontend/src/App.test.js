import { render, screen } from '@testing-library/react';
import App from './App';

// Mock the API module to avoid axios import issues
jest.mock('./services/api', () => ({
  customerApi: {
    getAllCustomers: jest.fn(),
  },
  inventoryApi: {
    getAllProducts: jest.fn(),
  },
  orderApi: {
    getAllOrders: jest.fn(),
  },
}));

test('renders e-commerce management system', () => {
  render(<App />);
  const headingElement = screen.getByText(/E-Commerce Management System/i);
  expect(headingElement).toBeInTheDocument();
});

test('renders navigation buttons', () => {
  render(<App />);
  const buttons = screen.getAllByRole('button');
  
  expect(buttons.length).toBeGreaterThanOrEqual(3);
  expect(buttons.some(btn => btn.textContent.includes('Customers'))).toBeTruthy();
  expect(buttons.some(btn => btn.textContent.includes('Products'))).toBeTruthy();
  expect(buttons.some(btn => btn.textContent.includes('Orders'))).toBeTruthy();
});
