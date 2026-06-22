# Passwort-Safe

Schulprojekt für **Modul 183** (Applikationssicherheit) & **Modul 323** (Funktionale Programmierung)  
BBW Winterthur | Noel von Daeniken & Rinaldo Lanza

Ein verschlüsselter Passwort-Manager mit AES-256-CBC-Verschlüsselung, JWT-Authentifizierung und funktionalen Programmiermustern.

---

## Setup & Start

### Voraussetzungen

- Java 21 (JDK)
- Maven (oder `./mvnw` im Backend-Verzeichnis)
- Node.js 18+ / npm

### Backend starten (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

- Läuft auf: `http://localhost:8080`
- H2-Konsole: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)
- Beim ersten Start werden automatisch 28 Testeinträge angelegt.

### Frontend starten (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

- Läuft auf: `http://localhost:5173`

---

## Standard-Login

| Feld | Wert |
|------|------|
| Benutzername | `admin` |
| Passwort | `Admin123!` |

---

## Tech-Stack

| Schicht | Technologie |
|---------|-------------|
| Backend | Java 21, Spring Boot 3, Spring Security, JWT (jjwt) |
| Datenbank | H2 (In-Memory), Spring Data JPA |
| Verschlüsselung | AES-256-CBC + PBKDF2WithHmacSHA256 (65 536 Iter.) |
| Authentifizierung | bcrypt (BCryptPasswordEncoder) + JWT |
| Frontend | React 18, Vite, Axios |

---

## Projektstruktur

```
password-safe/
├── Grundkonzept.md
├── Dokumentation.md
├── backend/                         ← Spring Boot, Java 21
│   └── src/main/java/ch/passwordsafe/
│       ├── config/                  ← SecurityConfig, JwtAuthFilter, DataInitializer
│       ├── controller/              ← AuthController, PasswordEntryController, CategoryController
│       ├── entity/                  ← User, Category, PasswordEntry
│       ├── repository/              ← JPA Repositories
│       └── service/                 ← AesEncryptionService, JwtService, AuthService, PasswordEntryService
└── frontend/                        ← React + Vite
    └── src/
        ├── pages/                   ← LoginPage.jsx, SafePage.jsx
        └── services/                ← api.js (Axios)
```

---

## Features

- **Verschlüsselte Speicherung**: Benutzername, Passwort, E-Mail und Notizen werden mit AES-256-CBC verschlüsselt. Der Schlüssel wird mittels PBKDF2 aus dem Master-Passwort abgeleitet und **nie** in der Datenbank gespeichert.
- **Passwortgüte-Anzeige**: Beim Login und bei jedem Eintrag wird die Passwortstärke in Echtzeit als Farbbalken angezeigt.
- **Kategorien**: Einträge können Rubriken (Privat, Schule, Verein, Arbeit, Games) zugeordnet werden.
- **Suche & Sortierung**: Volltextsuche und Sortierung nach Titel, URL, Benutzer oder Rubrik.
- **Edit-Funktion**: Bestehende Einträge können über ein Modal vollständig bearbeitet werden (`PUT /api/entries/{id}`).
- **28 Testeinträge**: Beim ersten Start werden realistische Einträge für alle Rubriken generiert.

---

## API-Übersicht

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| POST | `/api/auth/login` | Login, gibt JWT zurück |
| GET | `/api/entries` | Alle Einträge (entschlüsselt) |
| POST | `/api/entries` | Neuer Eintrag |
| PUT | `/api/entries/{id}` | Eintrag bearbeiten |
| DELETE | `/api/entries/{id}` | Eintrag löschen |
| GET | `/api/categories` | Alle Rubriken |
| POST | `/api/categories` | Neue Rubrik |
| PUT | `/api/categories/{id}` | Rubrik umbenennen |
| DELETE | `/api/categories/{id}` | Rubrik löschen |
