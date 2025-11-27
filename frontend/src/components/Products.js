import React, { useState, useEffect } from 'react';
import { inventoryApi } from '../services/api';
import './Products.css';

function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    sku: '',
    description: '',
    category: '',
    price: '',
    stockQuantity: '',
    reorderLevel: '',
    active: true
  });

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await inventoryApi.getAllProducts();
      setProducts(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch products. Make sure the inventory service is running on port 8082.');
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      sku: '',
      description: '',
      category: '',
      price: '',
      stockQuantity: '',
      reorderLevel: '',
      active: true
    });
    setEditingProduct(null);
    setShowCreateForm(false);
  };

  const handleEdit = (product) => {
    setFormData({
      name: product.name,
      sku: product.sku,
      description: product.description || '',
      category: product.category,
      price: product.price,
      stockQuantity: product.stockQuantity,
      reorderLevel: product.reorderLevel || '',
      active: product.active
    });
    setEditingProduct(product);
    setShowCreateForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const productData = {
        ...formData,
        price: parseFloat(formData.price),
        stockQuantity: parseInt(formData.stockQuantity),
        reorderLevel: formData.reorderLevel ? parseInt(formData.reorderLevel) : null
      };

      if (editingProduct) {
        await inventoryApi.updateProduct(editingProduct.id, productData);
        alert('Product updated successfully!');
      } else {
        await inventoryApi.createProduct(productData);
        alert('Product created successfully!');
      }
      
      resetForm();
      fetchProducts();
    } catch (err) {
      alert('Failed to save product: ' + (err.response?.data?.message || err.message));
      console.error('Error saving product:', err);
    }
  };

  const handleDelete = async (productId) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    try {
      await inventoryApi.deleteProduct(productId);
      alert('Product deleted successfully!');
      fetchProducts();
    } catch (err) {
      alert('Failed to delete product: ' + (err.response?.data?.message || err.message));
      console.error('Error deleting product:', err);
    }
  };

  const getStockStatus = (stock, reorderLevel) => {
    if (stock === 0) return 'out-of-stock';
    if (reorderLevel && stock <= reorderLevel) return 'low-stock';
    return 'in-stock';
  };

  if (loading) {
    return <div className="loading">Loading products...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>{error}</p>
        <button onClick={fetchProducts}>Retry</button>
      </div>
    );
  }

  return (
    <div className="products-container">
      <div className="products-header">
        <h2>Inventory Management</h2>
        <button 
          className="btn-primary" 
          onClick={() => setShowCreateForm(!showCreateForm)}
        >
          {showCreateForm ? 'Cancel' : '+ Add New Product'}
        </button>
      </div>

      {showCreateForm && (
        <div className="product-form-container">
          <h3>{editingProduct ? 'Edit Product' : 'Add New Product'}</h3>
          <form onSubmit={handleSubmit} className="product-form">
            <div className="form-row">
              <div className="form-group">
                <label>Product Name *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>SKU *</label>
                <input
                  type="text"
                  value={formData.sku}
                  onChange={(e) => setFormData({...formData, sku: e.target.value})}
                  required
                  disabled={!!editingProduct}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({...formData, description: e.target.value})}
                rows="3"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Category *</label>
                <select
                  value={formData.category}
                  onChange={(e) => setFormData({...formData, category: e.target.value})}
                  required
                >
                  <option value="">Select Category</option>
                  <option value="Electronics">Electronics</option>
                  <option value="Clothing">Clothing</option>
                  <option value="Books">Books</option>
                  <option value="Home & Garden">Home & Garden</option>
                  <option value="Sports">Sports</option>
                  <option value="Toys">Toys</option>
                </select>
              </div>
              <div className="form-group">
                <label>Price *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={formData.price}
                  onChange={(e) => setFormData({...formData, price: e.target.value})}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Stock Quantity *</label>
                <input
                  type="number"
                  min="0"
                  value={formData.stockQuantity}
                  onChange={(e) => setFormData({...formData, stockQuantity: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>Reorder Level</label>
                <input
                  type="number"
                  min="0"
                  value={formData.reorderLevel}
                  onChange={(e) => setFormData({...formData, reorderLevel: e.target.value})}
                />
              </div>
            </div>

            <div className="form-group checkbox-group">
              <label>
                <input
                  type="checkbox"
                  checked={formData.active}
                  onChange={(e) => setFormData({...formData, active: e.target.checked})}
                />
                Active (available for sale)
              </label>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-submit">
                {editingProduct ? 'Update Product' : 'Create Product'}
              </button>
              <button type="button" onClick={resetForm} className="btn-cancel">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="product-list">
        <h3>Product Catalog</h3>
        {products.length === 0 ? (
          <p>No products found.</p>
        ) : (
          <div className="products-grid">
            {products.map((product) => (
              <div key={product.id} className="product-card-large">
                <div className="product-header">
                  <h4>{product.name}</h4>
                  {!product.active && <span className="badge inactive">Inactive</span>}
                </div>
                <p className="product-sku">SKU: {product.sku}</p>
                <p className="product-category">{product.category}</p>
                {product.description && (
                  <p className="product-description">{product.description}</p>
                )}
                <div className="product-pricing">
                  <span className="product-price">${product.price.toFixed(2)}</span>
                  <span className={`stock-badge ${getStockStatus(product.stockQuantity, product.reorderLevel)}`}>
                    {product.stockQuantity} in stock
                  </span>
                </div>
                {product.reorderLevel && (
                  <p className="reorder-info">Reorder at: {product.reorderLevel}</p>
                )}
                <div className="product-actions">
                  <button onClick={() => handleEdit(product)} className="btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(product.id)} className="btn-delete">
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Products;
