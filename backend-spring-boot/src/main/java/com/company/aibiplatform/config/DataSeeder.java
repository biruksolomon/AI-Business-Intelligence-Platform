package com.company.aibiplatform.config;

import com.company.aibiplatform.entity.Category;
import com.company.aibiplatform.entity.Role;
import com.company.aibiplatform.entity.User;
import com.company.aibiplatform.repository.CategoryRepository;
import com.company.aibiplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once on startup so there's always at least one login-able admin
 * account and a couple of starter categories — purely a developer
 * convenience, safe to delete once you have real data.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .fullName("Default Admin")
                    .email("admin@aibiplatform.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Seeded default admin: admin@aibiplatform.com / Admin@123 (change this immediately)");
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("laptop-accessories").build());
            categoryRepository.save(Category.builder().name("electronics").build());
            categoryRepository.save(Category.builder().name("kitchen").build());
        }
    }
}