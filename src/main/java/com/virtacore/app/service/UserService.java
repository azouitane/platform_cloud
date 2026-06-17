package com.virtacore.app.service;


import com.virtacore.app.dto.request.UpdateUserRequest;
import com.virtacore.app.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse getUserById(UUID id);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);
}
