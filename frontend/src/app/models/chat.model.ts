/** Message tel qu'il est renvoye par le backend (REST et WebSocket). */
export interface ChatMessage {
  id: number;
  conversationId: number;
  senderName: string;
  content: string;
  /** Date ISO 8601, par exemple 2026-01-01T10:00:00Z */
  createdAt: string;
}

/** Charge utile envoyee au backend pour publier un message. */
export interface SendMessagePayload {
  senderName: string;
  content: string;
}

export interface Conversation {
  id: number;
  createdAt: string;
}
