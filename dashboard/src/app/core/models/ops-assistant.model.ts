export interface AskRequest {
  orderId: string;
  question: string;
}

export interface AskResponse {
  answer: string;
}

export interface FlaggedOrder {
  id: string;
  orderId: string;
  diagnosis: string;
  detectedAt: string;
}

export type PendingActionStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXECUTED';

export interface PendingAction {
  id: string;
  orderId: string;
  proposedAction: string;
  reasoning: string;
  status: PendingActionStatus;
  createdAt: string;
}