package ch.passwordsafe.service;

import ch.passwordsafe.entity.Category;
import ch.passwordsafe.entity.User;
import ch.passwordsafe.repository.CategoryRepository;
import ch.passwordsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<Category> getAll(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    public Category create(Long userId, String name) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = new Category();
        category.setName(name);
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category update(Long userId, Long categoryId, String newName) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Rubrik nicht gefunden"));
        category.setName(newName);
        return categoryRepository.save(category);
    }

    public void delete(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Rubrik nicht gefunden"));
        categoryRepository.delete(category);
    }
}
