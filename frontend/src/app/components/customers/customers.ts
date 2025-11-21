import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CustomerService, Customer } from '../../services/customer';

@Component({
  selector: 'app-customers',
  imports: [CommonModule, MatTableModule, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './customers.html',
  styleUrl: './customers.css',
})
export class Customers implements OnInit {
  customers: Customer[] = [];
  displayedColumns: string[] = ['id', 'firstName', 'lastName', 'email', 'phone', 'city', 'state'];
  loading = true;
  error: string | null = null;

  constructor(private customerService: CustomerService) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.customerService.getAllCustomers().subscribe({
      next: (data) => {
        this.customers = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load customers. Please ensure the Customer Service is running on port 8081.';
        this.loading = false;
        console.error('Error loading customers:', err);
      }
    });
  }
}
