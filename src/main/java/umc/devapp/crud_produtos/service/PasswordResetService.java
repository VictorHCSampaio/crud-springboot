package umc.devapp.crud_produtos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import umc.devapp.crud_produtos.entity.Usuario;
import umc.devapp.crud_produtos.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private EmailService emailService;

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    @Transactional
    public void generatePasswordResetToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário com este email não encontrado"));

        String token = UUID.randomUUID().toString();
        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));

        usuarioRepository.save(usuario);

        // Enviar email com o token
        emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNome(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Token de recuperação inválido"));

        if (usuario.getPasswordResetTokenExpiry() == null || LocalDateTime.now().isAfter(usuario.getPasswordResetTokenExpiry())) {
            throw new ResponseStatusException(BAD_REQUEST, "Token de recuperação expirado");
        }

        usuario.setPasswordHash(passwordService.hashPassword(newPassword));
        usuario.setPasswordResetToken(null);
        usuario.setPasswordResetTokenExpiry(null);

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return usuarioRepository.findByPasswordResetToken(token)
                .map(usuario -> usuario.getPasswordResetTokenExpiry() != null && LocalDateTime.now().isBefore(usuario.getPasswordResetTokenExpiry()))
                .orElse(false);
    }
}
