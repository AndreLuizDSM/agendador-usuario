package com.javanauta.usuario.infrastructure.config;

import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeederConfig {

    @Bean
    public CommandLineRunner seedAdminUser(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            String emailAdmin = "andre.teste.notificacao@gmail.com";

            // Regra de idempotência: só cria se não existir
            if (!repository.existsByEmail(emailAdmin)) {
                Usuario admin = new Usuario();
                admin.setNome("Recrutador Avaliador");
                admin.setEmail(emailAdmin);
                admin.setSenha(passwordEncoder.encode("senha123"));
                System.out.println("✅ Usuário de teste criado via Seeder: " + emailAdmin);
                repository.save(admin);
            }
        };
    }
}