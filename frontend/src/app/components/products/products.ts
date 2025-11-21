import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProductService, Product } from '../../services/product';

@Component({
  selector: 'app-products',
  imports: [CommonModule, MatTableModule, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class Products implements OnInit {
  products: Product[] = [];
  displayedColumns: string[] = ['id', 'name', 'sku', 'category', 'price', 'stockQuantity'];
  loading = true;
  error: string | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAllProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load products. Please ensure the Inventory Service is running on port 8082.';
        this.loading = false;
        console.error('Error loading products:', err);
      }
    });
  }
}
