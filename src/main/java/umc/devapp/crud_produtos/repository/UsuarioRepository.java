package umc.devapp.crud_produtos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import umc.devapp.crud_produtos.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
}
