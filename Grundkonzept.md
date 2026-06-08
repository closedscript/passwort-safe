# Grundkonzept – Passwort-Safe

**Modul 183 & 323** | Rinaldo Lanza | Autor: nveedee | Datum: 08.06.2026

---

## 1. Projektbeschreibung

Eine Web-Anwendung zur sicheren Verwaltung von Zugangsdaten (Benutzername, Passwort, URL, Bemerkungen). Die Einträge werden verschlüsselt gespeichert und sind nach Login abrufbar. Einträge lassen sich in Rubriken organisieren (z.B. Privat, Schule, Verein).

---

## 2. Architektur

**Frontend:** React + Vite (JavaScript)  
**Backend:** Java 21 + Spring Boot 3  
**Datenbank:** H2 In-Memory (Entwicklung)  
**Kommunikation:** REST API (JSON)

```
Browser (React)
    │
    ▼ HTTP/REST
Spring Boot Backend
    │
    ▼
H2 In-Memory DB
```

---

## 3. Verschlüsselung (Modul 183)

### Gewähltes Verfahren: AES (Advanced Encryption Standard)

- **Typ:** Symmetrische Verschlüsselung
- **Schlüssellänge:** 256 Bit
- **Modus:** AES/CBC/PKCS5Padding
- **Schlüsselherleitung:** PBKDF2WithHmacSHA256 aus dem Master-Passwort + Salt
- **Speicherformat:** Base64-kodiert in der Datenbank

**Begründung:** AES ist ein bewährter, effizienter Standard für die Verschlüsselung von Daten im Ruhezustand. Da es sich um einen einzelnen Benutzer handelt, ist ein symmetrisches Verfahren ausreichend und einfacher umzusetzen als RSA.

### Schlüsselmanagement

- Das Master-Passwort verlässt das Frontend **nie im Klartext** (wird gehasht übertragen)
- Der AES-Schlüssel wird serverseitig aus Master-Passwort + Salt abgeleitet (PBKDF2)
- Der abgeleitete Schlüssel wird **nicht** in der DB gespeichert – nur bei aktiver Session im Speicher
- Jeder verschlüsselte Eintrag hat einen eigenen IV (Initialization Vector)

---

## 4. Authentisierung

- Fester Benutzer in der DB (kein Registrierungs-Flow)
- Master-Passwort wird als bcrypt-Hash gespeichert
- Nach Login: JWT-Token für Session-Management
- Passwortgüte: mind. 8 Zeichen, Gross-/Kleinbuchstaben, Ziffer, Sonderzeichen

---

## 5. Datenstruktur

### Entitäten

**User**
- id, username, passwordHash, salt, createdAt

**Category (Rubrik)**
- id, name, user_id

**PasswordEntry (Eintrag)**
- id, title, url, usernameEncrypted, passwordEncrypted, emailEncrypted, notesEncrypted, iv, category_id, user_id, createdAt, updatedAt

---

## 6. OWASP Top Ten – Massnahmen (Modul 183)

| Risiko | Massnahme |
|--------|-----------|
| **Broken Access Control** | Jeder Endpunkt prüft per JWT ob der eingeloggte User die Ressource besitzt |
| **Injection** | Keine nativen SQL-Queries – ausschliesslich JPA/Hibernate mit parametrisierten Queries |
| **Cross-Site Scripting (XSS)** | React escaped Output automatisch; keine `dangerouslySetInnerHTML` |
| **Cryptographic Failures** | AES-256-CBC + PBKDF2, keine schwachen Algorithmen (kein MD5, kein DES) |
| **Insecure Design** | Passwörter nie im Klartext; AES-Schlüssel nur im Session-Speicher |
| **Identification & Authentication Failures** | bcrypt für Master-Passwort, JWT mit Ablaufzeit, Passwortgüte-Prüfung |

---

## 7. Funktionale Programmierung (Modul 323)

### Eingesetzte Konzepte

- **Pure Functions:** Sortier- und Filterfunktionen im Frontend ohne Seiteneffekte
- **Immutable Data:** Zustandsverwaltung mit React-Hooks (`useState`, niemals direktes Mutieren)
- **Higher-Order Functions:** `.filter()`, `.map()`, `.sort()`, `.reduce()` für Listenverarbeitung
- **Lambda-Ausdrücke (Java):** Stream API im Backend für Filterung und Transformation
- **Deklarativer Stil:** Beschreibung des Endzustands statt imperativem "wie"

### Beispiel-Anwendungsfälle

- Einträge nach Rubrik filtern → `entries.filter(e => e.categoryId === selected)`
- Spalten sortieren (ASC/DESC) → pure sort-Funktion ohne Originalarray zu verändern
- Suche über alle Felder → `entries.filter(e => fields.some(f => e[f].includes(query)))`

---

## 8. Rubriken-Struktur (Pflicht, Modul 323)

Einträge können in Rubriken organisiert werden:
- Standard-Rubriken: **Privat**, **Schule**, **Verein**
- Rubriken können hinzugefügt, umbenannt und gelöscht werden
- Filter/Dropdown im Frontend zur Auswahl der Rubrik

---

## 9. Sicherheitsrelevante Punkte (Zusammenfassung)

- Passwörter nie im Klartext in DB oder Logs
- Passwortfelder im Frontend immer als `type="password"` (`*****`)
- Passwortgüte wird geprüft (Länge ≥ 8, Komplexität)
- Benutzer sieht nur eigene Einträge (Row-Level-Security via JWT)
- HTTPS wird vorausgesetzt (Produktionsumgebung)
- IV pro Eintrag individuell (verhindert Pattern-Analyse)
