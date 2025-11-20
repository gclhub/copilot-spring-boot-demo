import React, { useState, useEffect } from 'react';
import { orderApi } from '../services/api';

function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await orderApi.getAllOrders();
      setOrders(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch orders. Make sure the order service is running on port 8083.');
      console.error('Error fetching orders:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading orders...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>{error}</p>
        <button onClick={fetchOrders}>Retry</button>
      </div>
    );
  }

  return (
    <div className="orders-container">
      <h2>Orders</h2>
      <div className="order-list">
        {orders.length === 0 ? (
          <p>No orders found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Order Number</th>
                <th>Customer ID</th>
                <th>Status</th>
                <th>Total Amount</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.orderNumber}</td>
                  <td>{order.customerId}</td>
                  <td><span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span></td>
                  <td>${order.totalAmount.toFixed(2)}</td>
                  <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default Orders;
