# Passwort-Safe

Modul 183 (Kryptographie) & Modul 323 (Funktionale Programmierung) | Rinaldo Lanza

## Setup & Start

### Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
# Läuft auf http://localhost:8080
# H2-Console: http://localhost:8080/h2-console
```

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
# Läuft auf http://localhost:5173
```

## Standard-Login
- Benutzername: `admin`
- Passwort: `Admin123!`

## Projektstruktur
```
password-safe/
├── Grundkonzept.md
├── Dokumentation.md        ← noch zu erstellen
├── backend/                ← Spring Boot, Java 21
│   └── src/main/java/ch/passwordsafe/
│       ├── entity/         ← User, Category, PasswordEntry
│       ├── repository/     ← JPA Repositories
│       ├── service/        ← AES, Auth, Entries, Categories
│       ├── controller/     ← REST API
│       └── config/         ← Security, JWT, DataInitializer
└── frontend/               ← React + Vite
    └── src/
        ├── pages/          ← LoginPage, SafePage
        └── services/       ← api.js (axios)
```

## Technologien
- **Backend:** Java 21, Spring Boot 3, Spring Security, JWT, H2
- **Frontend:** React 18, Vite, Axios
- **Verschlüsselung:** AES-256-CBC + PBKDF2WithHmacSHA256

## Nächste Schritte (Zwischenabgabe 1, 15.06)
- [ ] Eintrag bearbeiten (PUT /api/entries/{id})
- [ ] Master-Passwort ändern
- [ ] Dokumentation.md schreiben
- [ ] Testdaten generieren (generatedata.com)
