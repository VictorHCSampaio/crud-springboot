package umc.devapp.crud_produtos.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Objects;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.LOCKED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import umc.devapp.crud_produtos.dto.auth.RegisterRequest;
import umc.devapp.crud_produtos.dto.auth.TotpSetupResponse;
import umc.devapp.crud_produtos.entity.Usuario;
import umc.devapp.crud_produtos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuthService {

    public static final String PRIMARY_AUTH_USER_ID = "PRIMARY_AUTH_USER_ID";

    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;


    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;
    private final TotpService totpService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final HttpSessionSecurityContextRepository securityContextRepository;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordService passwordService,
            TotpService totpService,
            BruteForceProtectionService bruteForceProtectionService,
            HttpSessionSecurityContextRepository securityContextRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
        this.totpService = totpService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public TotpSetupResponse register(RegisterRequest request) {
        usuarioRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new ResponseStatusException(BAD_REQUEST, "Usuario ja existe");
        });
        usuarioRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new ResponseStatusException(BAD_REQUEST, "Email ja cadastrado");
        });

        String secret = totpService.generateSecret();
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordService.hashPassword(request.password()))
                .totpSecret(secret)
                .enabled(true)
                .failedAttempts(0)
                .build();

        Usuario savedUsuario = Objects.requireNonNull(usuarioRepository.save(usuario));
        return new TotpSetupResponse(secret, totpService.buildQrUri(savedUsuario.getUsername(), secret));
    }

    @Transactional
    public void loginPrimaryStep(String username, String password, HttpSession session) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Credenciais invalidas"));

        if (bruteForceProtectionService.isBlocked(usuario)) {
            throw new ResponseStatusException(LOCKED, "Conta temporariamente bloqueada");
        }

        if (!passwordService.matches(password, usuario.getPasswordHash())) {
            bruteForceProtectionService.registerFailedAttempt(usuario);
            throw new ResponseStatusException(UNAUTHORIZED, "Credenciais invalidas");
        }

        session.setAttribute(PRIMARY_AUTH_USER_ID, usuario.getId());
    }

    @Transactional
    public void verifyTotpAndAuthenticate(
            int code,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Long userId = (Long) session.getAttribute(PRIMARY_AUTH_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Login primario nao realizado");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Usuario invalido"));

        // 🔐 Só exige 2FA fora do ambiente local
        if (!"local".equals(activeProfile)) {
            if (!totpService.verifyCode(usuario.getTotpSecret(), code)) {
                bruteForceProtectionService.registerFailedAttempt(usuario);
                throw new ResponseStatusException(UNAUTHORIZED, "Codigo TOTP invalido");
            }
        }

        bruteForceProtectionService.registerSuccessfulAttempt(usuario);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        session.removeAttribute(PRIMARY_AUTH_USER_ID);
    }

    @Transactional(readOnly = true)
    public TotpSetupResponse getTotpSetup(HttpSession session) {
        Long userId = (Long) session.getAttribute(PRIMARY_AUTH_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Login primario nao realizado");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Usuario invalido"));

        return new TotpSetupResponse(
                usuario.getTotpSecret(),
                totpService.buildQrUri(usuario.getUsername(), usuario.getTotpSecret())
        );
    }

    public void logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
    }
}
