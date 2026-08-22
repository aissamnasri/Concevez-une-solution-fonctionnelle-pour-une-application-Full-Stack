import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat.model';
import { ChatApiService } from '../services/chat-api.service';
import { ChatSocketService } from '../services/chat-socket.service';

/**
 * Ecran unique du PoC : historique de la conversation, saisie et envoi.
 * L'interface est volontairement minimale (voir README, section "Perimetre").
 */
@Component({
  selector: 'app-chat',
  imports: [FormsModule, DatePipe],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {

  private readonly chatApi = inject(ChatApiService);
  private readonly chatSocket = inject(ChatSocketService);

  readonly connected = this.chatSocket.connected;
  readonly messages = signal<ChatMessage[]>([]);
  readonly senderName = signal('Alice');
  readonly conversationId = signal(environment.defaultConversationId);
  readonly draft = signal('');
  readonly errorMessage = signal<string | null>(null);

  private incomingMessages?: Subscription;

  ngOnInit(): void {
    this.incomingMessages = this.chatSocket.messages$.subscribe((message) =>
      this.messages.update((messages) => [...messages, message])
    );
    this.joinConversation();
  }

  /** Charge l'historique en REST puis ouvre le WebSocket sur la conversation. */
  joinConversation(): void {
    const conversationId = this.conversationId();
    this.errorMessage.set(null);
    this.messages.set([]);

    this.chatApi.getHistory(conversationId).subscribe({
      next: (history) => {
        this.messages.set(history);
        this.chatSocket.connect(conversationId);
      },
      error: () => {
        this.chatSocket.disconnect();
        this.errorMessage.set(
          `Conversation ${conversationId} indisponible. Vérifiez son identifiant et que le backend est démarré.`
        );
      }
    });
  }

  sendMessage(): void {
    const content = this.draft().trim();
    const senderName = this.senderName().trim();

    if (!senderName || !content) {
      this.errorMessage.set('Renseignez votre nom et un message avant d’envoyer.');
      return;
    }

    this.chatSocket.sendMessage(this.conversationId(), { senderName, content });
    this.draft.set('');
    this.errorMessage.set(null);
  }

  ngOnDestroy(): void {
    this.incomingMessages?.unsubscribe();
    this.chatSocket.disconnect();
  }
}
