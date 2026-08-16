export type OrderStatus =
  | 'CREATED'
  | 'STOCK_RESERVED'
  | 'PAID'
  | 'SHIPPED'
  | 'CANCELLED'
  | 'FAILED';

export interface OrderItem {
  id: string;
  orderId: string;
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: string;
  customerId: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}

export interface OrderEvent {
  id: string;
  orderId: string;
  eventType: string;
  payloadJson: string;
  createdAt: string;
}

export interface CreateOrderItemRequest {
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateOrderRequest {
  customerId: string;
  items: CreateOrderItemRequest[];
}