import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ChatMessage, Conversation } from '../models/chat.model';

/**
 * Appels REST du tchat.
 * REST sert a lire l'historique ; l'envoi des messages passe par le WebSocket.
 */
@Injectable({ providedIn: 'root' })
export class ChatApiService {

  private readonly http = inject(HttpClient);

  /** GET /api/conversations/{id}/messages */
  getHistory(conversationId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(
      `${environment.apiBaseUrl}/conversations/${conversationId}/messages`
    );
  }

  /** POST /api/conversations */
  createConversation(): Observable<Conversation> {
    return this.http.post<Conversation>(`${environment.apiBaseUrl}/conversations`, {});
  }
}
