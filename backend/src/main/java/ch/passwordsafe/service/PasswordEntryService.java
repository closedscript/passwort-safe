package ch.passwordsafe.service;

import ch.passwordsafe.entity.Category;
import ch.passwordsafe.entity.PasswordEntry;
import ch.passwordsafe.entity.User;
import ch.passwordsafe.repository.CategoryRepository;
import ch.passwordsafe.repository.PasswordEntryRepository;
import ch.passwordsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasswordEntryService {

    private final PasswordEntryRepository entryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AesEncryptionService aesService;

    // ── Erstellen ─────────────────────────────────────────────────────────────

    public PasswordEntry create(Long userId, String masterPassword,
                                 String title, String url,
                                 String username, String password,
                                 String email, String notes,
                                 Long categoryId) throws Exception {

        User user = userRepository.findById(userId).orElseThrow();
        SecretKey key = aesService.deriveKey(masterPassword, user.getSalt());
        String iv = aesService.generateIv();

        PasswordEntry entry = new PasswordEntry();
        entry.setTitle(title);
        entry.setUrl(url);
        entry.setIv(iv);
        entry.setUsernameEncrypted(aesService.encrypt(username, key, iv));
        entry.setPasswordEncrypted(aesService.encrypt(password, key, iv));
        entry.setEmailEncrypted(aesService.encrypt(email, key, iv));
        entry.setNotesEncrypted(aesService.encrypt(notes, key, iv));
        entry.setUser(user);

        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(entry::setCategory);
        }

        return entryRepository.save(entry);
    }

    // ── Lesen & Entschlüsseln ─────────────────────────────────────────────────

    /**
     * Gibt alle Einträge eines Users zurück, entschlüsselt.
     * Verwendet Stream API + Lambda (Higher-Order Function) – Modul 323.
     */
    public List<Map<String, Object>> getAllDecrypted(Long userId, String masterPassword) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        SecretKey key = aesService.deriveKey(masterPassword, user.getSalt());

        // Stream + map = Higher-Order Function (Modul 323)
        return entryRepository.findByUserId(userId).stream()
                .map(entry -> decryptEntry(entry, key))
                .collect(Collectors.toList());
    }

    /**
     * Filtert Einträge nach Rubrik.
     * Pure function: gibt neue Liste zurück, Original bleibt unverändert (Immutability).
     */
    public List<Map<String, Object>> getByCategory(Long userId, Long categoryId, String masterPassword) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        SecretKey key = aesService.deriveKey(masterPassword, user.getSalt());

        // filter = Higher-Order Function mit Lambda-Predicate (Modul 323)
        Predicate<PasswordEntry> byCategory = entry ->
                entry.getCategory() != null && entry.getCategory().getId().equals(categoryId);

        return entryRepository.findByUserId(userId).stream()
                .filter(byCategory)
                .map(entry -> decryptEntry(entry, key))
                .collect(Collectors.toList());
    }

    /**
     * Sucht über alle entschlüsselten Felder (title, url, username, email).
     * Higher-Order Function: Funktion als Argument (Predicate<Map>).
     */
    public List<Map<String, Object>> search(Long userId, String masterPassword, String query) throws Exception {
        String lowerQuery = query.toLowerCase();

        // Pure function als Predicate: keine Seiteneffekte
        Predicate<Map<String, Object>> matchesQuery = entry ->
                List.of("title", "url", "username", "email", "notes").stream()
                        .anyMatch(field -> {
                            Object value = entry.get(field);
                            return value != null && value.toString().toLowerCase().contains(lowerQuery);
                        });

        return getAllDecrypted(userId, masterPassword).stream()
                .filter(matchesQuery)
                .collect(Collectors.toList());
    }

    /**
     * Sortiert Einträge nach einem Feld (ASC/DESC).
     * Pure function: gibt neue sortierte Liste zurück – original unverändert (Modul 323).
     */
    public List<Map<String, Object>> sortBy(List<Map<String, Object>> entries, String field, boolean ascending) {
        // Comparator als Higher-Order Function (Modul 323)
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                entry -> Optional.ofNullable(entry.get(field)).map(Object::toString).orElse(""),
                String.CASE_INSENSITIVE_ORDER
        );

        return entries.stream()
                .sorted(ascending ? comparator : comparator.reversed())
                .collect(Collectors.toList()); // neue Liste, Original bleibt immutable
    }

    // ── Löschen ───────────────────────────────────────────────────────────────

    public void delete(Long userId, Long entryId) {
        PasswordEntry entry = entryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new RuntimeException("Eintrag nicht gefunden"));
        entryRepository.delete(entry);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Entschlüsselt einen Eintrag zu einer Map.
     * Pure function: kein interner State verändert.
     */
    private Map<String, Object> decryptEntry(PasswordEntry entry, SecretKey key) {
        try {
            String iv = entry.getIv();
            return Map.of(
                    "id", entry.getId(),
                    "title", entry.getTitle(),
                    "url", Optional.ofNullable(entry.getUrl()).orElse(""),
                    "username", Optional.ofNullable(aesService.decrypt(entry.getUsernameEncrypted(), key, iv)).orElse(""),
                    "password", Optional.ofNullable(aesService.decrypt(entry.getPasswordEncrypted(), key, iv)).orElse(""),
                    "email", Optional.ofNullable(aesService.decrypt(entry.getEmailEncrypted(), key, iv)).orElse(""),
                    "notes", Optional.ofNullable(aesService.decrypt(entry.getNotesEncrypted(), key, iv)).orElse(""),
                    "categoryId", entry.getCategory() != null ? entry.getCategory().getId() : null,
                    "categoryName", entry.getCategory() != null ? entry.getCategory().getName() : null,
                    "createdAt", entry.getCreatedAt().toString(),
                    "updatedAt", entry.getUpdatedAt().toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Entschlüsselung fehlgeschlagen: " + entry.getId(), e);
        }
    }
}
