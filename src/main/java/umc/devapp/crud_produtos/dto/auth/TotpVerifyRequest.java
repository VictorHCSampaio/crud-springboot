package umc.devapp.crud_produtos.dto.auth;

import jakarta.validation.constraints.NotNull;

public record TotpVerifyRequest(@NotNull Integer code) {
}
