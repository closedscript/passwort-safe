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
            new Object[]{"Google",         "https://google.com",         "max.muster",     "G00gle#2024!",     "max@gmail.com",       "",                        privat},
            new Object[]{"Netflix",        "https://netflix.com",        "max.muster",     "N3tflix@Stream",   "max@gmail.com",       "Familien-Abo",            privat},
            new Object[]{"GitHub",         "https://github.com",         "maxmuster",      "Gh_tok3n#Sec!",    "max@github.com",      "Persönliches Konto",     privat},
            new Object[]{"Instagram",      "https://instagram.com",      "max.muster",     "Insta!2024#Max",   "max@gmail.com",       "",                        privat},
            new Object[]{"Spotify",        "https://spotify.com",        "max.muster",     "Sp0tify#Music!",   "max@gmail.com",       "Premium",                 privat},
            new Object[]{"Amazon",         "https://amazon.de",          "max.muster",     "Am@zon#Shop24",    "max@gmail.com",       "",                        privat},
            new Object[]{"iCloud",         "https://icloud.com",         "max@icloud.com", "iCl0ud!@Apple",    "max@icloud.com",      "2FA aktiv",               privat},

            new Object[]{"Microsoft 365",  "https://office.com",         "max@schule.ch",  "M365!School#2024", "max@schule.ch",       "Schulkonto",              schule},
            new Object[]{"Moodle",         "https://moodle.ch",          "mmuster",        "M00dle#School!23", "max@schule.ch",       "",                        schule},
            new Object[]{"Teams",          "https://teams.microsoft.com","max@schule.ch",  "T3ams!Sch#24",     "max@schule.ch",       "Schulklassen-Chat",       schule},
            new Object[]{"Adobe CC",       "https://creativecloud.adobe.com","mmuster",    "Ad0be#CC!2024",    "max@gmail.com",       "Schülerlizenz",           schule},
            new Object[]{"Lucidchart",     "https://lucidchart.com",     "max@schule.ch",  "Lucid#Chart!23",   "max@schule.ch",       "UML-Diagramme",           schule},
            new Object[]{"Overleaf",       "https://overleaf.com",       "mmuster",        "0verle@f!Latex24", "max@gmail.com",       "LaTeX-Editor",            schule},

            new Object[]{"Vereinsportal",  "https://vereinsportal.ch",   "max.muster",     "Ver3in#Portal!24", "max@verein.ch",       "Mitgliederbereich",       verein},
            new Object[]{"Strava",         "https://strava.com",         "mmuster",        "Str@va!Run2024",   "max@gmail.com",       "Lauf-Tracker",            verein},
            new Object[]{"TeamApp",        "https://teamapp.com",        "max.muster",     "Te@mApp#Verein!",  "max@gmail.com",       "Vereins-App",             verein},
            new Object[]{"SuisseID",       "https://suisseid.ch",        "mmuster",        "Su1sse!D#Safe",    "max@gmail.com",       "E-Government Login",      verein},
            new Object[]{"SportApp",       "https://sportapp.com",       "max.muster",     "Sp0rtApp#2024!",   "max@gmail.com",       "Fitness-Tracker",         verein},

            new Object[]{"LinkedIn",       "https://linkedin.com",       "max-muster",     "L1nked!In#Jobs",   "max@linkedin.com",    "Berufsnetzwerk",          arbeit},
            new Object[]{"Slack",          "https://slack.com",          "max@firma.ch",   "Sl@ck#Chat!2024",  "max@firma.ch",        "Workspace: Firma",        arbeit},
            new Object[]{"Confluence",     "https://confluence.atlassian.com","mmuster",   "C0nfl!uence#Wiki", "max@firma.ch",        "Dokumentation",           arbeit},
            new Object[]{"Jira",           "https://jira.atlassian.com", "mmuster",        "J1ra#Sprint!24",   "max@firma.ch",        "Issue-Tracker",           arbeit},
            new Object[]{"Zoom",           "https://zoom.us",            "max@firma.ch",   "Z00m!Meet#2024",   "max@firma.ch",        "",                        arbeit},

            new Object[]{"Steam",          "https://store.steampowered.com","mmuster",     "St3am#Games!24",   "max@gmail.com",       "2FA aktiv",               games},
            new Object[]{"Epic Games",     "https://epicgames.com",      "mmuster",        "Ep!cG@mes#2024",   "max@gmail.com",       "Fortnite, Rocket League", games},
            new Object[]{"Battle.net",     "https://battle.net",         "mmuster#1234",   "B@ttl3Net!2024",   "max@gmail.com",       "Blizzard Konto",          games},
            new Object[]{"Nintendo",       "https://nintendo.com",       "mmuster",        "N1nt3ndo!@2024",   "max@gmail.com",       "Switch Online",           games},
            new Object[]{"PlayStation",    "https://playstation.com",    "mmuster",        "PS5#Play!2024",    "max@gmail.com",       "PS Plus Essential",       games}
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
