package com.kush.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kush.payload.dto.UserDTO;

public interface UserService {

    UserDTO createUser(UserDTO request);

    UserDTO getUserById(Long id);

    UserDTO getCurrentUser();

    UserDTO updateUser(Long id, UserDTO request);

    void deleteUser(Long id);

    Page<UserDTO> getAllUsers(String search, Pageable pageable);

    boolean userExists(String email);
}
