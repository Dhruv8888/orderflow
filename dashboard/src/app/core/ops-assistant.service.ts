import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AskRequest,
  AskResponse,
  FlaggedOrder,
  PendingAction,
} from './models/ops-assistant.model';

@Injectable({ providedIn: 'root' })
export class OpsAssistantService {
  private http = inject(HttpClient);
  private baseUrl = environment.opsAssistantUrl;

  ask(request: AskRequest): Observable<AskResponse> {
    return this.http.post<AskResponse>(`${this.baseUrl}/ops-assistant/ask`, request);
  }

  getFlaggedOrders(): Observable<FlaggedOrder[]> {
    return this.http.get<FlaggedOrder[]>(`${this.baseUrl}/ops-assistant/flagged-orders`);
  }

  getPendingActions(): Observable<PendingAction[]> {
    return this.http.get<PendingAction[]>(`${this.baseUrl}/ops-assistant/pending-actions`);
  }

  approveAction(id: string): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/ops-assistant/pending-actions/${id}/approve`,
      {}
    );
  }

  rejectAction(id: string): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/ops-assistant/pending-actions/${id}/reject`,
      {}
    );
  }
}