package com.clickfarma.backend;

import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Admin 1 - Douglas
			if (!usuarioRepository.existsByEmail("jdts1@discente.ifpe.edu.br")) {
				Usuario admin1 = new Usuario();
				admin1.setNome("Douglas (Admin)");
				admin1.setEmail("jdts1@discente.ifpe.edu.br");
				admin1.setSenha(passwordEncoder.encode("admin123click"));
				admin1.setRole("ADMIN");
				usuarioRepository.save(admin1);
				System.out.println("✅ Admin 1 (Douglas) criado!");
			}
			// Admin 2 - Gustavson
			if (!usuarioRepository.existsByEmail("gmb6@discente.ifpe.edu.br")) {
				Usuario admin2 = new Usuario();
				admin2.setNome("Gustavson (Admin)");
				admin2.setEmail("gmb6@discente.ifpe.edu.br");
				admin2.setSenha(passwordEncoder.encode("admin123click"));
				admin2.setRole("ADMIN");
				usuarioRepository.save(admin2);
				System.out.println("✅ Admin 2 (Gustavson) criado!");
			}
		};
	}
}
