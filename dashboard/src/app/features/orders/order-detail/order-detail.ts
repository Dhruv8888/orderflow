import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, timer, switchMap } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';

import { OrderService } from '../../../core/order.service';
import { Order, OrderEvent, OrderStatus } from '../../../core/models/order.model';

const POLL_INTERVAL_MS = 2000;

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatButtonModule,
  ],
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.scss',
})
export class OrderDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private destroyRef = inject(DestroyRef);

  orderId = this.route.snapshot.paramMap.get('id')!;

  order = signal<Order | null>(null);
  history = signal<OrderEvent[]>([]);
  loading = signal(true);
  loadError = signal<string | null>(null);

  ngOnInit(): void {
    timer(0, POLL_INTERVAL_MS)
      .pipe(
        switchMap(() =>
          forkJoin({
            order: this.orderService.getOrder(this.orderId),
            history: this.orderService.getOrderHistory(this.orderId),
          })
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ order, history }) => {
          this.order.set(order);
          this.history.set(history);
          this.loading.set(false);
          this.loadError.set(null);
        },
        error: () => {
          this.loading.set(false);
          this.loadError.set('Could not load this order. It may not exist, or the service may be down.');
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

  eventIcon(eventType: string): string {
    const type = eventType.toLowerCase();
    if (type.includes('created')) return 'add_circle';
    if (type.includes('reservationfailed')) return 'error';
    if (type.includes('reserved')) return 'inventory_2';
    if (type.includes('paymentcompleted')) return 'check_circle';
    if (type.includes('paymentfailed')) return 'error';
    if (type.includes('paymentrequested')) return 'payments';
    if (type.includes('refund')) return 'currency_exchange';
    if (type.includes('release')) return 'undo';
    if (type.includes('shipped')) return 'local_shipping';
    if (type.includes('shipment')) return 'local_shipping';
    if (type.includes('remediation')) return 'build';
    return 'circle';
  }

  eventIconClass(eventType: string): string {
    const type = eventType.toLowerCase();
    if (type.includes('failed')) return 'icon-failed';
    if (type.includes('completed') || type.includes('shipped') || type.includes('reserved')) {
      return 'icon-success';
    }
    return 'icon-neutral';
  }

  formatPayload(payloadJson: string): string {
    try {
      return JSON.stringify(JSON.parse(payloadJson), null, 2);
    } catch {
      return payloadJson;
    }
  }
}