package com.practical.myblog.service;

import com.practical.myblog.dto.*;
import com.practical.myblog.exception.UserValidationException;
import com.practical.myblog.model.Role;
import com.practical.myblog.model.Token;
import com.practical.myblog.model.TokenType;
import com.practical.myblog.model.User;
import com.practical.myblog.repository.TokenRepository;
import com.practical.myblog.repository.UserRepository;
import com.practical.myblog.security.JwtService;
import com.practical.myblog.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public Page<UserResponseDTO> getAllUsers(int pageNo, int pageSize) {
        log.info("Retrieving all users with pagination - Page: {}, Size: {}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return userRepository.findAll(pageable)
                .map(user -> modelMapper.map(user, UserResponseDTO.class));
    }

    @Override
    public UserResponseDTO getUser(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserResponseDTO.class))
                .orElseThrow(() -> new UserValidationException(ErrorMessages.USER_NOT_FOUND_WITH_ID + id));
    }


    @Override
    public void deleteUser(Long id) {
        log.info("Attempting to delete user with ID: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("User with ID: {} deleted successfully", id);
        } else {
            log.error("User not found with ID: {}", id);
            throw new UserValidationException(ErrorMessages.USER_NOT_FOUND_WITH_ID + id);
        }
    }

    @Override
    public AuthenticationResponseDTO authenticateUser(AuthenticationRequestDTO request) {
        log.info("User: {} is attempting to log in", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserValidationException(ErrorMessages.USER_NOT_FOUND_WITH_USERNAME + request.getUsername()));

        var jwtToken = jwtService.generateToken(user);
        log.info("Generated JWT token for user: {}", user.getUsername());

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthenticationResponseDTO registerUser(UserRequestDTO request) {
        log.info("User: {} is attempting to register", request.getUsername());

        String jwtToken = "";

        try {
            var user = User.builder()
                    .username(request.getUsername())
                    .displayName(request.getDisplayName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .build();
            var savedUser = userRepository.save(user);
            jwtToken = jwtService.generateToken(user);
            log.info("User registered successfully: {}", savedUser.getUsername());
            saveUserToken(savedUser, jwtToken);
        } catch (DataIntegrityViolationException e) {
            log.error("Username not unique: {}", request.getUsername());
            throw new UserValidationException(ErrorMessages.USERNAME_NOT_UNIQUE);
        }

        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .build();
    }


    private void saveUserToken(User user, String jwtToken) {
        log.info("Saving token for user: {}", user.getUsername());
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        log.info("Revoking all tokens for user: {}", user.getUsername());
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            log.info("No valid tokens found for user: {}", user.getUsername());
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
            log.info("Revoked token: {}", token.getToken());
        });
        tokenRepository.saveAll(validUserTokens);
        log.info("All valid tokens revoked for user: {}", user.getUsername());
    }

}
