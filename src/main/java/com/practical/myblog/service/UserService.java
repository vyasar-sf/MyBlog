package com.practical.myblog.service;

import com.practical.myblog.dto.AuthenticationRequestDTO;
import com.practical.myblog.dto.AuthenticationResponseDTO;
import com.practical.myblog.dto.UserRequestDTO;
import com.practical.myblog.dto.UserResponseDTO;
import org.springframework.data.domain.Page;

public interface UserService {

    /**
     * Return list of all users
     * @param pageNo Page number
     * @param pageSize Page size
     * @return List of all users in the database
     */
    Page<UserResponseDTO> getAllUsers(int pageNo, int pageSize);

    /**
     * Finds the user for the corresponding user ID
     * @param id
     * @return UserResponseDTO for the corresponding user ID
     */
    UserResponseDTO getUser(Long id);

    /**
     * Deletes the user
     * @param id
     */
    void deleteUser(Long id);

    /**
     * Logs in user
     * @param request
     * @return AuthenticationResponse
     */
    AuthenticationResponseDTO authenticateUser(AuthenticationRequestDTO request);

    /**
     * Adds a new user
     * @param request
     * @return AuthenticationResponse
     */
    AuthenticationResponseDTO registerUser(UserRequestDTO request);
}
