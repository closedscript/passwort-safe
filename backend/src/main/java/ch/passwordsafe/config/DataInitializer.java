package ch.passwordsafe.config;

import ch.passwordsafe.entity.Category;
import ch.passwordsafe.entity.User;
import ch.passwordsafe.repository.CategoryRepository;
import ch.passwordsafe.repository.UserRepository;
import ch.passwordsafe.service.AesEncryptionService;
import ch.passwordsafe.service.PasswordEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Erstellt beim Start einen Standard-Benutzer, Rubriken und 28 Testeinträge.
 * Login: admin / Admin123!
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AesEncryptionService aesService;
    private final PasswordEntryService entryService;

    private static final String MASTER_PW = "Admin123!";

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByUsername("admin")) return;

        String salt = aesService.generateSalt();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode(MASTER_PW));
        admin.setSalt(salt);
        userRepository.save(admin);

        // Standard-Rubriken (Modul 323: Pflicht-Erweiterung)
        List<String> catNames = List.of("Privat", "Schule", "Verein", "Arbeit", "Games");
        catNames.forEach(name -> {
            Category cat = new Category();
            cat.setName(name);
            cat.setUser(admin);
            categoryRepository.save(cat);
        });

        Long userId = admin.getId();
        Map<String, Long> catIds = new java.util.LinkedHashMap<>();
        categoryRepository.findByUserId(userId)
                .forEach(c -> catIds.put(c.getName(), c.getId()));

        Long privat = catIds.get("Privat");
        Long schule = catIds.get("Schule");
        Long verein = catIds.get("Verein");
        Long arbeit = catIds.get("Arbeit");
        Long games  = catIds.get("Games");

        // 28 Testeinträge – Modul 323: Stream + Lambda in seedEntries()
        List<Object[]> seeds = List.of(
            // title, url, username, password, email, notes, categoryId
            new Object[]{"Google",         "https://google.com",         "admin",          "G00gle#2024!",     "noel@gmail.com",      "",                        privat},
            new Object[]{"Netflix",        "https://netflix.com",        "noel",           "N3tflix@Stream",   "noel@gmail.com",      "Familien-Abo",            privat},
            new Object[]{"GitHub",         "https://github.com",         "nveedee",        "Gh_tok3n#Sec!",    "noel@github.com",     "Persönliches Konto",     privat},
            new Object[]{"Instagram",      "https://instagram.com",      "noel.vd",        "Insta!2024#Noel",  "noel@gmail.com",      "",                        privat},
            new Object[]{"Spotify",        "https://spotify.com",        "noel",           "Sp0tify#Music!",   "noel@gmail.com",      "Premium",                 privat},
            new Object[]{"Amazon",         "https://amazon.de",          "noel.vd",        "Am@zon#Shop24",    "noel@gmail.com",      "",                        privat},
            new Object[]{"iCloud",         "https://icloud.com",         "noel@icloud.com","iCl0ud!@Apple",    "noel@icloud.com",     "2FA aktiv",               privat},

            new Object[]{"Microsoft 365",  "https://office.com",         "noel@bbw.ch",    "M365!BBW#2024",    "noel@bbw.ch",         "BBW Schulkonto",          schule},
            new Object[]{"Moodle BBW",     "https://moodle.bbw.ch",      "nveedee",        "M00dle#BBW!23",    "noel@bbw.ch",         "",                        schule},
            new Object[]{"Teams BBW",      "https://teams.microsoft.com","noel@bbw.ch",    "T3ams!BBW#24",     "noel@bbw.ch",         "Schulklassen-Chat",       schule},
            new Object[]{"Adobe CC",       "https://creativecloud.adobe.com","nveedee",    "Ad0be#CC!2024",    "noel@gmail.com",      "Schülerlizenz",           schule},
            new Object[]{"Lucidchart",     "https://lucidchart.com",     "noel@bbw.ch",    "Lucid#Chart!23",   "noel@bbw.ch",         "UML-Diagramme",           schule},
            new Object[]{"Overleaf",       "https://overleaf.com",       "nveedee",        "0verle@f!Latex24", "noel@gmail.com",      "LaTeX-Editor",            schule},

            new Object[]{"STV Portal",     "https://stv-fsg.ch",         "noel.vd",        "STV#Turnen!24",    "noel@stv.ch",         "Swiss Turnen Verein",     verein},
            new Object[]{"Strava",         "https://strava.com",         "nveedee",        "Str@va!Run2024",   "noel@gmail.com",      "Lauf-Tracker",            verein},
            new Object[]{"TeamApp",        "https://teamapp.com",        "noel.vd",        "Te@mApp#Verein!",  "noel@gmail.com",      "Vereins-App",             verein},
            new Object[]{"SuisseID",       "https://suisseid.ch",        "nveedee",        "Su1sse!D#Safe",    "noel@gmail.com",      "E-Government Login",      verein},
            new Object[]{"SportApp",       "https://sportapp.com",       "noel.vd",        "Sp0rtApp#2024!",   "noel@gmail.com",      "Fitness-Tracker",         verein},

            new Object[]{"LinkedIn",       "https://linkedin.com",       "noel-vd",        "L1nked!In#Jobs",   "noel@linkedin.com",   "Berufsnetzwerk",          arbeit},
            new Object[]{"Slack",          "https://slack.com",          "noel@firma.ch",  "Sl@ck#Chat!2024",  "noel@firma.ch",       "Workspace: BBW",          arbeit},
            new Object[]{"Confluence",     "https://confluence.atlassian.com","nveedee",   "C0nfl!uence#Wiki", "noel@firma.ch",       "Dokumentation",           arbeit},
            new Object[]{"Jira",           "https://jira.atlassian.com", "nveedee",        "J1ra#Sprint!24",   "noel@firma.ch",       "Issue-Tracker",           arbeit},
            new Object[]{"Zoom",           "https://zoom.us",            "noel@firma.ch",  "Z00m!Meet#2024",   "noel@firma.ch",       "",                        arbeit},

            new Object[]{"Steam",          "https://store.steampowered.com","nveedee",     "St3am#Games!24",   "noel@gmail.com",      "2FA aktiv",               games},
            new Object[]{"Epic Games",     "https://epicgames.com",      "nveedee",        "Ep!cG@mes#2024",   "noel@gmail.com",      "Fortnite, Rocket League", games},
            new Object[]{"Battle.net",     "https://battle.net",         "nveedee#1234",   "B@ttl3Net!2024",   "noel@gmail.com",      "Blizzard Konto",          games},
            new Object[]{"Nintendo",       "https://nintendo.com",       "nveedee",        "N1nt3ndo!@2024",   "noel@gmail.com",      "Switch Online",           games},
            new Object[]{"PlayStation",    "https://playstation.com",    "nveedee",        "PS5#Play!2024",    "noel@gmail.com",      "PS Plus Essential",       games}
        );

        // Modul 323: Stream + Lambda – jeder Seed-Eintrag wird über eine Higher-Order Function verarbeitet
        seeds.stream()
                .forEach(s -> {
                    try {
                        entryService.create(userId, MASTER_PW,
                                (String) s[0], (String) s[1], (String) s[2],
                                (String) s[3], (String) s[4], (String) s[5],
                                (Long)   s[6]);
                    } catch (Exception e) {
                        throw new RuntimeException("Seed fehlgeschlagen: " + s[0], e);
                    }
                });

        System.out.println("✓ Standard-Benutzer angelegt: admin / Admin123!");
        System.out.println("✓ " + seeds.size() + " Testeinträge erstellt");
    }
}
