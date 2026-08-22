import { Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';

import { environment } from '../../environments/environment';
import { ChatMessage, SendMessagePayload } from '../models/chat.model';

/**
 * Connexion WebSocket (protocole STOMP) au backend Spring Boot.
 *
 * - abonnement  : /topic/conversations/{id}  -> messages diffuses par le serveur
 * - publication : /app/conversations/{id}/send -> messages envoyes par ce client
 */
@Injectable({ providedIn: 'root' })
export class ChatSocketService {

  /** Etat de la connexion, affiche dans l'interface. */
  readonly connected = signal(false);

  private readonly incomingMessages = new Subject<ChatMessage>();
  private client?: Client;

  /** Messages recus en temps reel pour la conversation courante. */
  get messages$(): Observable<ChatMessage> {
    return this.incomingMessages.asObservable();
  }

  connect(conversationId: number): void {
    this.disconnect();

    const client = new Client({
      brokerURL: environment.wsUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        this.connected.set(true);
        client.subscribe(`/topic/conversations/${conversationId}`, (frame: IMessage) => {
          this.incomingMessages.next(JSON.parse(frame.body) as ChatMessage);
        });
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: (frame) => console.error('Erreur STOMP :', frame.headers['message'])
    });

    client.activate();
    this.client = client;
  }

  sendMessage(conversationId: number, payload: SendMessagePayload): void {
    this.client?.publish({
      destination: `/app/conversations/${conversationId}/send`,
      body: JSON.stringify(payload)
    });
  }

  disconnect(): void {
    void this.client?.deactivate();
    this.client = undefined;
    this.connected.set(false);
  }
}
