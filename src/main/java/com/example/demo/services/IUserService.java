package com.example.demo.services;

import com.example.demo.dtos.UpdateUserDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.User;

public interface IUserService {

    User createUser(UserDTO userDTO) throws Exception;

    String login(String phoneNumber, String password, Long roleId) throws Exception;

    User getUserDetailsFromToken(String token) throws Exception;

    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;

    User getUserDetailsFromRefreshToken(String refreshToken) throws Exception;
}
