package umc.devapp.crud_produtos.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email
) {
}

