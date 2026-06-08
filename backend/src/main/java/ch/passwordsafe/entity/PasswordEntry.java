package ch.passwordsafe.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_entries")
@Data
@NoArgsConstructor
public class PasswordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String url;

    // AES-verschlüsselt, Base64-kodiert
    @Column(name = "username_encrypted")
    private String usernameEncrypted;

    @Column(name = "password_encrypted", nullable = false)
    private String passwordEncrypted;

    @Column(name = "email_encrypted")
    private String emailEncrypted;

    @Column(name = "notes_encrypted", length = 2000)
    private String notesEncrypted;

    // Initialization Vector pro Eintrag (Base64)
    @Column(nullable = false)
    private String iv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
