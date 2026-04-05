package com.finance.finance_backend.service;

import com.finance.finance_backend.dto.UserRequest;
import com.finance.finance_backend.dto.UserResponse;
import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.exception.ResourceNotFoundException;
import com.finance.finance_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing users.
 * Admin-only operations: list, update, toggle status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves all users in the system.
     *
     * @return list of UserResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single user by ID.
     *
     * @param id the user ID
     * @return UserResponse DTO
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = findUserById(id);
        return UserResponse.fromEntity(user);
    }

    /**
     * Updates a user's name, role, and active status.
     *
     * @param id      the user ID
     * @param request the update request DTO
     * @return updated UserResponse DTO
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = findUserById(id);

        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive());

        userRepository.save(user);
        log.info("User updated successfully with id: {}", id);

        return UserResponse.fromEntity(user);
    }

    /**
     * Toggles the active/inactive status of a user.
     *
     * @param id the user ID
     * @return updated UserResponse DTO
     */
    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        log.info("Toggling status for user with id: {}", id);
        User user = findUserById(id);

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        log.info("User id: {} is now: {}",
                id, user.getIsActive() ? "ACTIVE" : "INACTIVE");

        return UserResponse.fromEntity(user);
    }

    /**
     * Fetches a User entity by ID or throws ResourceNotFoundException.
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
