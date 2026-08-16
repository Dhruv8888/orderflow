import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'orders',
    loadComponent: () =>
      import('./features/orders/order-list/order-list').then(
        (m) => m.OrderList
      ),
  },
  {
    path: 'orders/:id',
    loadComponent: () =>
      import('./features/orders/order-detail/order-detail').then(
        (m) => m.OrderDetail
      ),
  },
  {
    path: 'ops-assistant',
    loadComponent: () =>
      import('./features/ops-assistant/ops-assistant/ops-assistant').then(
        (m) => m.OpsAssistant
      ),
  },
  {
    path: 'demo-controls',
    loadComponent: () =>
      import('./features/demo-controls/demo-controls/demo-controls').then(
        (m) => m.DemoControls
      ),
  },
  { path: '', redirectTo: 'orders', pathMatch: 'full' },
  { path: '**', redirectTo: 'orders' },
];