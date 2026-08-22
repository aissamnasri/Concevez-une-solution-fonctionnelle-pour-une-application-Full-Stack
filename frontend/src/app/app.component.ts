import { Component } from '@angular/core';

import { ChatComponent } from './chat/chat.component';

@Component({
  selector: 'app-root',
  imports: [ChatComponent],
  template: `
    <header>
      <h1>Your Car Your Way — PoC tchat</h1>
      <p>Preuve de concept : tchat temps réel Angular / Spring Boot / PostgreSQL.</p>
    </header>
    <main>
      <app-chat />
    </main>
  `,
  styles: `
    header,
    main {
      max-width: 720px;
      margin: 0 auto;
    }

    h1 {
      font-size: 1.4rem;
      margin-bottom: 0.25rem;
    }

    header p {
      margin-top: 0;
      color: var(--color-muted);
    }
  `
})
export class AppComponent {}
