# Your Car Your Way — PoC tchat

## 1. Présentation

Your Car Your Way modernise ses applications web internationales. L'architecture cible retenue est
un **monolithe modulaire** : frontend Angular, backend Spring Boot exposant une API REST, base
PostgreSQL, le tout reproductible avec Docker.

Une fonctionnalité de cette cible sort du cadre « requête / réponse » habituel : le **tchat entre un
client et un conseiller**, qui doit être temps réel. Ce dépôt contient une **preuve de concept (PoC)**
limitée à cette fonctionnalité, afin de vérifier qu'elle s'intègre proprement dans l'architecture
validée, **sans introduire de microservices**.

Ce n'est pas le produit final : c'est une démonstration technique.

## 2. Objectif du PoC

> Valider techniquement la faisabilité d'un tchat temps réel dans l'architecture cible de
> Your Car Your Way.

Concrètement, le PoC démontre que :

1. Angular communique avec Spring Boot ;
2. Spring Boot gère une communication WebSocket ;
3. deux clients échangent des messages en temps réel ;
4. chaque message est persisté dans PostgreSQL ;
5. l'historique est récupérable via une API REST ;
6. REST et WebSocket cohabitent proprement dans le même backend ;
7. l'environnement se lance facilement (Docker Compose) ;
8. un développeur junior peut comprendre et démarrer le projet avec ce README.

## 3. Périmètre

Inclus dans ce PoC :

- une conversation identifiée par un identifiant numérique ;
- consultation de l'historique des messages ;
- envoi d'un message ;
- réception des nouveaux messages en temps réel, sans rechargement de page ;
- persistance de chaque message dans PostgreSQL ;
- récupération de l'historique après un rafraîchissement (F5) ;
- API REST : création d'une conversation, lecture d'une conversation, lecture de l'historique ;
- endpoint WebSocket dédié au tchat ;
- tests backend (service, persistance, endpoint REST, intégration WebSocket) et test frontend ;
- démarrage de PostgreSQL via Docker Compose.

## 4. Hors périmètre

Volontairement **non développé**, car non nécessaire à la démonstration :

réservation, paiement, gestion des véhicules, gestion des agences, gestion des comptes clients,
back-office d'administration, notifications e-mail, SMS, pièces jointes, partage de fichiers,
appels audio, appels vidéo, réactions, emojis avancés, recherche avancée, groupes, indicateurs de
présence ou de saisie, authentification et autorisation, architecture microservices, Kubernetes,
infrastructure cloud, stack de monitoring complète.

L'expéditeur est identifié par un **simple nom saisi dans l'interface**. C'est une simplification
assumée du PoC : il n'y a **aucune authentification**.

## 5. Architecture

```mermaid
flowchart LR
    subgraph Navigateur
        A["Angular 20<br/>ChatComponent"]
    end

    subgraph Backend["Spring Boot 3.5"]
        R["MessageController<br/>ConversationController<br/>(REST)"]
        W["ChatWebSocketController<br/>(STOMP)"]
        S["ChatService<br/>(logique métier)"]
        P["Repositories<br/>(Spring Data JPA)"]
    end

    DB[("PostgreSQL 16")]

    A -->|"REST : historique"| R
    A <-->|"WebSocket : temps réel"| W
    R --> S
    W --> S
    S --> P
    P -->|JPA / Hibernate| DB
```

Lecture du diagramme :

- Angular utilise **deux canaux complémentaires** vers le même backend. REST pour ce qui est
  ponctuel (charger l'historique au démarrage ou après un F5), WebSocket pour ce qui est continu
  (recevoir les messages des autres participants).
- Les deux contrôleurs (REST et WebSocket) sont de simples adaptateurs : ils délèguent tout à
  `ChatService`. **La logique métier n'est écrite qu'une fois.**
- `ChatService` est la seule couche qui parle aux repositories ; les repositories sont les seuls à
  parler à la base.
- Aucun composant supplémentaire (broker externe, service séparé) n'est nécessaire : le PoC reste
  un monolithe modulaire, conformément à l'architecture validée.

## 6. Structure du projet

```
ycyw-chat-poc/
├── docker-compose.yml          # PostgreSQL pour le développement local
├── .env.example                # Variables d'environnement à copier en .env
├── .gitignore
├── README.md
│
├── backend/                    # Application Spring Boot
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/yourcaryourway/chat/
│       │   ├── ChatApplication.java             # Point d'entrée
│       │   ├── config/
│       │   │   ├── WebSocketConfig.java         # Endpoint /ws, préfixes /app et /topic
│       │   │   ├── CorsConfig.java              # CORS de l'API REST (dev)
│       │   │   ├── WebSocketEventLogger.java    # Logs connexion / déconnexion
│       │   │   └── DemoConversationInitializer.java  # Crée la conversation n° 1
│       │   ├── controller/
│       │   │   ├── ConversationController.java  # REST : créer / lire une conversation
│       │   │   ├── MessageController.java       # REST : historique
│       │   │   └── ChatWebSocketController.java # WebSocket : envoi + diffusion
│       │   ├── service/ChatService.java         # Logique métier du tchat
│       │   ├── repository/                      # Accès aux données (Spring Data JPA)
│       │   ├── entity/                          # Conversation, Message
│       │   ├── dto/                             # Contrats d'entrée / sortie
│       │   └── exception/                       # Erreurs métier + handler REST
│       ├── main/resources/application.properties
│       └── test/                                # Tests unitaires et d'intégration
│
└── frontend/                   # Application Angular
    ├── package.json
    ├── angular.json
    └── src/
        ├── environments/environment.ts          # URL du backend
        └── app/
            ├── app.component.ts
            ├── app.config.ts
            ├── chat/                            # Écran du tchat
            ├── services/
            │   ├── chat-api.service.ts          # Appels REST
            │   └── chat-socket.service.ts       # Connexion WebSocket / STOMP
            └── models/chat.model.ts             # Types partagés avec l'API
```

## 7. Prérequis

| Outil | Version utilisée | Remarque |
|---|---|---|
| Java (JDK) | 21 | version cible du `pom.xml` |
| Maven | 3.9+ | ou l'exécution Maven intégrée à votre IDE |
| Node.js | 22.x | Angular 20 exige `^20.19`, `^22.12` ou `^24` |
| npm | 10.x | installé avec Node.js |
| Angular CLI | 20.x | inutile globalement : `npx ng …` utilise la version du projet |
| Docker | avec Compose v2 (`docker compose`) | pour PostgreSQL |
| Git | 2.x | |

Vérification rapide :

```bash
java -version
mvn -version
node -v
npm -v
docker compose version
```

## 8. Installation

```bash
git clone <url-du-depot>
cd ycyw-chat-poc
```

Démarrer la base de données :

```bash
docker compose up -d
```

Installer les dépendances du frontend :

```bash
cd frontend
npm install
cd ..
```

Le backend télécharge ses dépendances tout seul au premier `mvn` (aucune commande d'installation
séparée).

## 9. Configuration

**PostgreSQL (docker-compose.yml)**

| Paramètre | Valeur par défaut |
|---|---|
| base | `ycyw_chat` |
| utilisateur | `ycyw` |
| mot de passe | `ycyw-local-dev` |
| port | `5432` |

Ces valeurs sont des **valeurs de développement local**, pas des secrets. Aucun secret réel n'est
versionné. Pour les surcharger : `cp .env.example .env` puis modifier le `.env` (non versionné).

**Backend (`backend/src/main/resources/application.properties`)**

Toutes les valeurs sensibles sont externalisées et surchargeables par variable d'environnement :

| Variable | Défaut |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/ycyw_chat` |
| `DATABASE_USERNAME` | `ycyw` |
| `DATABASE_PASSWORD` | `ycyw-local-dev` |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |

Le schéma de base est généré par Hibernate (`spring.jpa.hibernate.ddl-auto=update`) : c'est une
simplification de PoC, voir la section « Limites ».

**Ports utilisés**

| Service | Port |
|---|---|
| Frontend Angular | 4200 |
| Backend Spring Boot | 8080 |
| PostgreSQL | 5432 |

**WebSocket** : endpoint `ws://localhost:8080/ws`, préfixe d'envoi `/app`, préfixe de diffusion
`/topic`. Les origines autorisées pour la poignée de main sont les mêmes que pour le CORS REST.

**Frontend (`frontend/src/environments/environment.ts`)** : `apiBaseUrl`, `wsUrl` et
`defaultConversationId`. À modifier si vous changez les ports du backend.

## 10. Lancer le projet

Trois terminaux, dans cet ordre.

**1 — Base de données**

```bash
docker compose up -d
docker compose ps          # postgres doit être "healthy"
```

**2 — Backend**

```bash
cd backend
mvn spring-boot:run
```

Le backend écoute sur `http://localhost:8080`. Au premier démarrage sur une base vide, il crée une
conversation de démonstration (identifiant `1`) et l'écrit dans les logs.

**3 — Frontend**

```bash
cd frontend
npm start
```

L'application est disponible sur `http://localhost:4200`.

Pour arrêter : `Ctrl+C` dans les deux terminaux, puis `docker compose down`
(`docker compose down -v` pour supprimer aussi les données).

## 11. Tester le tchat

1. Lancer les trois composants (section 10).
2. Ouvrir `http://localhost:4200` dans un premier navigateur (ou onglet) — **A**.
3. Ouvrir la même URL dans un second navigateur, ou une fenêtre de navigation privée — **B**.
4. Sur A, saisir le nom `Alice` ; sur B, saisir le nom `Bob`. Les deux gardent l'identifiant de
   conversation `1` et cliquent sur **Rejoindre**. L'indicateur affiche « Temps réel : connecté ».
5. Depuis A, écrire `Bonjour` et cliquer sur **Envoyer**.
6. Vérifier que le message apparaît **immédiatement sur B**, sans rechargement.
7. Depuis B, répondre `Bonjour Alice` et vérifier la réception immédiate sur A.
8. Rafraîchir la page (F5) sur A **et** sur B.
9. Vérifier que les deux messages sont toujours affichés : ils viennent de PostgreSQL, via REST.

Vérifier la persistance directement en base (facultatif) :

```bash
docker exec -it ycyw-chat-postgres psql -U ycyw -d ycyw_chat -c "SELECT id, sender_name, content, created_at FROM messages ORDER BY id;"
```

## 12. API REST

Base : `http://localhost:8080/api`

### `GET /api/conversations/{conversationId}/messages`

Historique complet d'une conversation, du plus ancien au plus récent.

| Élément | Détail |
|---|---|
| Paramètre | `conversationId` (chemin, entier) |
| Réponse | `200 OK` — tableau de messages |
| Erreurs | `404` conversation inconnue, `400` identifiant non numérique |

```json
[
  {
    "id": 1,
    "conversationId": 1,
    "senderName": "Alice",
    "content": "Bonjour",
    "createdAt": "2026-01-01T10:00:00Z"
  }
]
```

### `POST /api/conversations`

Crée une conversation vide (utile pour démontrer plusieurs fils de discussion).

| Élément | Détail |
|---|---|
| Corps | aucun |
| Réponse | `201 Created` — `{ "id": 2, "createdAt": "..." }` |

### `GET /api/conversations/{conversationId}`

Vérifie l'existence d'une conversation.

| Élément | Détail |
|---|---|
| Réponse | `200 OK` — `{ "id": 1, "createdAt": "..." }` |
| Erreurs | `404` conversation inconnue |

### Format des erreurs

```json
{ "status": 404, "message": "Conversation introuvable : 99", "timestamp": "2026-01-01T10:00:00Z" }
```

Exemples avec curl :

```bash
curl http://localhost:8080/api/conversations/1/messages
curl -X POST http://localhost:8080/api/conversations
```

## 13. WebSocket

| Élément | Valeur |
|---|---|
| URL de connexion | `ws://localhost:8080/ws` |
| Protocole | STOMP sur WebSocket natif (pas de SockJS) |
| Destination d'envoi | `/app/conversations/{conversationId}/send` |
| Destination d'abonnement | `/topic/conversations/{conversationId}` |

**Message envoyé par le client** :

```json
{ "senderName": "Alice", "content": "Bonjour" }
```

`senderName` : obligatoire, 50 caractères maximum. `content` : obligatoire, 1000 caractères maximum.
Un message invalide est rejeté côté serveur et journalisé ; il n'est ni enregistré, ni diffusé.

**Message diffusé par le serveur** (identique au format REST) :

```json
{
  "id": 1,
  "conversationId": 1,
  "senderName": "Alice",
  "content": "Bonjour",
  "createdAt": "2026-01-01T10:00:00Z"
}
```

### Pourquoi WebSocket, et pas seulement REST ?

REST fonctionne en requête / réponse : **seul le client peut déclencher un échange**. Pour afficher
le message d'un autre participant, il faudrait interroger le serveur en boucle (polling), ce qui
coûte cher en requêtes inutiles tout en gardant un délai perceptible.

WebSocket ouvre une connexion permanente et bidirectionnelle : **le serveur peut pousser** un
message vers les clients dès qu'il arrive. C'est exactement le besoin du tchat.

Les deux approches sont **complémentaires** et c'est ce que le PoC valide : REST pour l'historique
(ponctuel, cacheable, facile à tester), WebSocket pour le flux temps réel.

### Trajet d'un message

```mermaid
sequenceDiagram
    participant A as Navigateur A (Alice)
    participant B as Navigateur B (Bob)
    participant WS as ChatWebSocketController
    participant SVC as ChatService
    participant DB as PostgreSQL

    A->>WS: SEND /app/conversations/1/send<br/>{senderName, content}
    Note over WS: validation du contenu
    WS->>SVC: saveMessage(1, requête)
    SVC->>DB: INSERT INTO messages
    DB-->>SVC: message #42 (id + createdAt)
    SVC-->>WS: MessageResponse
    WS->>A: /topic/conversations/1
    WS->>B: /topic/conversations/1
    Note over A,B: affichage immédiat, sans rechargement
```

Points importants :

- **Le message est persisté avant d'être diffusé.** Un client ne peut donc pas afficher un message
  absent de la base : l'historique rechargé après un F5 est toujours cohérent avec ce qui a été vu.
- L'expéditeur reçoit lui aussi la diffusion : tous les clients affichent la même version du message
  (même identifiant, même horodatage serveur).
- La diffusion est faite par le **broker en mémoire** de Spring vers tous les abonnés du topic de la
  conversation. Les autres conversations ne reçoivent rien.

## 14. Tests

**Backend**

```bash
cd backend
mvn test
```

| Test | Ce qu'il vérifie |
|---|---|
| `ChatServiceTest` | envoi d'un message (contenu nettoyé, DTO renvoyé), refus si la conversation n'existe pas, ordre de l'historique |
| `MessageRepositoryTest` | persistance réelle et relecture ordonnée, cloisonnement entre conversations |
| `MessageControllerTest` | endpoint REST `GET …/messages` : réponse `200` et cas `404` |
| `ChatWebSocketIntegrationTest` | un client STOMP réel envoie un message : il est diffusé aux abonnés **et** présent en base |

Les tests utilisent une base **H2 en mémoire** : ils s'exécutent sans Docker et sans PostgreSQL.
C'est un compromis de PoC ; un projet réel utiliserait Testcontainers pour tester sur le même
moteur qu'en production.

**Frontend**

```bash
cd frontend
npm test                 # mode interactif, ouvre un navigateur
npm run test:ci          # exécution unique, Chrome headless (nécessite Chrome installé)
```

`ChatApiService` est couvert par `chat-api.service.spec.ts` (lecture de l'historique et création
d'une conversation, avec `HttpTestingController`).

La couverture n'est volontairement pas maximisée : les tests ciblent le mécanisme central du PoC.

## 15. Choix techniques

| Choix | Raison |
|---|---|
| **Angular** | imposé par l'architecture cible ; framework structurant (TypeScript, injection de dépendances, tests intégrés) adapté à une application internationale maintenue dans la durée |
| **Spring Boot** | imposé par l'architecture cible ; REST, WebSocket, JPA et tests dans un seul écosystème, ce qui évite d'ajouter une brique technique juste pour le temps réel |
| **PostgreSQL** | imposé par l'architecture cible ; SGBD relationnel éprouvé, adapté à des données structurées et historisées comme des messages |
| **REST** | standard pour les échanges ponctuels ; simple à tester, à documenter et à mettre en cache |
| **WebSocket** | seul moyen d'obtenir une diffusion serveur → clients sans polling (voir section 13) |
| **STOMP** (sur WebSocket) | fournit nativement l'abonnement par destination (`/topic/conversations/{id}`) : Spring gère la liste des abonnés, le code applicatif se limite à une ligne de diffusion. Plus lisible qu'un registre de sessions écrit à la main |
| **WebSocket natif, sans SockJS** | SockJS n'apporte qu'une compatibilité avec des navigateurs qui ne gèrent pas WebSocket ; ce n'est plus le cas aujourd'hui, et l'enlever réduit le code et les concepts à comprendre |
| **Docker Compose** | environnement reproductible : un développeur junior obtient la même base que tout le monde en une commande |
| **Broker en mémoire** | suffisant pour une instance unique ; un broker externe (RabbitMQ) serait nécessaire seulement à la mise à l'échelle (voir section 17) |

## 16. Limites du PoC

Ce projet est une **preuve de concept**, pas une version de production. En l'état :

- **Aucune authentification ni autorisation** : l'expéditeur est un nom libre saisi dans
  l'interface, et n'importe qui peut rejoindre n'importe quelle conversation en changeant
  l'identifiant. Un vrai déploiement exigerait une authentification et un contrôle d'accès à la
  conversation.
- **Schéma géré par Hibernate** (`ddl-auto=update`) au lieu de migrations versionnées
  (Flyway / Liquibase).
- **Broker en mémoire** : une seule instance backend. Avec plusieurs instances, deux clients
  connectés à des instances différentes ne se verraient pas.
- **Erreurs WebSocket seulement journalisées** côté serveur : le client n'est pas notifié qu'un
  message a été rejeté.
- **Pas de pagination de l'historique** : la conversation est chargée en entier.
- **Pas de reprise de l'historique après reconnexion** : la reconnexion automatique est active, mais
  les messages reçus pendant une coupure ne sont pas rattrapés (un F5 les récupère).
- **Tests sur H2**, pas sur PostgreSQL.
- **CORS ouvert au frontend de développement** uniquement ; à durcir avant tout déploiement.
- **Interface volontairement minimale** : le PoC ne démontre pas de compétences UI. Les bases
  d'accessibilité sont respectées (structure HTML sémantique, labels associés aux champs, navigation
  clavier, focus visible, liste des messages en `aria-live`, contrastes suffisants), mais aucun
  audit RGAA complet n'a été mené.

## 17. Évolutions possibles

À envisager **après** validation du PoC, si le tchat est intégré au produit :

- authentification et rattachement des messages à un compte client / conseiller ;
- file d'attente et affectation des conversations à un conseiller disponible ;
- indicateurs de présence, de saisie et accusés de lecture ;
- pagination de l'historique et rattrapage des messages après reconnexion ;
- pièces jointes, puis appels audio et vidéo (prévus au cahier des charges, hors PoC) ;
- migrations de schéma versionnées (Flyway) ;
- broker externe (RabbitMQ) pour supporter plusieurs instances backend ;
- conteneurisation du backend et du frontend, puis intégration à la chaîne CI/CD ;
- observabilité complète (métriques, traces, agrégation de logs) ;
- tests d'intégration sur PostgreSQL via Testcontainers, et tests end-to-end du scénario à deux clients.

## Annexe — Modèle de données

```mermaid
erDiagram
    CONVERSATIONS ||--o{ MESSAGES : contient
    CONVERSATIONS {
        bigint id PK
        timestamp created_at
    }
    MESSAGES {
        bigint id PK
        bigint conversation_id FK
        varchar sender_name
        varchar content
        timestamp created_at
    }
```

Modèle **minimal**, limité au strict nécessaire pour démontrer le tchat : une conversation contient
plusieurs messages, un message appartient à une conversation. Le modèle métier complet de
Your Car Your Way (clients, agences, véhicules, réservations) n'est volontairement pas reproduit.

`sender_name` remplace une véritable référence utilisateur, faute d'authentification dans le PoC.
