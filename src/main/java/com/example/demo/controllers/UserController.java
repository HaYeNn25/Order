package com.example.demo.controllers;

import com.example.demo.components.JwtTokenUtils;
import com.example.demo.dtos.UpdateUserDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserLoginDTO;
import com.example.demo.models.User;
import com.example.demo.responses.LoginResponse;
import com.example.demo.responses.ObjectResponse;
import com.example.demo.responses.RegisterResponse;
import com.example.demo.responses.UserResponse;
import com.example.demo.services.IUserService;
import com.example.demo.components.LocalizationUtils;
import com.example.demo.untils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final JwtTokenUtils jwtTokenUtils;

    @PostMapping("register")
    public ResponseEntity<ObjectResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMess = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ObjectResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .message(errorMess.toString())
                    .build());
        }
        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            return ResponseEntity.badRequest().body(ObjectResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
                    .build());
        }
        User user = userService.createUser(userDTO);
        return ResponseEntity.ok(ObjectResponse.builder()
                .status(HttpStatus.CREATED)
                .data(UserResponse.fromUser(user))
                .message(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ObjectResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
    ) throws Exception {
        String token = userService.login(
                userLoginDTO.getPhoneNumber(),
                userLoginDTO.getPassword(),
                userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId());
        return ResponseEntity.ok(ObjectResponse.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                .data(token)
                .status(HttpStatus.OK)
                .build());
    }

    @PostMapping("/details")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ObjectResponse> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws Exception {
        String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
        User user = userService.getUserDetailsFromToken(extractedToken);
        return ResponseEntity.ok(
                ObjectResponse.builder()
                .message("Get user's detail successfully")
                .data(UserResponse.fromUser(user))
                .status(HttpStatus.OK)
                .build()
        );
    }

    @PutMapping("/details/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ResponseEntity<ObjectResponse> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws Exception{
        String extractedToken = authorizationHeader.substring(7);
        User user = userService.getUserDetailsFromToken(extractedToken);
        // Ensure that the user making the request matches the user being updated
        if (user.getId() != userId) {
            return ResponseEntity.ok(
                    ObjectResponse.builder()
                            .message("Update user detail fails")
                            .status(HttpStatus.FORBIDDEN)
                            .build()
            );
        }
        User updatedUser = userService.updateUser(userId, updatedUserDTO);
        return ResponseEntity.ok(
                ObjectResponse.builder()
                .message("Update user detail successfully")
                .data(UserResponse.fromUser(updatedUser))
                .status(HttpStatus.OK)
                .build()
        );
    }


}
