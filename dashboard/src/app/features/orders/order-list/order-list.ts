import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { OrderService } from '../../../core/order.service';
import { Order, OrderStatus } from '../../../core/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './order-list.html',
  styleUrl: './order-list.scss',
})
export class OrderList implements OnInit {
  private fb = inject(FormBuilder);
  private orderService = inject(OrderService);
  private router = inject(Router);

  orders = signal<Order[]>([]);
  loadingOrders = signal(true);
  submitting = signal(false);
  submitError = signal<string | null>(null);

  displayedColumns = ['id', 'customerId', 'status', 'createdAt'];

  orderForm = this.fb.nonNullable.group({
    customerId: ['', Validators.required],
    items: this.fb.array([this.createItemRow()]),
  });

  get items() {
    return this.orderForm.controls.items;
  }

  private createItemRow() {
    return this.fb.nonNullable.group({
      productId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }

  addItemRow(): void {
    this.items.push(this.createItemRow());
  }

  removeItemRow(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loadingOrders.set(true);
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loadingOrders.set(false);
      },
      error: () => {
        this.loadingOrders.set(false);
      },
    });
  }

  submitOrder(): void {
    if (this.orderForm.invalid) {
      this.orderForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.submitError.set(null);

    const raw = this.orderForm.getRawValue();

    this.orderService
      .createOrder({
        customerId: raw.customerId,
        items: raw.items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
        })),
      })
      .subscribe({
        next: (order) => {
          this.submitting.set(false);
          this.router.navigate(['/orders', order.id]);
        },
        error: (err) => {
          this.submitting.set(false);
          this.submitError.set(
            err?.error?.message ?? 'Failed to create order. Check the console for details.'
          );
        },
      });
  }

  statusColor(status: OrderStatus): string {
    switch (status) {
      case 'SHIPPED':
        return 'status-success';
      case 'FAILED':
      case 'CANCELLED':
        return 'status-failed';
      default:
        return 'status-pending';
    }
  }
}