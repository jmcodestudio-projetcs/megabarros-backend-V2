package br.com.jmcodestudio.autenticacaotemplate.adapters.web.controller;

import br.com.jmcodestudio.autenticacaotemplate.adapters.web.dto.auth.*;
import br.com.jmcodestudio.autenticacaotemplate.application.port.in.AuthenticateUseCase;
import br.com.jmcodestudio.autenticacaotemplate.application.port.in.ChangePasswordUseCase;
import br.com.jmcodestudio.autenticacaotemplate.application.port.in.RefreshTokenUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController serves as a REST controller providing endpoints for authentication-related operations.
 *
 * It handles HTTP requests for login, token refresh, and password change functionalities,
 * utilizing use cases provided by the application core. The controller isolates
 * web-specific concerns from the business rules by leveraging data transfer objects (DTOs)
 * to manage input and output payloads.
 *
 * Endpoints:
 * - /auth/login: Authenticates a user and returns an authentication response containing tokens and user details.
 * - /auth/refresh: Refreshes the user's access token based on a valid refresh token.
 * - /auth/change-password: Changes the password of the authenticated user after validating the current password.
 *
 * O que faz: expõe endpoints HTTP e delega para os use cases.
 * Por que: controllers continuam finos, sem regra de negócio.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticateUseCase authenticate;
    private final RefreshTokenUseCase refresh;
    private final ChangePasswordUseCase changePassword;

    public AuthController(AuthenticateUseCase authenticate, RefreshTokenUseCase refresh, ChangePasswordUseCase changePassword) {
        this.authenticate = authenticate;
        this.refresh = refresh;
        this.changePassword = changePassword;
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid LoginRequestDTO body) {
        var result = authenticate.login(body.email(), body.senha());
        return new AuthResponseDTO(result.userId(), result.email(), result.role(), result.accessToken(), result.refreshToken());
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@RequestBody @Valid RefreshTokenRequestDTO body) {
        var result = refresh.refresh(body.refreshToken());
        return new AuthResponseDTO(result.userId(), result.email(), result.role(), result.accessToken(), result.refreshToken());
    }

    @PostMapping("/change-password")
    public void changePassword(@AuthenticationPrincipal(expression = "userId") Long userId,
                               @RequestBody @Valid ChangePasswordRequestDTO body) {
        changePassword.changePassword(userId, body.currentPassword(), body.newPassword());
    }
}