package ch.passwordsafe.service;

import ch.passwordsafe.dto.AuthDto;
import ch.passwordsafe.entity.User;
import ch.passwordsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Ungültige Anmeldedaten"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Ungültige Anmeldedaten");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthDto.LoginResponse(token, user.getUsername());
    }

    /**
     * Prüft Passwortgüte: mind. 8 Zeichen, Gross-/Kleinbuchstaben, Ziffer, Sonderzeichen.
     * Pure function: deterministisch, keine Seiteneffekte.
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
