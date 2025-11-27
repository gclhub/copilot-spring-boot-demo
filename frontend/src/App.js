import React, { useState } from 'react';
import './App.css';
import Customers from './components/Customers';
import Products from './components/Products';
import Orders from './components/Orders';

function App() {
  const [activeTab, setActiveTab] = useState('customers');

  return (
    <div className="App">
      <header className="App-header">
        <h1>E-Commerce Management System</h1>
        <p className="subtitle">Microservices Demo with UI Testing Support</p>
      </header>
      
      <nav className="navigation">
        <button 
          className={activeTab === 'customers' ? 'active' : ''} 
          onClick={() => setActiveTab('customers')}
        >
          Customers
        </button>
        <button 
          className={activeTab === 'products' ? 'active' : ''} 
          onClick={() => setActiveTab('products')}
        >
          Products
        </button>
        <button 
          className={activeTab === 'orders' ? 'active' : ''} 
          onClick={() => setActiveTab('orders')}
        >
          Orders
        </button>
      </nav>

      <main className="content">
        {activeTab === 'customers' && <Customers />}
        {activeTab === 'products' && <Products />}
        {activeTab === 'orders' && <Orders />}
      </main>

      <footer className="App-footer">
        <p>Testing frameworks: Jest | Cucumber | Selenium</p>
      </footer>
    </div>
  );
}

export default App;
