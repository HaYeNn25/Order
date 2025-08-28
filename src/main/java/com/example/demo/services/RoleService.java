package com.example.demo.services;

import com.example.demo.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService{
    @Override
    public List<Role> getAllRoles() {
        return List.of();
    }
}
