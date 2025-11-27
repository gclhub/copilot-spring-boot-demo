import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Products from './Products';
import { inventoryApi } from '../services/api';

jest.mock('../services/api');

// Helper to find input by label text (since labels don't have htmlFor)
const getInputByLabel = (container, labelText) => {
  const labels = Array.from(container.querySelectorAll('label'));
  const label = labels.find(l => l.textContent.includes(labelText));
  if (!label) throw new Error(`Label with text "${labelText}" not found`);
  const formGroup = label.closest('.form-group');
  return formGroup.querySelector('input, textarea, select');
};

const mockProducts = [
  {
    id: 1,
    name: 'Laptop',
    description: 'High-performance laptop',
    price: 999.99,
    category: 'Electronics',
    stockQuantity: 50,
    reorderLevel: 10,
    sku: 'LAP-001',
    active: true
  },
  {
    id: 2,
    name: 'Mouse',
    description: 'Wireless mouse',
    price: 29.99,
    category: 'Electronics',
    stockQuantity: 5,
    reorderLevel: 10,
    sku: 'MOU-001',
    active: true
  },
  {
    id: 3,
    name: 'Keyboard',
    description: 'Mechanical keyboard',
    price: 149.99,
    category: 'Electronics',
    stockQuantity: 0,
    reorderLevel: 10,
    sku: 'KEY-001',
    active: true
  },
  {
    id: 4,
    name: 'Monitor',
    description: '27-inch 4K monitor',
    price: 499.99,
    category: 'Electronics',
    stockQuantity: 25,
    reorderLevel: 10,
    sku: 'MON-001',
    active: true
  }
];

describe('Products Component', () => {
  beforeEach(() => {
    inventoryApi.getAllProducts.mockResolvedValue({ data: mockProducts });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Initial Rendering', () => {
    it('renders product grid on load', async () => {
      render(<Products />);

      expect(screen.getByText('Loading products...')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      expect(screen.getByText('Laptop')).toBeInTheDocument();
      expect(screen.getByText('Mouse')).toBeInTheDocument();
      expect(screen.getByText('Keyboard')).toBeInTheDocument();
      expect(screen.getByText('Monitor')).toBeInTheDocument();
    });

    it('displays product count', async () => {
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Product Catalog')).toBeInTheDocument();
      });
    });

    it('displays product prices', async () => {
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('$999.99')).toBeInTheDocument();
        expect(screen.getByText('$29.99')).toBeInTheDocument();
        expect(screen.getByText('$149.99')).toBeInTheDocument();
        expect(screen.getByText('$499.99')).toBeInTheDocument();
      });
    });
  });

  describe('Stock Status Badges', () => {
    it('displays green in-stock badge for adequate inventory', async () => {
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopCard = screen.getByText('Laptop').closest('.product-card-large');
      const badge = within(laptopCard).getByText(/50 in stock/);
      
      expect(badge).toHaveClass('stock-badge');
      expect(badge).toHaveClass('in-stock');
    });

    it('displays yellow low-stock badge for inventory below 10', async () => {
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Mouse')).toBeInTheDocument();
      });

      const mouseCard = screen.getByText('Mouse').closest('.product-card-large');
      const badge = within(mouseCard).getByText(/5 in stock/);
      
      expect(badge).toHaveClass('stock-badge');
      expect(badge).toHaveClass('low-stock');
    });

    it('displays red out-of-stock badge for zero inventory', async () => {
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Keyboard')).toBeInTheDocument();
      });

      const keyboardCard = screen.getByText('Keyboard').closest('.product-card-large');
      const badge = within(keyboardCard).getByText(/0 in stock/);
      
      expect(badge).toHaveClass('stock-badge');
      expect(badge).toHaveClass('out-of-stock');
    });
  });

  describe('Add Product Form', () => {
    it('displays add product form when button clicked', async () => {
      const user = userEvent.setup();
      const { container } = render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Product'));

      expect(screen.getByText('Add New Product')).toBeInTheDocument();
      expect(getInputByLabel(container, 'Product Name')).toBeInTheDocument();
      expect(getInputByLabel(container, 'Price')).toBeInTheDocument();
      expect(getInputByLabel(container, 'Stock Quantity')).toBeInTheDocument();
      expect(getInputByLabel(container, 'Category')).toBeInTheDocument();
    });

    it('adds new product with all fields', async () => {
      const user = userEvent.setup();
      const newProduct = {
        name: 'Webcam',
        description: 'HD webcam',
        price: 79.99,
        category: 'Electronics',
        stock: 15,
        sku: 'WEB-001'
      };

      const createdProduct = { id: 5, ...newProduct };
      inventoryApi.createProduct.mockResolvedValue({ data: createdProduct });
      inventoryApi.getAllProducts.mockResolvedValueOnce({ data: mockProducts })
        .mockResolvedValueOnce({ data: [...mockProducts, createdProduct] });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Product'));

      await user.type(screen.getByRole('textbox', { name: /Product Name/ }), newProduct.name);
      await user.type(screen.getByRole('textbox', { name: /Description/ }), newProduct.description);
      await user.type(screen.getByRole('spinbutton', { name: /Price/ }), newProduct.price.toString());
      await user.type(screen.getByRole('spinbutton', { name: /Stock Quantity/ }), newProduct.stock.toString());
      await user.selectOptions(screen.getByRole('combobox', { name: /Category/ }), newProduct.category);
      await user.type(screen.getByRole('textbox', { name: /SKU/ }), newProduct.sku);

      await user.click(screen.getByText('Create Product'));

      await waitFor(() => {
        expect(inventoryApi.createProduct).toHaveBeenCalledWith(expect.objectContaining({
          name: newProduct.name,
          price: newProduct.price,
          stockQuantity: newProduct.stock
        }));
        expect(global.alert).toHaveBeenCalledWith('Product created successfully!');
      });
    });

    it('validates price is positive number', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Product'));

      const priceInput = screen.getByRole('spinbutton', { name: /Price/ });
      await user.type(priceInput, '-10');

      expect(priceInput).toBeInvalid();
    });

    it('validates stock quantity is non-negative', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Product'));

      const stockInput = screen.getByRole('spinbutton', { name: /Stock Quantity/ });
      await user.type(stockInput, '-5');

      expect(stockInput).toBeInvalid();
    });

    it('validates required fields', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Product'));
      await user.click(screen.getByText('Create Product'));

      const nameInput = screen.getByRole('textbox', { name: /Product Name/ });
      expect(nameInput).toBeInvalid();
    });

    it('handles API errors during creation', async () => {
      const user = userEvent.setup();
      inventoryApi.createProduct.mockRejectedValue({
        response: { data: { message: 'Duplicate SKU' } }
      });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Inventory Management')).toBeInTheDocument();
      });

      await user.click(screen.getByText('+ Add New Product'));
      await user.type(screen.getByRole('textbox', { name: /Product Name/ }), 'Test Product');
      await user.type(screen.getByRole('spinbutton', { name: /Price/ }), '99.99');
      await user.type(screen.getByRole('spinbutton', { name: /Stock Quantity/ }), '10');
      await user.selectOptions(screen.getByRole('combobox', { name: /Category/ }), 'Electronics');
      await user.type(screen.getByRole('textbox', { name: /SKU/ }), 'LAP-001');

      await user.click(screen.getByText('Create Product'));

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(expect.stringContaining('Failed to save product'));
      });
    });
  });

  describe('Edit Product', () => {
    it('pre-fills form with product data when edit clicked', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopCard = screen.getByText('Laptop').closest('.product-card');
      await user.click(within(laptopCard).getByText('Edit'));

      expect(screen.getByText('Edit Product')).toBeInTheDocument();
      expect(screen.getByRole('textbox', { name: /Product Name/ })).toHaveValue('Laptop');
      expect(screen.getByRole('spinbutton', { name: /Price/ })).toHaveValue(999.99);
      expect(screen.getByRole('spinbutton', { name: /Stock Quantity/ })).toHaveValue(50);
    });

    it('updates product and refreshes display', async () => {
      const user = userEvent.setup();
      const updatedProduct = {
        ...mockProducts[0],
        price: 899.99,
        stock: 45
      };

      inventoryApi.updateProduct.mockResolvedValue({ data: updatedProduct });
      inventoryApi.getAllProducts.mockResolvedValueOnce({ data: mockProducts })
        .mockResolvedValueOnce({ data: [updatedProduct, ...mockProducts.slice(1)] });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopCard = screen.getByText('Laptop').closest('.product-card');
      await user.click(within(laptopCard).getByText('Edit'));

      const priceInput = screen.getByRole('spinbutton', { name: /Price/ });
      await user.clear(priceInput);
      await user.type(priceInput, '899.99');

      const stockInput = screen.getByRole('spinbutton', { name: /Stock Quantity/ });
      await user.clear(stockInput);
      await user.type(stockInput, '45');

      await user.click(screen.getByText('Update Product'));

      await waitFor(() => {
        expect(inventoryApi.updateProduct).toHaveBeenCalledWith(1, expect.objectContaining({
          price: 899.99,
          stock: 45
        }));
        expect(global.alert).toHaveBeenCalledWith('Product updated successfully!');
      });
    });

    it('stock badge updates after stock quantity change', async () => {
      const user = userEvent.setup();
      const updatedProduct = {
        ...mockProducts[0],
        stock: 3
      };

      inventoryApi.updateProduct.mockResolvedValue({ data: updatedProduct });
      inventoryApi.getAllProducts.mockResolvedValueOnce({ data: mockProducts })
        .mockResolvedValueOnce({ data: [updatedProduct, ...mockProducts.slice(1)] });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      // Initially should have in-stock badge
      let laptopCard = screen.getByText('Laptop').closest('.product-card');
      expect(within(laptopCard).getByText('In Stock')).toBeInTheDocument();

      // Edit to low stock
      await user.click(within(laptopCard).getByText('Edit'));

      const stockInput = screen.getByRole('spinbutton', { name: /Stock Quantity/ });
      await user.clear(stockInput);
      await user.type(stockInput, '3');

      await user.click(screen.getByText('Update Product'));

      // Wait for update and check new badge
      await waitFor(() => {
        laptopCard = screen.getByText('Laptop').closest('.product-card');
        expect(within(laptopCard).getByText('Low Stock')).toBeInTheDocument();
      });
    });
  });

  describe('Delete Product', () => {
    it('deletes product with confirmation', async () => {
      const user = userEvent.setup();
      global.confirm.mockReturnValue(true);
      inventoryApi.deleteProduct.mockResolvedValue({ status: 204 });
      inventoryApi.getAllProducts.mockResolvedValueOnce({ data: mockProducts })
        .mockResolvedValueOnce({ data: mockProducts.slice(1) });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopCard = screen.getByText('Laptop').closest('.product-card');
      await user.click(within(laptopCard).getByText('Delete'));

      await waitFor(() => {
        expect(global.confirm).toHaveBeenCalledWith('Are you sure you want to delete this product?');
        expect(inventoryApi.deleteProduct).toHaveBeenCalledWith(1);
        expect(global.alert).toHaveBeenCalledWith('Product deleted successfully!');
      });
    });

    it('cancels deletion when user declines', async () => {
      const user = userEvent.setup();
      global.confirm.mockReturnValue(false);

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopCard = screen.getByText('Laptop').closest('.product-card');
      await user.click(within(laptopCard).getByText('Delete'));

      expect(global.confirm).toHaveBeenCalled();
      expect(inventoryApi.deleteProduct).not.toHaveBeenCalled();
    });

    it('handles error when product is in active orders', async () => {
      const user = userEvent.setup();
      global.confirm.mockReturnValue(true);
      inventoryApi.deleteProduct.mockRejectedValue({
        response: { data: { message: 'Cannot delete product in active orders' } }
      });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const laptopCard = screen.getByText('Laptop').closest('.product-card');
      await user.click(within(laptopCard).getByText('Delete'));

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(
          expect.stringContaining('Cannot delete product in active orders')
        );
      });
    });
  });

  describe('Category Filter', () => {
    it('filters products by category', async () => {
      const user = userEvent.setup();
      const mixedProducts = [
        ...mockProducts,
        {
          id: 5,
          name: 'Office Chair',
          price: 299.99,
          category: 'Furniture',
          stock: 10,
          sku: 'CHA-001'
        }
      ];

      inventoryApi.getAllProducts.mockResolvedValue({ data: mixedProducts });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
        expect(screen.getByText('Office Chair')).toBeInTheDocument();
      });

      const categorySelect = screen.getByLabelText('Filter by Category');
      await user.selectOptions(categorySelect, 'Furniture');

      await waitFor(() => {
        expect(screen.getByText('Office Chair')).toBeInTheDocument();
        expect(screen.queryByText('Laptop')).not.toBeInTheDocument();
      });
    });

    it('shows all products when "All Categories" selected', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const categorySelect = screen.getByLabelText('Filter by Category');
      await user.selectOptions(categorySelect, 'Electronics');
      await user.selectOptions(categorySelect, '');

      expect(screen.getByText('Laptop')).toBeInTheDocument();
      expect(screen.getByText('Mouse')).toBeInTheDocument();
    });
  });

  describe('Search Functionality', () => {
    it('searches products by name', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search products...');
      await user.type(searchInput, 'laptop');

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
        expect(screen.queryByText('Mouse')).not.toBeInTheDocument();
      });
    });

    it('searches products by SKU', async () => {
      const user = userEvent.setup();
      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText('Search products...');
      await user.type(searchInput, 'MOU-001');

      await waitFor(() => {
        expect(screen.getByText('Mouse')).toBeInTheDocument();
        expect(screen.queryByText('Laptop')).not.toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error message when API call fails', async () => {
      inventoryApi.getAllProducts.mockRejectedValue(new Error('Network error'));

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText(/Failed to fetch products/)).toBeInTheDocument();
        expect(screen.getByText('Retry')).toBeInTheDocument();
      });
    });

    it('retries fetching products when retry button clicked', async () => {
      const user = userEvent.setup();
      inventoryApi.getAllProducts.mockRejectedValueOnce(new Error('Network error'))
        .mockResolvedValueOnce({ data: mockProducts });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('Retry')).toBeInTheDocument();
      });

      await user.click(screen.getByText('Retry'));

      await waitFor(() => {
        expect(screen.getByText('Laptop')).toBeInTheDocument();
        expect(inventoryApi.getAllProducts).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Empty State', () => {
    it('displays message when no products exist', async () => {
      inventoryApi.getAllProducts.mockResolvedValue({ data: [] });

      render(<Products />);

      await waitFor(() => {
        expect(screen.getByText('No products found.')).toBeInTheDocument();
      });
    });
  });

  describe('Loading State', () => {
    it('displays loading indicator while fetching products', () => {
      inventoryApi.getAllProducts.mockImplementation(() => new Promise(() => {}));

      render(<Products />);

      expect(screen.getByText('Loading products...')).toBeInTheDocument();
    });
  });
});
