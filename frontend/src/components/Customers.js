import React, { useState, useEffect } from 'react';
import { customerApi, orderApi } from '../services/api';
import './Customers.css';

function Customers() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState(null);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [customerOrders, setCustomerOrders] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'USA'
  });

  useEffect(() => {
    fetchCustomers();
  }, []);

  const fetchCustomers = async () => {
    try {
      setLoading(true);
      const response = await customerApi.getAllCustomers();
      setCustomers(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch customers. Make sure the customer service is running on port 8081.');
      console.error('Error fetching customers:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomerOrders = async (customerId) => {
    try {
      const response = await orderApi.getOrdersByCustomer(customerId);
      setCustomerOrders(response.data);
    } catch (err) {
      console.error('Error fetching customer orders:', err);
      setCustomerOrders([]);
    }
  };

  const resetForm = () => {
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      state: '',
      zipCode: '',
      country: 'USA'
    });
    setEditingCustomer(null);
    setShowCreateForm(false);
  };

  const handleEdit = (customer) => {
    setFormData({
      firstName: customer.firstName,
      lastName: customer.lastName,
      email: customer.email,
      phone: customer.phone,
      address: customer.address || '',
      city: customer.city || '',
      state: customer.state || '',
      zipCode: customer.zipCode || '',
      country: customer.country || 'USA'
    });
    setEditingCustomer(customer);
    setShowCreateForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      if (editingCustomer) {
        await customerApi.updateCustomer(editingCustomer.id, formData);
        alert('Customer updated successfully!');
      } else {
        await customerApi.createCustomer(formData);
        alert('Customer created successfully!');
      }
      
      resetForm();
      fetchCustomers();
    } catch (err) {
      alert('Failed to save customer: ' + (err.response?.data?.message || err.message));
      console.error('Error saving customer:', err);
    }
  };

  const handleDelete = async (customerId) => {
    if (!window.confirm('Are you sure you want to delete this customer?')) {
      return;
    }

    try {
      await customerApi.deleteCustomer(customerId);
      alert('Customer deleted successfully!');
      fetchCustomers();
    } catch (err) {
      alert('Failed to delete customer: ' + (err.response?.data?.message || err.message));
      console.error('Error deleting customer:', err);
    }
  };

  const viewCustomerDetails = async (customer) => {
    setSelectedCustomer(customer);
    await fetchCustomerOrders(customer.id);
  };

  const filteredCustomers = customers.filter(customer => {
    const searchLower = searchTerm.toLowerCase();
    return (
      customer.firstName.toLowerCase().includes(searchLower) ||
      customer.lastName.toLowerCase().includes(searchLower) ||
      customer.email.toLowerCase().includes(searchLower) ||
      customer.phone.includes(searchTerm) ||
      (customer.city && customer.city.toLowerCase().includes(searchLower))
    );
  });

  if (loading) {
    return <div className="loading">Loading customers...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>{error}</p>
        <button onClick={fetchCustomers}>Retry</button>
      </div>
    );
  }

  return (
    <div className="customers-container">
      <div className="customers-header">
        <h2>Customer Management</h2>
        <button 
          className="btn-primary" 
          onClick={() => setShowCreateForm(!showCreateForm)}
        >
          {showCreateForm ? 'Cancel' : '+ Add New Customer'}
        </button>
      </div>

      {showCreateForm && (
        <div className="customer-form-container">
          <h3>{editingCustomer ? 'Edit Customer' : 'Add New Customer'}</h3>
          <form onSubmit={handleSubmit} className="customer-form">
            <div className="form-row">
              <div className="form-group">
                <label>First Name *</label>
                <input
                  type="text"
                  value={formData.firstName}
                  onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>Last Name *</label>
                <input
                  type="text"
                  value={formData.lastName}
                  onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                  required
                  disabled={!!editingCustomer}
                />
              </div>
              <div className="form-group">
                <label>Phone *</label>
                <input
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => setFormData({...formData, phone: e.target.value})}
                  required
                  placeholder="(555) 123-4567"
                />
              </div>
            </div>

            <div className="form-group">
              <label>Address</label>
              <input
                type="text"
                value={formData.address}
                onChange={(e) => setFormData({...formData, address: e.target.value})}
                placeholder="123 Main Street"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>City</label>
                <input
                  type="text"
                  value={formData.city}
                  onChange={(e) => setFormData({...formData, city: e.target.value})}
                />
              </div>
              <div className="form-group">
                <label>State</label>
                <input
                  type="text"
                  value={formData.state}
                  onChange={(e) => setFormData({...formData, state: e.target.value})}
                  placeholder="NY"
                  maxLength="2"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>ZIP Code</label>
                <input
                  type="text"
                  value={formData.zipCode}
                  onChange={(e) => setFormData({...formData, zipCode: e.target.value})}
                  placeholder="10001"
                />
              </div>
              <div className="form-group">
                <label>Country</label>
                <input
                  type="text"
                  value={formData.country}
                  onChange={(e) => setFormData({...formData, country: e.target.value})}
                />
              </div>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-submit">
                {editingCustomer ? 'Update Customer' : 'Create Customer'}
              </button>
              <button type="button" onClick={resetForm} className="btn-cancel">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="customer-list">
        <div className="list-header">
          <h3>Customer Directory ({filteredCustomers.length})</h3>
          <div className="search-box">
            <input
              type="text"
              placeholder="Search customers..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
        </div>

        {filteredCustomers.length === 0 ? (
          <p className="no-results">
            {searchTerm ? 'No customers found matching your search.' : 'No customers found.'}
          </p>
        ) : (
          <div className="customers-grid">
            {filteredCustomers.map((customer) => (
              <div key={customer.id} className="customer-card">
                <div className="customer-card-header">
                  <div className="customer-avatar">
                    {customer.firstName.charAt(0)}{customer.lastName.charAt(0)}
                  </div>
                  <div className="customer-info">
                    <h4>{customer.firstName} {customer.lastName}</h4>
                    <p className="customer-id">ID: {customer.id}</p>
                  </div>
                </div>
                
                <div className="customer-details">
                  <div className="detail-row">
                    <span className="detail-icon">📧</span>
                    <span className="detail-text">{customer.email}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-icon">📱</span>
                    <span className="detail-text">{customer.phone}</span>
                  </div>
                  {customer.city && (
                    <div className="detail-row">
                      <span className="detail-icon">📍</span>
                      <span className="detail-text">
                        {customer.city}{customer.state ? `, ${customer.state}` : ''}
                      </span>
                    </div>
                  )}
                </div>

                <div className="customer-actions">
                  <button onClick={() => viewCustomerDetails(customer)} className="btn-view">
                    View Details
                  </button>
                  <button onClick={() => handleEdit(customer)} className="btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(customer.id)} className="btn-delete">
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {selectedCustomer && (
        <div className="modal-overlay" onClick={() => setSelectedCustomer(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Customer Details</h3>
            <div className="customer-detail-view">
              <div className="detail-section">
                <h4>Personal Information</h4>
                <div className="detail-grid">
                  <div className="detail-item">
                    <label>Full Name:</label>
                    <span>{selectedCustomer.firstName} {selectedCustomer.lastName}</span>
                  </div>
                  <div className="detail-item">
                    <label>Email:</label>
                    <span>{selectedCustomer.email}</span>
                  </div>
                  <div className="detail-item">
                    <label>Phone:</label>
                    <span>{selectedCustomer.phone}</span>
                  </div>
                  <div className="detail-item">
                    <label>Customer Since:</label>
                    <span>{new Date(selectedCustomer.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
              </div>

              {selectedCustomer.address && (
                <div className="detail-section">
                  <h4>Address</h4>
                  <div className="address-block">
                    <p>{selectedCustomer.address}</p>
                    <p>
                      {selectedCustomer.city}{selectedCustomer.state && `, ${selectedCustomer.state}`} {selectedCustomer.zipCode}
                    </p>
                    <p>{selectedCustomer.country}</p>
                  </div>
                </div>
              )}

              <div className="detail-section">
                <h4>Order History ({customerOrders.length})</h4>
                {customerOrders.length === 0 ? (
                  <p className="no-orders">No orders found for this customer.</p>
                ) : (
                  <table className="orders-summary">
                    <thead>
                      <tr>
                        <th>Order #</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Amount</th>
                      </tr>
                    </thead>
                    <tbody>
                      {customerOrders.map((order) => (
                        <tr key={order.id}>
                          <td>{order.orderNumber}</td>
                          <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                          <td>
                            <span className={`status status-${order.status.toLowerCase()}`}>
                              {order.status}
                            </span>
                          </td>
                          <td>${order.totalAmount.toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr>
                        <td colSpan="3"><strong>Total Spent:</strong></td>
                        <td>
                          <strong>
                            ${customerOrders.reduce((sum, order) => sum + order.totalAmount, 0).toFixed(2)}
                          </strong>
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                )}
              </div>
            </div>
            <button onClick={() => setSelectedCustomer(null)} className="btn-close">Close</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Customers;
