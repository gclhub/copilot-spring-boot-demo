import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OrderService, Order } from '../../services/order';

@Component({
  selector: 'app-orders',
  imports: [CommonModule, MatTableModule, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class Orders implements OnInit {
  orders: Order[] = [];
  displayedColumns: string[] = ['id', 'orderNumber', 'customerName', 'orderDate', 'status', 'totalAmount'];
  loading = true;
  error: string | null = null;

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getAllOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load orders. Please ensure the Order Service is running on port 8083.';
        this.loading = false;
        console.error('Error loading orders:', err);
      }
    });
  }
}
