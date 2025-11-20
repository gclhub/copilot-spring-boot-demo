import React, { useState, useEffect } from 'react';
import { customerApi } from '../services/api';

function Customers() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
      <h2>Customers</h2>
      <div className="customer-list">
        {customers.length === 0 ? (
          <p>No customers found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>City</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.id}</td>
                  <td>{customer.firstName} {customer.lastName}</td>
                  <td>{customer.email}</td>
                  <td>{customer.phone}</td>
                  <td>{customer.city}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default Customers;
