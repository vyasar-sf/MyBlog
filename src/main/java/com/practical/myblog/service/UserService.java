package com.practical.myblog.service;

import com.practical.myblog.dto.AuthenticationRequestDTO;
import com.practical.myblog.dto.AuthenticationResponseDTO;
import com.practical.myblog.dto.UserRequestDTO;
import com.practical.myblog.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    /**
     * Return list of all users
     * @return List of all users in the database
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Finds the user for the corresponding user ID
     * @param id
     * @return UserResponseDTO for the corresponding user ID
     */
    UserResponseDTO getUser(Long id);

    /**
     * Adds a new user
     * @param userRequestDTO
     * @return UserResponseDTO
     */
//    UserResponseDTO registerUser(UserRequestDTO userRequestDTO);

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
