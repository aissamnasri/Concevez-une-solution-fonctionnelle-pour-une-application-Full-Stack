import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat.model';
import { ChatApiService } from './chat-api.service';

describe('ChatApiService', () => {
  let service: ChatApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ChatApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('récupère l\'historique d\'une conversation', () => {
    const history: ChatMessage[] = [
      {
        id: 1,
        conversationId: 1,
        senderName: 'Alice',
        content: 'Bonjour',
        createdAt: '2026-01-01T10:00:00Z'
      }
    ];
    let received: ChatMessage[] | undefined;

    service.getHistory(1).subscribe((messages) => (received = messages));

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/conversations/1/messages`);
    expect(request.request.method).toBe('GET');
    request.flush(history);

    expect(received).toEqual(history);
  });

  it('crée une conversation', () => {
    let createdId: number | undefined;

    service.createConversation().subscribe((conversation) => (createdId = conversation.id));

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/conversations`);
    expect(request.request.method).toBe('POST');
    request.flush({ id: 42, createdAt: '2026-01-01T10:00:00Z' });

    expect(createdId).toBe(42);
  });
});
