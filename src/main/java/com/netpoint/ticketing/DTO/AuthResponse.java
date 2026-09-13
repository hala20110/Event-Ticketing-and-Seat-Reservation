package com.netpoint.ticketing.DTO;

import com.netpoint.ticketing.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private Role role;
    private Long userId;
}