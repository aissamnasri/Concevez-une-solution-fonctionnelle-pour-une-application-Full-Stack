/**
 * Configuration du frontend.
 * Les URL pointent vers le backend Spring Boot lance en local (port 8080).
 */
export const environment = {
  apiBaseUrl: 'http://localhost:8080/api',
  wsUrl: 'ws://localhost:8080/ws',
  defaultConversationId: 1
};
