import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { Customers } from './components/customers/customers';
import { Products } from './components/products/products';
import { Orders } from './components/orders/orders';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'customers', component: Customers },
  { path: 'products', component: Products },
  { path: 'orders', component: Orders },
  { path: '**', redirectTo: '' }
];
