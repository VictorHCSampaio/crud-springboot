package umc.devapp.crud_produtos.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umc.devapp.crud_produtos.dto.auth.AuthMessageResponse;
import umc.devapp.crud_produtos.dto.auth.ConfirmPasswordResetRequest;
import umc.devapp.crud_produtos.dto.auth.LoginRequest;
import umc.devapp.crud_produtos.dto.auth.PasswordResetRequest;
import umc.devapp.crud_produtos.dto.auth.PasswordResetResponse;
import umc.devapp.crud_produtos.dto.auth.RegisterRequest;
import umc.devapp.crud_produtos.dto.auth.TotpSetupResponse;
import umc.devapp.crud_produtos.dto.auth.TotpVerifyRequest;
import umc.devapp.crud_produtos.service.AuthService;
import umc.devapp.crud_produtos.service.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<TotpSetupResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthMessageResponse> loginPrimaryStep(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {
        authService.loginPrimaryStep(request.username(), request.password(), session);
        return ResponseEntity.ok(new AuthMessageResponse("Senha validada. Envie o codigo 2FA."));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthMessageResponse> verifyTotp(
            @Valid @RequestBody TotpVerifyRequest request,
            HttpSession session,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authService.verifyTotpAndAuthenticate(request.code(), session, httpRequest, httpResponse);
        return ResponseEntity.ok(new AuthMessageResponse("Autenticacao concluida com sucesso."));
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<TotpSetupResponse> getTotpSetup(HttpSession session) {
        return ResponseEntity.ok(authService.getTotpSetup(session));
    }

    @PostMapping("/signout")
    public ResponseEntity<AuthMessageResponse> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(new AuthMessageResponse("Sessao invalidada com sucesso."));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<PasswordResetResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.generatePasswordResetToken(request.email());
        return ResponseEntity.ok(new PasswordResetResponse(
                "Email de recuperação enviado com sucesso. Verifique sua caixa de entrada."
        ));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<AuthMessageResponse> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new AuthMessageResponse("Senha resetada com sucesso. Faça login com sua nova senha."));
    }
}
