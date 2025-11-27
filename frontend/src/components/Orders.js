import React, { useState, useEffect } from 'react';
import { orderApi, customerApi, inventoryApi } from '../services/api';
import './Orders.css';

function Orders() {
  const [orders, setOrders] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);
  
  // Form state
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [cart, setCart] = useState([]);
  const [shippingInfo, setShippingInfo] = useState({
    address: '',
    city: '',
    state: '',
    zip: '',
    country: 'USA'
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [ordersRes, customersRes, productsRes] = await Promise.all([
        orderApi.getAllOrders(),
        customerApi.getAllCustomers(),
        inventoryApi.getAllProducts()
      ]);
      setOrders(ordersRes.data);
      setCustomers(customersRes.data);
      setProducts(productsRes.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch data. Make sure all services are running.');
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = (product) => {
    const existingItem = cart.find(item => item.productId === product.id);
    if (existingItem) {
      setCart(cart.map(item =>
        item.productId === product.id
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setCart([...cart, {
        productId: product.id,
        productName: product.name,
        productSku: product.sku,
        quantity: 1,
        unitPrice: product.price
      }]);
    }
  };

  const removeFromCart = (productId) => {
    setCart(cart.filter(item => item.productId !== productId));
  };

  const updateQuantity = (productId, newQuantity) => {
    if (newQuantity < 1) {
      removeFromCart(productId);
      return;
    }
    setCart(cart.map(item =>
      item.productId === productId
        ? { ...item, quantity: newQuantity }
        : item
    ));
  };

  const calculateTotal = () => {
    return cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
  };

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    if (!selectedCustomerId || cart.length === 0) {
      alert('Please select a customer and add items to cart');
      return;
    }

    try {
      const orderData = {
        customerId: parseInt(selectedCustomerId),
        items: cart,
        shippingAddress: shippingInfo.address,
        shippingCity: shippingInfo.city,
        shippingState: shippingInfo.state,
        shippingZip: shippingInfo.zip,
        shippingCountry: shippingInfo.country
      };

      await orderApi.createOrder(orderData);
      alert('Order created successfully!');
      
      // Reset form
      setShowCreateForm(false);
      setSelectedCustomerId('');
      setCart([]);
      setShippingInfo({
        address: '',
        city: '',
        state: '',
        zip: '',
        country: 'USA'
      });
      
      fetchData();
    } catch (err) {
      alert('Failed to create order: ' + (err.response?.data?.message || err.message));
      console.error('Error creating order:', err);
    }
  };

  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      await orderApi.updateOrderStatus(orderId, newStatus);
      alert('Order status updated successfully!');
      fetchData();
    } catch (err) {
      alert('Failed to update order status: ' + (err.response?.data?.message || err.message));
      console.error('Error updating order:', err);
    }
  };

  const viewOrderDetails = (order) => {
    setSelectedOrder(order);
  };

  if (loading) {
    return <div className="loading">Loading orders...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>{error}</p>
        <button onClick={fetchData}>Retry</button>
      </div>
    );
  }

  return (
    <div className="orders-container">
      <div className="orders-header">
        <h2>Order Management</h2>
        <button 
          className="btn-primary" 
          onClick={() => setShowCreateForm(!showCreateForm)}
        >
          {showCreateForm ? 'Cancel' : '+ Create New Order'}
        </button>
      </div>

      {showCreateForm && (
        <div className="order-form-container">
          <h3>Create New Order</h3>
          <form onSubmit={handleCreateOrder} className="order-form">
            <div className="form-section">
              <h4>1. Select Customer</h4>
              <select
                value={selectedCustomerId}
                onChange={(e) => setSelectedCustomerId(e.target.value)}
                required
              >
                <option value="">-- Select Customer --</option>
                {customers.map(customer => (
                  <option key={customer.id} value={customer.id}>
                    {customer.firstName} {customer.lastName} ({customer.email})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-section">
              <h4>2. Add Products</h4>
              <div className="products-grid">
                {products.filter(p => p.stockQuantity > 0).map(product => (
                  <div key={product.id} className="product-card">
                    <h5>{product.name}</h5>
                    <p className="product-sku">{product.sku}</p>
                    <p className="product-price">${product.price.toFixed(2)}</p>
                    <p className="product-stock">Stock: {product.stockQuantity}</p>
                    <button
                      type="button"
                      onClick={() => addToCart(product)}
                      className="btn-add"
                    >
                      Add to Cart
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {cart.length > 0 && (
              <div className="form-section">
                <h4>3. Shopping Cart</h4>
                <table className="cart-table">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>Price</th>
                      <th>Quantity</th>
                      <th>Subtotal</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cart.map(item => (
                      <tr key={item.productId}>
                        <td>{item.productName}</td>
                        <td>${item.unitPrice.toFixed(2)}</td>
                        <td>
                          <input
                            type="number"
                            min="1"
                            value={item.quantity}
                            onChange={(e) => updateQuantity(item.productId, parseInt(e.target.value))}
                            className="quantity-input"
                          />
                        </td>
                        <td>${(item.unitPrice * item.quantity).toFixed(2)}</td>
                        <td>
                          <button
                            type="button"
                            onClick={() => removeFromCart(item.productId)}
                            className="btn-remove"
                          >
                            Remove
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr>
                      <td colSpan="3"><strong>Total:</strong></td>
                      <td><strong>${calculateTotal().toFixed(2)}</strong></td>
                      <td></td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            )}

            <div className="form-section">
              <h4>4. Shipping Information</h4>
              <div className="shipping-form">
                <input
                  type="text"
                  placeholder="Address"
                  value={shippingInfo.address}
                  onChange={(e) => setShippingInfo({...shippingInfo, address: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="City"
                  value={shippingInfo.city}
                  onChange={(e) => setShippingInfo({...shippingInfo, city: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="State"
                  value={shippingInfo.state}
                  onChange={(e) => setShippingInfo({...shippingInfo, state: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="ZIP Code"
                  value={shippingInfo.zip}
                  onChange={(e) => setShippingInfo({...shippingInfo, zip: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="Country"
                  value={shippingInfo.country}
                  onChange={(e) => setShippingInfo({...shippingInfo, country: e.target.value})}
                  required
                />
              </div>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-submit">Place Order</button>
              <button type="button" onClick={() => setShowCreateForm(false)} className="btn-cancel">Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="order-list">
        <h3>All Orders</h3>
        {orders.length === 0 ? (
          <p>No orders found.</p>
        ) : (
          <table className="orders-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Order Number</th>
                <th>Customer ID</th>
                <th>Status</th>
                <th>Total Amount</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.orderNumber}</td>
                  <td>{order.customerId}</td>
                  <td>
                    <span className={`status status-${order.status.toLowerCase()}`}>
                      {order.status}
                    </span>
                  </td>
                  <td>${order.totalAmount.toFixed(2)}</td>
                  <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                  <td className="action-buttons">
                    <button onClick={() => viewOrderDetails(order)} className="btn-view">View</button>
                    {order.status === 'PENDING' && (
                      <button onClick={() => handleUpdateStatus(order.id, 'CONFIRMED')} className="btn-confirm">Confirm</button>
                    )}
                    {order.status === 'CONFIRMED' && (
                      <button onClick={() => handleUpdateStatus(order.id, 'CANCELLED')} className="btn-cancel-order">Cancel</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {selectedOrder && (
        <div className="modal-overlay" onClick={() => setSelectedOrder(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Order Details</h3>
            <div className="order-details">
              <p><strong>Order Number:</strong> {selectedOrder.orderNumber}</p>
              <p><strong>Customer ID:</strong> {selectedOrder.customerId}</p>
              <p><strong>Status:</strong> {selectedOrder.status}</p>
              <p><strong>Total Amount:</strong> ${selectedOrder.totalAmount.toFixed(2)}</p>
              <p><strong>Shipping Address:</strong> {selectedOrder.shippingAddress}, {selectedOrder.shippingCity}, {selectedOrder.shippingState} {selectedOrder.shippingZip}</p>
              <p><strong>Created:</strong> {new Date(selectedOrder.createdAt).toLocaleString()}</p>
              
              {selectedOrder.items && selectedOrder.items.length > 0 && (
                <>
                  <h4>Order Items:</h4>
                  <table className="items-table">
                    <thead>
                      <tr>
                        <th>Product</th>
                        <th>SKU</th>
                        <th>Quantity</th>
                        <th>Unit Price</th>
                        <th>Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedOrder.items.map((item, index) => (
                        <tr key={index}>
                          <td>{item.productName}</td>
                          <td>{item.productSku}</td>
                          <td>{item.quantity}</td>
                          <td>${item.unitPrice.toFixed(2)}</td>
                          <td>${item.subtotal.toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </>
              )}
            </div>
            <button onClick={() => setSelectedOrder(null)} className="btn-close">Close</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Orders;
