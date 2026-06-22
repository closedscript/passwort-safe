# Dokumentation – Passwort-Safe

**Modul 183** (Applikationssicherheit implementieren) & **Modul 323** (Funktional programmieren)  
BBW Winterthur | Autoren: Noel von Daeniken, Rinaldo Lanza

---

## Modul 183 – Kryptographie und Applikationssicherheit

### Gewählte Verschlüsselung: AES-256-CBC

Zum Verschlüsseln aller sensitiven Felder (Benutzername, Passwort, E-Mail, Notizen) wird **AES-256 im CBC-Modus** eingesetzt. AES wurde gegenüber RSA bewusst gewählt, weil:

- **Single-User-Anwendung**: RSA (asymmetrisch) ist für Szenarien ausgelegt, bei denen Public- und Private-Key getrennt sind (z. B. E-Mail-Verschlüsselung zwischen Personen). Hier gibt es nur einen Nutzer mit einem Master-Passwort – symmetrische Verschlüsselung reicht vollständig aus.
- **Performance**: AES-CBC ist bei blockweise verarbeiteten Daten deutlich effizienter als RSA. Das ist relevant, wenn viele Einträge gleichzeitig entschlüsselt werden.
- **Standardkonformität**: AES-256 gilt als NIST-Standard und ist für die nächsten Jahrzehnte als sicher eingestuft.

### Schlüsselmanagement: PBKDF2 + Salt + IV

Der AES-Schlüssel wird **niemals direkt gespeichert**. Er wird zur Laufzeit aus dem Master-Passwort des Nutzers abgeleitet:

```java
// AesEncryptionService.java – deriveKey()
SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, 65536, 256);
byte[] keyBytes = factory.generateSecret(spec).getEncoded();
return new SecretKeySpec(keyBytes, "AES");
```

- **PBKDF2WithHmacSHA256**: 65 536 Iterationen machen Brute-Force-Angriffe rechnerisch teuer.
- **Salt (pro User, Base64)**: Verhindert Rainbow-Table-Angriffe. Wird bei der Benutzeranlage zufällig generiert (`SecureRandom`) und in der Datenbank gespeichert.
- **IV (pro Eintrag, Base64)**: Jeder Eintrag erhält beim Speichern einen neuen, zufälligen Initialization Vector. Damit produziert dasselbe Klartext-Passwort bei zwei Einträgen unterschiedliche Ciphertext-Ausgaben.
- **Key nur im Session-Speicher**: Das Master-Passwort wird nach dem Login im `sessionStorage` des Browsers abgelegt (`api.js`, Header `X-Master-Password`). Es verlässt den Browser nur über HTTPS und wird nach dem Schliessen des Tabs automatisch gelöscht.

### Authentifizierung: bcrypt + JWT

```java
// SecurityConfig.java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

- **bcrypt**: Das Master-Passwort wird mit BCrypt gehasht in der Datenbank gespeichert. BCrypt enthält intern einen Salt und ist bewusst langsam (Work-Factor), was Brute-Force erschwert.
- **JWT**: Nach erfolgreichem Login erhält der Client ein JWT (HS256, konfigurierbare Ablaufzeit via `app.jwt.expiration`). Jede API-Anfrage wird über einen `JwtAuthFilter` validiert, bevor sie verarbeitet wird.

```java
// JwtService.java – generateToken()
return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getKey(), SignatureAlgorithm.HS256)
        .compact();
```

### OWASP Top 10 – Massnahmen im Projekt

| # | Kategorie | Umsetzung |
|---|-----------|-----------|
| A01 | Broken Access Control | Jede API-Route ist JWT-gesichert. Einträge werden immer per `findByIdAndUserId` abgefragt – kein Nutzer kann fremde Daten lesen oder schreiben. |
| A02 | Cryptographic Failures | AES-256-CBC + PBKDF2 (65 536 Iter.) + zufälliger IV pro Eintrag. Kein schwaches MD5/SHA1. |
| A03 | Injection | Spring Data JPA verwendet parametrisierte Queries, kein rohes SQL. |
| A05 | Security Misconfiguration | CORS ist auf `localhost:5173` beschränkt; H2-Console nur in Entwicklung erreichbar; CSRF für die stateless REST-API deaktiviert. |
| A07 | Auth Failures | bcrypt-Hash + JWT-Token mit Ablaufzeit. Kein Session-Fixation-Problem (stateless). |
| A08 | Software Integrity | Abhängigkeiten via Maven mit fixierten Versionen; kein Fremdjscript ohne CSP. |

### Passwortgüte-Prüfung

Beim Anmelden und beim Erstellen bzw. Bearbeiten eines Eintrags wird das eingetippte Passwort in Echtzeit bewertet:

```js
// LoginPage.jsx / SafePage.jsx – checkPasswordStrength() – Pure Function
const checkPasswordStrength = (pw) => {
  const checks = [
    pw.length >= 8,        // mind. 8 Zeichen
    /[A-Z]/.test(pw),     // Grossbuchstabe
    /[a-z]/.test(pw),     // Kleinbuchstabe
    /[0-9]/.test(pw),     // Zahl
    /[^A-Za-z0-9]/.test(pw), // Sonderzeichen
  ]
  const score = checks.filter(Boolean).length
  // score 1–5 → Sehr schwach / Schwach / Mittel / Stark / Sehr stark
}
```

Die Stärke wird als farbiger Balken angezeigt (rot → gelb → grün). Da es sich um eine Pure Function handelt, liefert sie bei gleicher Eingabe immer dasselbe Ergebnis und kann ohne Mocking getestet werden.

### Herausforderungen und Learnings

- **IV-Verwaltung**: Anfangs wurde ein globaler IV pro User verwendet, was semantische Sicherheit verletzt. Korrekt ist ein zufälliger IV *pro Eintrag*, der zusammen mit dem Ciphertext gespeichert wird.
- **Key nicht cachen**: Beim Bearbeiten eines Eintrags wird ein neuer IV generiert und alle Felder neu verschlüsselt – so bleibt jeder Speichervorgang kryptographisch unabhängig.
- **Master-Passwort im Header**: Das Passwort wird bei jeder Anfrage im `X-Master-Password`-Header mitgeschickt, damit der Server den Key ableiten kann. Alternativ wäre eine serverseitige Session denkbar, die aber den stateless-Vorteil von JWT aufhebt.

---

## Modul 323 – Funktionale Programmierung

### Eingesetzte FP-Konzepte

#### 1. Pure Functions

Eine Pure Function hat **keine Seiteneffekte** und liefert bei gleicher Eingabe immer die gleiche Ausgabe.

**Frontend:**
```js
// SafePage.jsx
const filterEntries = (entries, search, categoryId) =>
  entries
    .filter(e => !categoryId || e.categoryId === categoryId)
    .filter(e => !search || ['title', 'url', 'username', 'email', 'notes']
      .some(f => (e[f] || '').toLowerCase().includes(search.toLowerCase())))
```
`filterEntries` verändert das originale `entries`-Array nie – es gibt immer eine neue gefilterte Liste zurück.

**Backend:**
```java
// AesEncryptionService.java – deriveKey() ist deterministisch
public SecretKey deriveKey(String masterPassword, String saltBase64) throws Exception {
    byte[] salt = Base64.getDecoder().decode(saltBase64);
    // gleiche Eingabe → gleicher Schlüssel, keine Seiteneffekte
    ...
}
```

#### 2. Immutable Data

Das Original-Array wird nie mutiert. Stattdessen wird eine neue Kopie erzeugt:

```js
// SafePage.jsx – sortEntries()
const sortEntries = (entries, field, asc) => {
  return [...entries].sort((a, b) => { ... })  // Spread erzeugt Kopie
}
```

```java
// PasswordEntryService.java – sortBy()
return entries.stream()
        .sorted(ascending ? comparator : comparator.reversed())
        .collect(Collectors.toList()); // neue Liste, Original bleibt unverändert
```

#### 3. Higher-Order Functions (HOF)

Eine Higher-Order Function nimmt Funktionen als Argumente oder gibt sie zurück.

**Backend – Predicate als Argument:**
```java
// PasswordEntryService.java – search()
Predicate<Map<String, Object>> matchesQuery = entry ->
        List.of("title", "url", "username", "email", "notes").stream()
                .anyMatch(field -> {
                    Object value = entry.get(field);
                    return value != null && value.toString().toLowerCase().contains(lowerQuery);
                });

return getAllDecrypted(userId, masterPassword).stream()
        .filter(matchesQuery)   // HOF: filter nimmt Predicate als Argument
        .collect(Collectors.toList());
```

**Backend – Comparator als HOF:**
```java
// PasswordEntryService.java – sortBy()
Comparator<Map<String, Object>> comparator = Comparator.comparing(
        entry -> Optional.ofNullable(entry.get(field)).map(Object::toString).orElse(""),
        String.CASE_INSENSITIVE_ORDER
);
return entries.stream().sorted(ascending ? comparator : comparator.reversed())...
```

#### 4. Lambda-Ausdrücke

```java
// DataInitializer.java – Seed-Verarbeitung via Lambda
seeds.stream()
        .forEach(s -> {
            entryService.create(userId, MASTER_PW, (String) s[0], ...);
        });
```

```java
// DataInitializer.java – Rubrik-Anlage via Lambda
catNames.forEach(name -> {
    Category cat = new Category();
    cat.setName(name);
    categoryRepository.save(cat);
});
```

#### 5. Stream API

Die Java Stream API erlaubt deklarative Verarbeitungspipelines ohne explizite Schleifen:

```java
// PasswordEntryService.java – getAllDecrypted()
return entryRepository.findByUserId(userId).stream()
        .map(entry -> decryptEntry(entry, key))   // Transformation
        .collect(Collectors.toList());            // Terminierung
```

Imperatives Äquivalent zum Vergleich:
```java
// Imperativ (nicht im Projekt)
List<Map<String, Object>> result = new ArrayList<>();
for (PasswordEntry entry : entryRepository.findByUserId(userId)) {
    result.add(decryptEntry(entry, key));
}
return result;
```

Der funktionale Stil ist kompakter, ausdrucksstärker und ohne Hilfsvariablen auskommen.

### Unterschied zu imperativem Stil

| Kriterium | Imperativ | Funktional (unser Projekt) |
|-----------|-----------|---------------------------|
| Iteration | `for`-Schleifen mit Index | `stream().map().filter()` |
| Mutation | Zustand wird direkt verändert | Neue Kopien, kein `set`-Aufruf |
| Lesbarkeit | Viel Boilerplate | Kompakte Pipelines |
| Testbarkeit | Seiteneffekte erschweren Unit-Tests | Pure Functions ohne Mocking testbar |
| Parallelisierung | Aufwändig | `parallelStream()` ohne Codeänderung |

### Vorteile im Projekt

- **Lesbarkeit**: `filterEntries(entries, search, categoryId)` ist sofort verständlich – kein mentales Durchlaufen von Schleifenindizes.
- **Testbarkeit**: Pure Functions wie `checkPasswordStrength` und `deriveKey` lassen sich ohne laufenden Server oder Datenbank testen.
- **Keine ungewollten Seiteneffekte**: Da `sortEntries` und `filterEntries` das Original nicht verändern, können Suche, Filter und Sortierung unabhängig voneinander angewendet werden, ohne inkonsistente Zustände zu erzeugen.

### Kompetenzstufe (Selbstdeklaration)

| Person | FP-Kompetenzstufe | Begründung |
|--------|-------------------|------------|
| Noel von Daeniken | Fortgeschritten | HOF, Stream API und reine Funktionen wurden selbstständig angewendet und auf Backend (Java) und Frontend (JS) übertragen. |
| Rinaldo Lanza | Grundlegend | Verständnis für Lambda-Syntax und `filter`/`map`, eigenständige Umsetzung einfacherer Pipelines. |

### Reflexion: Herausforderungen bei FP

- **Umgewöhnung**: Java ist primär objektorientiert. Lambdas und Streams fühlten sich anfangs ungewohnt an, besonders die `Comparator.comparing`-Syntax mit reversal.
- **Fehlerbehandlung in Streams**: In Java müssen checked Exceptions in Streams mit `try/catch` umhüllt werden, was den funktionalen Stil teilweise unterbricht (z. B. in `decryptEntry`).
- **Immutability in JPA**: JPA-Entities sind mutable by Design (`@Data`, Setter). Der funktionale Stil musste daher auf die Service-Schicht beschränkt werden; direkt in den Entities ist rein funktionales Programmieren nicht sinnvoll.
- **JavaScript vs. Java**: In JavaScript sind Arrow Functions und Array-Methoden wie `filter`, `map`, `some` sehr natürlich. In Java erfordert derselbe Effekt mehr Zeremonie (Imports, generische Typen, `Collectors`).
