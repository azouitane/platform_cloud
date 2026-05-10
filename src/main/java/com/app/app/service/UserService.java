package com.app.app.service;


import com.app.app.dto.request.UpdateUserRequest;
import com.app.app.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getUserById(Long id);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
