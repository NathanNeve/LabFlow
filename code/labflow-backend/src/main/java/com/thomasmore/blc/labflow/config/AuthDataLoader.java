package com.thomasmore.blc.labflow.config;

import com.thomasmore.blc.labflow.entity.auth.Rol;
import com.thomasmore.blc.labflow.entity.auth.User;
import com.thomasmore.blc.labflow.repository.auth.RolRepository;
import com.thomasmore.blc.labflow.repository.auth.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AuthDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthDataLoader.class);

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    public AuthDataLoader(UserRepository userRepository, RolRepository rolRepository) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) {
        String admin_password = "USER_ADMIN_PASSWORD";
        String nathan_password = "USER_NATHAN_PASSWORD";
        String cesar_password = "USER_CESAR_PASSWORD";

        String adminPw = System.getenv(admin_password);
        String nathanPw = System.getenv(nathan_password);
        String cesarPw = System.getenv(cesar_password);

        String adminRaw;
        String nathanRaw;
        String cesarRaw;
        if (adminPw == null || nathanPw == null || cesarPw == null) {
            Dotenv dotenv = Dotenv.configure().load();
            adminRaw = dotenv.get(admin_password);
            nathanRaw = dotenv.get(nathan_password);
            cesarRaw = dotenv.get(cesar_password);
        } else {
            adminRaw = adminPw;
            nathanRaw = nathanPw;
            cesarRaw = cesarPw;
        }

        // One bcrypt for storage. If the value already looks like a bcrypt hash (e.g.
        // from a secrets manager), use as-is.
        String adminPasswordHashed = toStoredPassword(adminRaw, admin_password);
        String nathanPasswordHashed = toStoredPassword(nathanRaw, nathan_password);
        String cesarPasswordHashed = toStoredPassword(cesarRaw, cesar_password);

        Rol rol_admin = new Rol("admin");
        Rol rol_student = new Rol("student");
        rolRepository.save(rol_admin);
        rolRepository.save(rol_student);

        User user0 = new User(adminPasswordHashed,
                "adminlabflow@digitalinnovation.be", "Admin", "DI", rol_admin);
        User user1 = new User(nathanPasswordHashed,
                "nathanneve@test.be", "Nathan", "Neve", rol_admin);
        User user2 = new User(cesarPasswordHashed,
                "césarvanleuffelen@test.be", "César", "van Leuffelen", rol_student);
        userRepository.save(user0);
        userRepository.save(user1);
        userRepository.save(user2);
    }

    private String toStoredPassword(String raw, String envKey) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Missing or empty password for " + envKey + " (set env vars or .env)");
        }
        if (raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$")) {
            return raw;
        }
        return encoder.encode(raw);
    }
}
