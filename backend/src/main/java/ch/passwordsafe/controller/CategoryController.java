package ch.passwordsafe.controller;

import ch.passwordsafe.entity.Category;
import ch.passwordsafe.entity.User;
import ch.passwordsafe.repository.UserRepository;
import ch.passwordsafe.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(Principal principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(
            categoryService.getAll(userId).stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
                .toList()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(Principal principal,
                                                       @RequestBody Map<String, String> body) {
        Long userId = getUserId(principal);
        Category cat = categoryService.create(userId, body.get("name"));
        return ResponseEntity.ok(Map.of("id", cat.getId(), "name", cat.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(Principal principal,
                                                       @PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        Long userId = getUserId(principal);
        Category cat = categoryService.update(userId, id, body.get("name"));
        return ResponseEntity.ok(Map.of("id", cat.getId(), "name", cat.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        categoryService.delete(getUserId(principal), id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow().getId();
    }
}
