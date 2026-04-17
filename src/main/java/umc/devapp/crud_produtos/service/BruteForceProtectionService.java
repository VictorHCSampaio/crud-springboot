package umc.devapp.crud_produtos.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.devapp.crud_produtos.config.SecurityAuthProperties;
import umc.devapp.crud_produtos.entity.Usuario;
import umc.devapp.crud_produtos.repository.UsuarioRepository;

@Service
public class BruteForceProtectionService {

    private final SecurityAuthProperties properties;
    private final UsuarioRepository usuarioRepository;

    public BruteForceProtectionService(SecurityAuthProperties properties, UsuarioRepository usuarioRepository) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean isBlocked(Usuario usuario) {
        return usuario.getLockUntil() != null && usuario.getLockUntil().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void registerFailedAttempt(Usuario usuario) {
        usuario.setFailedAttempts(usuario.getFailedAttempts() + 1);
        if (usuario.getFailedAttempts() >= properties.maxAttempts()) {
            usuario.setLockUntil(LocalDateTime.now().plusMinutes(properties.lockDurationMinutes()));
            usuario.setFailedAttempts(0);
        }
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registerSuccessfulAttempt(Usuario usuario) {
        usuario.setFailedAttempts(0);
        usuario.setLockUntil(null);
        usuario.setLastLoginAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
}
