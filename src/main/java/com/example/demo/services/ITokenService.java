package com.example.demo.services;

import com.example.demo.models.Token;
import com.example.demo.models.User;

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user) throws Exception;
}
