package com.kush.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kush.enums.UserRole;
import com.kush.exception.BadRequestException;
import com.kush.exception.ConflictException;
import com.kush.exception.ForbiddenException;
import com.kush.exception.ResourceNotFoundException;
import com.kush.mapper.UserMapper;
import com.kush.model.User;
import com.kush.payload.dto.UserDTO;
import com.kush.repository.UserRepository;
import com.kush.security.SecurityUtils;
import com.kush.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO request) {
        String email = UserMapper.normalizeEmail(request.getEmail());
        assertEmailAvailable(email, null);

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.USER;
        User saved = userRepository.save(
                UserMapper.toEntity(request, passwordEncoder.encode(request.getPassword()), role)
        );
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return UserMapper.toDto(requireUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        return UserMapper.toDto(requireByEmail(SecurityUtils.requireEmail()));
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO request) {
        User user = requireUser(id);
        assertCanMutate(user);

        if (request.getEmail() != null) {
            String email = UserMapper.normalizeEmail(request.getEmail());
            assertEmailAvailable(email, id);
        }

        UserMapper.applyUpdate(user, request);

        if (request.getRole() != null && request.getRole() != user.getRole()) {
            if (!SecurityUtils.isAdmin()) {
                throw new ForbiddenException("Only an admin can change a user's role");
            }
            user.setRole(request.getRole());
        }

        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 8) {
                throw new BadRequestException("Password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = requireUser(id);
        if (user.getEmail().equalsIgnoreCase(SecurityUtils.requireEmail())) {
            throw new ForbiddenException("You cannot delete your own account");
        }
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(String search, Pageable pageable) {
        Page<User> users = StringUtils.hasText(search)
                ? userRepository.searchByKeyword(search.trim(), pageable)
                : userRepository.findAll(pageable);
        return users.map(UserMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String email) {
        return userRepository.existsByEmailIgnoreCase(UserMapper.normalizeEmail(email));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with id: " + id));
    }

    private User requireByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with email: " + email));
    }

    private void assertEmailAvailable(String email, Long currentId) {
        boolean taken = currentId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (taken) {
            throw new ConflictException("user with given email already exist");
        }
    }

    private void assertCanMutate(User user) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!user.getEmail().equalsIgnoreCase(SecurityUtils.requireEmail())) {
            throw new ForbiddenException("You can only update your own profile");
        }
    }
}
