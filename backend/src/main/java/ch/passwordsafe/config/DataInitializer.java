package ch.passwordsafe.config;

import ch.passwordsafe.entity.Category;
import ch.passwordsafe.entity.User;
import ch.passwordsafe.repository.CategoryRepository;
import ch.passwordsafe.repository.UserRepository;
import ch.passwordsafe.service.AesEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Erstellt beim Start einen Standard-Benutzer und Standard-Rubriken.
 * Login: admin / Admin123!
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AesEncryptionService aesService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByUsername("admin")) return;

        String salt = aesService.generateSalt();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setSalt(salt);
        userRepository.save(admin);

        // Standard-Rubriken (Modul 323: Pflicht-Erweiterung)
        List.of("Privat", "Schule", "Verein", "Arbeit", "Games")
                .forEach(name -> {
                    Category cat = new Category();
                    cat.setName(name);
                    cat.setUser(admin);
                    categoryRepository.save(cat);
                });

        System.out.println("✓ Standard-Benutzer angelegt: admin / Admin123!");
    }
}
