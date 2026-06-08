package ch.passwordsafe.repository;

import ch.passwordsafe.entity.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByUserId(Long userId);
    List<PasswordEntry> findByUserIdAndCategoryId(Long userId, Long categoryId);
    Optional<PasswordEntry> findByIdAndUserId(Long id, Long userId);
}
