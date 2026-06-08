package ch.passwordsafe.controller;

import ch.passwordsafe.repository.UserRepository;
import ch.passwordsafe.service.PasswordEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class PasswordEntryController {

    private final PasswordEntryService entryService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            Principal principal,
            @RequestHeader("X-Master-Password") String masterPassword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "true") boolean ascending) throws Exception {

        Long userId = getUserId(principal);
        List<Map<String, Object>> entries;

        if (search != null && !search.isBlank()) {
            entries = entryService.search(userId, masterPassword, search);
        } else if (categoryId != null) {
            entries = entryService.getByCategory(userId, categoryId, masterPassword);
        } else {
            entries = entryService.getAllDecrypted(userId, masterPassword);
        }

        if (sortField != null && !sortField.isBlank()) {
            entries = entryService.sortBy(entries, sortField, ascending);
        }

        return ResponseEntity.ok(entries);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(
            Principal principal,
            @RequestHeader("X-Master-Password") String masterPassword,
            @RequestBody Map<String, Object> body) throws Exception {

        Long userId = getUserId(principal);
        Long categoryId = body.get("categoryId") != null
                ? Long.parseLong(body.get("categoryId").toString()) : null;

        entryService.create(userId, masterPassword,
                (String) body.get("title"),
                (String) body.get("url"),
                (String) body.get("username"),
                (String) body.get("password"),
                (String) body.get("email"),
                (String) body.get("notes"),
                categoryId);

        return ResponseEntity.ok(Map.of("message", "Eintrag erstellt"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        entryService.delete(getUserId(principal), id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow().getId();
    }
}
