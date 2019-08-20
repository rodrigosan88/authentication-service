package br.jus.pje.authentication.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.jus.pje.authentication.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long>{
	User findByUsername(String username);
}
