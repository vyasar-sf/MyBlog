package com.practical.myblog.security;

import com.practical.myblog.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found");
            return;
        }
        jwtToken = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwtToken).orElse(null);
        if (storedToken != null) {
            log.info("Token found in repository. Revoking token: {}", jwtToken);
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
            log.info("User logged out successfully and token revoked: {}", jwtToken);
        } else {
            log.warn("No token found in repository for token: {}", jwtToken);
        }
    }
}
